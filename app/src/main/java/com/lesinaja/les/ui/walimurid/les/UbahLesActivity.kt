package com.lesinaja.les.ui.walimurid.les

import android.R
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.View
import android.view.View.VISIBLE
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.umum.Wilayah
import com.lesinaja.les.base.walimurid.DataLes
import com.lesinaja.les.base.walimurid.LesKey
import com.lesinaja.les.base.walimurid.Pembayaran
import com.lesinaja.les.controller.walimurid.akun.DataLesController
import com.lesinaja.les.controller.walimurid.akun.PembayaranController
import com.lesinaja.les.controller.walimurid.akun.PendaftaranController
import com.lesinaja.les.databinding.ActivityTambahLesBinding
import com.lesinaja.les.ui.header.ToolbarFragment
import com.lesinaja.les.ui.tutor.akun.AkunTutorActivity
import com.lesinaja.les.ui.walimurid.siswa.UbahSiswaActivity
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class UbahLesActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    private lateinit var binding: ActivityTambahLesBinding
    private lateinit var imageBitmap: Bitmap

    var changeImage = "false"
    var idLes = ""
    var gajiTutor = 0

    var day = 0
    var month: Int = 0
    var year: Int = 0
    var hour: Int = 0
    var minute: Int = 0
    var myDay = 0
    var myMonth: Int = 0
    var myYear: Int = 0
    var myHour: Int = 0
    var myMinute: Int = 0
    var dateTime:Long = 0

    private lateinit var listJadwal: Array<Long>
    private lateinit var listJadwalLama: Array<Long>

    companion object {
        const val REQUEST_GALLERY = 100
        const val REQUEST_CAMERA = 101
        const val EXTRA_IDLESSISWA = "id_les_siswa"
        const val EXTRA_GAJITUTOR = "gaji_tutor"
        const val EXTRA_IDLES = "id_les"
        const val EXTRA_IDSISWA = "id_siswa"
        const val EXTRA_PREFERENSITUTOR = "preferensi_tutor"
        const val EXTRA_NAMASISWA = "nama_siswa"
        const val EXTRA_NAMALES = "nama_les"
        const val EXTRA_JUMLAHPERTEMUAN = "jumlah_pertemuan"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahLesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listJadwal = arrayOf()
        listJadwalLama = arrayOf()

        setToolbar("Ubah Les")

        binding.btnHapus.visibility = VISIBLE

        binding.textSiswa.text = "Siswa: ${intent.getStringExtra(EXTRA_NAMASISWA)}"

        binding.btnTambahLes.setOnClickListener {
            addLes()
            if (changeImage == "true") {
                uploadImage(imageBitmap, intent.getStringExtra(EXTRA_IDLESSISWA).toString())
            } else if (changeImage == "false") {
                uploadImageWithoutFile(intent.getStringExtra(EXTRA_IDLESSISWA).toString())
            }
            goToLes()
        }

        getBiayaDaftar()
        loadJadwal()

        setGenderChecked(intent.getStringExtra(EXTRA_PREFERENSITUTOR).toString())

        binding.btnJadwal.setOnClickListener {
            binding.tvJadwalLama.text = ""
            showCalendar()
        }

        binding.ivBukti.setOnClickListener {
            if (binding.textUnggah.text.toString().substringBefore(" --") != "pembayaran sudah diverifikasi admin") {
                openPhotoDialog()
            }
        }

        binding.btnTambahLes.setEnabled(true)

        binding.btnHapus.setOnClickListener {
            hapusLes()
        }
    }

    private fun setToolbar(judul: String) {
        val toolbarFragment = ToolbarFragment()
        val bundle = Bundle()

        bundle.putString("judul", judul)
        toolbarFragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(binding.header.id, toolbarFragment).commit()
    }

    private fun hapusLes() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("yakin ingin menghapus les ${intent.getStringExtra(EXTRA_NAMALES)}?")

        builder.setPositiveButton("Hapus") { p0,p1 ->
            val biaya = Database.database.getReference("pembayaran").orderByChild("id_pengirim").equalTo(intent.getStringExtra(EXTRA_IDLESSISWA))
            biaya.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (h in dataSnapshot.children) {
                        if (h.child("id_penerima").value.toString() == "null") {
                            Database.database.getReference("pembayaran/${h.key}").removeValue()
                            Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}").removeValue()
                            FirebaseStorage.getInstance().reference.child("bukti_bayar/${intent.getStringExtra(EXTRA_IDLESSISWA)}").delete()

                            Toast.makeText(applicationContext, "data berhasil dihapus", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(applicationContext, "tidak dapat menghapus les terverifikasi", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }

        builder.setNegativeButton("Batal") { p0,p1 ->
        }

        builder.show()
    }

    private fun goToLes() {
        Intent(this, LesActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }

    private fun loadJadwal() {
        val jadwal = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/waktu_mulai")
        jadwal.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (i in 0 until dataSnapshot.childrenCount) {
                    listJadwalLama = listJadwalLama.plus(dataSnapshot.child("${i}").value.toString().toLong())
                    binding.tvJadwalLama.text = binding.tvJadwalLama.text.toString()+ SimpleDateFormat("EEEE, dd MMMM yyyy").format(dataSnapshot.child("${i}").value)+" Jam "+ SimpleDateFormat("hh:mm aaa").format(dataSnapshot.child("${i}").value)+"\n"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun setGenderChecked(gender: String) {
        if (gender == "laki-laki") binding.laki.isChecked = true
        else if (gender == "perempuan") binding.perempuan.isChecked = true
        else binding.bebas.isChecked = true
    }

    private fun getBiayaDaftar() {
        val biaya = Database.database.getReference("pembayaran").orderByChild("id_pengirim").equalTo(intent.getStringExtra(EXTRA_IDLESSISWA))
        biaya.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (h in dataSnapshot.children) {
                    binding.textBiaya.text = "Biaya Pendaftaran: ${h.child("nominal").value.toString()} -- ${h.key}"
                    binding.tvLinkBukti.text = "${h.child("bukti").value.toString()}"
                    if (h.child("id_penerima").value.toString() != "null") {
                        binding.textUnggah.text = "pembayaran sudah diverifikasi admin -- ${h.key}"
                    } else {
                        binding.textUnggah.text = "unggah bukti pembayaran berikut -- ${h.key}"
                    }
                    Picasso.get().load(h.child("bukti").value.toString()).into(binding.ivBukti)
                    setLesAdapter()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun setLesAdapter() {
        var les = ArrayList<Wilayah>()
        les.add(Wilayah(
            "${intent.getStringExtra(EXTRA_IDLES).toString()}//${binding.textBiaya.text.toString().substringAfter(": ").substringBefore(" --")}**${intent.getStringExtra(EXTRA_GAJITUTOR)}",
            "${intent.getStringExtra(EXTRA_NAMALES).toString()} (${intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN).toString()}x)"
        ))

        if (binding.textUnggah.text.toString().substringBefore(" --") != "pembayaran sudah diverifikasi admin") {
            val alamat = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/kontak/id_desa")
            alamat.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val wilayah = Database.database.getReference(
                        "wilayah_provinsi/${
                            dataSnapshot.value.toString().substring(0, 2)
                        }/id_wilayah"
                    )
                    wilayah.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshotWilayah: DataSnapshot) {
                            val masterLes =
                                Database.database.getReference("master_les").orderByChild("wilayah")
                                    .equalTo(dataSnapshotWilayah.value.toString())
                            masterLes.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshotLes: DataSnapshot) {
                                    for (h in dataSnapshotLes.children) {
                                        val mapel =
                                            Database.database.getReference("master_mapel/${h.child("mapel").value}/nama")
                                        mapel.addValueEventListener(object : ValueEventListener {
                                            override fun onDataChange(dataSnapshotMapel: DataSnapshot) {
                                                val jenjang = Database.database.getReference(
                                                    "master_jenjangkelas/${
                                                        h.child("jenjangkelas").value
                                                    }/nama"
                                                )
                                                jenjang.addValueEventListener(object :
                                                    ValueEventListener {
                                                    override fun onDataChange(dataSnapshotJenjang: DataSnapshot) {
                                                        val paket = Database.database.getReference(
                                                            "master_paket/${
                                                                h.child("paket").value
                                                            }/jumlah_pertemuan"
                                                        )
                                                        paket.addValueEventListener(object :
                                                            ValueEventListener {
                                                            override fun onDataChange(
                                                                dataSnapshotPaket: DataSnapshot
                                                            ) {
                                                                if (intent.getStringExtra(
                                                                        EXTRA_IDLES
                                                                    ) != h.key
                                                                ) {
                                                                    les.add(
                                                                        Wilayah(
                                                                            "${h.key}//${h.child("biaya").value}**${
                                                                                h.child(
                                                                                    "gaji_tutor"
                                                                                ).value
                                                                            }",
                                                                            "${dataSnapshotMapel.value} ${dataSnapshotJenjang.value} (${dataSnapshotPaket.value}x)"
                                                                        )
                                                                    )
                                                                }
                                                            }

                                                            override fun onCancelled(databaseError: DatabaseError) {

                                                            }
                                                        })
                                                    }

                                                    override fun onCancelled(databaseError: DatabaseError) {}
                                                })
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {}
                                        })
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {}
                            })
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

        binding.spinLes.adapter = ArrayAdapter(
            this,
            R.layout.simple_list_item_1,
            les
        )

        binding.spinLes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val selectedObject = binding.spinLes.selectedItem as Wilayah
                idLes = selectedObject.id.substringBefore("//")
                gajiTutor = selectedObject.id.substringAfter("**").toInt()
                binding.textBiaya.text = "Biaya Pendaftaran: ${selectedObject.id.substringAfter("//").substringBefore("**")}"
            }
        }

    }

    fun showCalendar() {
        val calendar: Calendar = Calendar.getInstance()
        day = calendar.get(Calendar.DAY_OF_MONTH)
        month = calendar.get(Calendar.MONTH)
        year = calendar.get(Calendar.YEAR)
        val datePickerDialog = DatePickerDialog(this, this, year, month,day)
        datePickerDialog.show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        myDay = dayOfMonth
        myYear = year
        myMonth = month
        val calendar: Calendar = Calendar.getInstance()
        hour = calendar.get(Calendar.HOUR)
        minute = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(this, this, hour, minute,
            DateFormat.is24HourFormat(this))
        timePickerDialog.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        myHour = hourOfDay
        myMinute = minute

        dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm").parse("${myYear}-${(myMonth+1)}-${myDay} ${myHour}:${(myMinute)}").time
        listJadwal = listJadwal.plus(dateTime)

        binding.tvJadwal.text = binding.tvJadwal.text.toString()+""+SimpleDateFormat("EEEE, dd MMMM yyyy").format(dateTime)+" Jam "+SimpleDateFormat("hh:mm aaa").format(dateTime)+"\n"
    }

    private fun openPhotoDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Unggah Foto dari")
            .setPositiveButton("kamera",
                DialogInterface.OnClickListener { dialog, id ->
                    openCameraForImage()
                })
            .setNegativeButton("file",
                DialogInterface.OnClickListener { dialog, id ->
                    openGalleryForImage()
                })
        builder.show()
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun openCameraForImage() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(packageManager).also {
                startActivityForResult(intent, REQUEST_CAMERA)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == AkunTutorActivity.REQUEST_GALLERY) {
                binding.ivBukti.setImageURI(data?.data)
                imageBitmap = (binding.ivBukti.drawable as BitmapDrawable).bitmap
            } else if (requestCode == AkunTutorActivity.REQUEST_CAMERA) {
                imageBitmap = data?.extras?.get("data") as Bitmap
                binding.ivBukti.setImageBitmap(imageBitmap)
            }

            changeImage = "true"
        }
    }

    fun uploadImage(imageBitmap: Bitmap, keyLes: String) {
        val pembayaran = Pembayaran(
            true,
            "",
            "admin",
            keyLes,
            binding.textBiaya.text.toString().substringAfter(": ").toInt(),
            PembayaranController().getCurrentDateTime()
        )
        PembayaranController().uploadImage(imageBitmap, keyLes, binding.textUnggah.text.toString().substringAfter("-- "), pembayaran)
    }

    fun uploadImageWithoutFile(keyLes: String) {
        Database.database.getReference("pembayaran/${binding.textUnggah.text.toString().substringAfter("-- ")}/id_pengirim").setValue("${keyLes}")
        Database.database.getReference("pembayaran/${binding.textUnggah.text.toString().substringAfter("-- ")}/nominal").setValue(binding.textBiaya.text.toString().substringAfter(": "))
    }

    fun addLes() {
        val dataLes = DataLes(
            gajiTutor,
            idLes,
            intent.getStringExtra(EXTRA_IDSISWA).toString(),
            getGenderChecked()
        )
        if (binding.tvJadwalLama.text.toString() != "") {
            DataLesController().changeDataLesUpdate(dataLes, intent.getStringExtra(EXTRA_IDLESSISWA).toString(), listJadwalLama)
        } else {
            DataLesController().changeDataLesUpdate(dataLes, intent.getStringExtra(EXTRA_IDLESSISWA).toString(), listJadwal)
        }
    }

    private fun getGenderChecked(): String {
        if (binding.laki.isChecked) return "laki-laki"
        else if (binding.perempuan.isChecked) return "perempuan"
        else return "bebas"
    }
}