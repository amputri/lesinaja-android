package com.lesinaja.les.ui.walimurid.les

import android.R
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.umum.Wilayah
import com.lesinaja.les.base.walimurid.DataLes
import com.lesinaja.les.base.walimurid.Pembayaran
import com.lesinaja.les.controller.walimurid.akun.DataLesController
import com.lesinaja.les.controller.walimurid.akun.PembayaranController
import com.lesinaja.les.databinding.ActivityTambahLesBinding
import com.lesinaja.les.ui.header.ToolbarFragment
import com.lesinaja.les.ui.tutor.akun.AkunTutorActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TambahLesActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    private lateinit var binding: ActivityTambahLesBinding
    private lateinit var imageBitmap: Bitmap

    var idLes = ""
    var keyLes = ""
    var keyPembayaran = ""
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

    companion object {
        const val EXTRA_IDSISWA = "id_siswa"
        const val EXTRA_NAMASISWA = "nama_siswa"
        const val REQUEST_GALLERY = 100
        const val REQUEST_CAMERA = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahLesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setToolbar("Ambil Les")

        listJadwal = arrayOf()

        binding.textSiswa.text = "Siswa: ${intent.getStringExtra(EXTRA_NAMASISWA)}"

        setLesAdapter()

        binding.btnJadwal.setOnClickListener {
            showCalendar()
        }

        binding.ivBukti.setOnClickListener {
            openPhotoDialog()
        }

        binding.btnTambahLes.setOnClickListener {
            if (validateInputData()) {
                addLes()
                uploadImage(imageBitmap, keyLes)
                Toast.makeText(this, "berhasil ambil les", Toast.LENGTH_SHORT).show()
                goToLes()
            } else {
                Toast.makeText(this, "data belum valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setToolbar(judul: String) {
        val toolbarFragment = ToolbarFragment()
        val bundle = Bundle()

        bundle.putString("judul", judul)
        toolbarFragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(binding.header.id, toolbarFragment).commit()
    }

    private fun setLesAdapter() {
        var les = ArrayList<Wilayah>()
        les.add(Wilayah("0", "pilih les"))

        val alamat = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/kontak/id_desa")
        alamat.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val wilayah = Database.database.getReference("wilayah_provinsi/${dataSnapshot.value.toString().substring(0,2)}/id_wilayah")
                    wilayah.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshotWilayah: DataSnapshot) {
                            if (dataSnapshotWilayah.exists()) {
                                val masterLes = Database.database.getReference("master_les").orderByChild("wilayah").equalTo(dataSnapshotWilayah.value.toString())
                                masterLes.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshotLes: DataSnapshot) {
                                        if (dataSnapshotLes.exists()) {
                                            for (h in dataSnapshotLes.children) {
                                                val mapel = Database.database.getReference("master_mapel/${h.child("mapel").value}/nama")
                                                mapel.addValueEventListener(object : ValueEventListener {
                                                    override fun onDataChange(dataSnapshotMapel: DataSnapshot) {
                                                        if (dataSnapshotMapel.exists()) {
                                                            val jenjang = Database.database.getReference("master_jenjangkelas/${h.child("jenjangkelas").value}/nama")
                                                            jenjang.addValueEventListener(object : ValueEventListener {
                                                                override fun onDataChange(dataSnapshotJenjang: DataSnapshot) {
                                                                    if (dataSnapshotJenjang.exists()) {
                                                                        val paket = Database.database.getReference("master_paket/${h.child("paket").value}/jumlah_pertemuan")
                                                                        paket.addValueEventListener(object : ValueEventListener {
                                                                            override fun onDataChange(dataSnapshotPaket: DataSnapshot) {
                                                                                if (dataSnapshotPaket.exists()) {
                                                                                    les.add(Wilayah(
                                                                                        "${h.key}//${h.child("biaya").value}**${h.child("gaji_tutor").value}",
                                                                                        "${dataSnapshotMapel.value} ${dataSnapshotJenjang.value} (${dataSnapshotPaket.value}x)"
                                                                                    ))
                                                                                }
                                                                            }
                                                                            override fun onCancelled(databaseError: DatabaseError) {}
                                                                        })
                                                                    }
                                                                }
                                                                override fun onCancelled(databaseError: DatabaseError) {}
                                                            })
                                                        }
                                                    }
                                                    override fun onCancelled(databaseError: DatabaseError) {}
                                                })
                                            }
                                        }
                                    }
                                    override fun onCancelled(databaseError: DatabaseError) {}
                                })
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        binding.spinLes.adapter = ArrayAdapter(
            this,
            R.layout.simple_list_item_1,
            les
        )

        binding.spinLes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
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

        val timePickerDialog = TimePickerDialog(this, this, hour, minute, DateFormat.is24HourFormat(this))
        timePickerDialog.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        myHour = hourOfDay
        myMinute = minute

        dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm").parse("${myYear}-${(myMonth+1)}-${myDay} ${myHour}:${(myMinute)}").time

        listJadwal = listJadwal.plus(dateTime)

        binding.tvJadwal.text = binding.tvJadwal.text.toString()+SimpleDateFormat("EEEE, dd MMMM yyyy").format(dateTime)+" Jam "+SimpleDateFormat("hh:mm aaa").format(dateTime)+"\n"
    }

    private fun openPhotoDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Unggah Foto dari")
        builder.setPositiveButton("kamera") { p0,p1 ->
            openCameraForImage()
        }
        builder.setNegativeButton("file") { p0,p1 ->
            openGalleryForImage()
        }
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

            binding.btnTambahLes.setEnabled(true)
        }
    }

    private fun getGenderChecked(): String {
        if (binding.laki.isChecked) return "laki-laki"
        else if (binding.perempuan.isChecked) return "perempuan"
        else return "bebas"
    }

    private fun validateInputData(): Boolean {
        var status = true

        if (idLes == "0") status = false
        if (listJadwal.size == 0) status = false
        if (binding.textBiaya.text == "") status = false

        return status
    }

    private fun addLes() {
        keyLes = DataLesController().getNewKey()
        val dataLes = DataLes(
            gajiTutor,
            idLes,
            intent.getStringExtra(EXTRA_IDSISWA).toString(),
            getGenderChecked()
        )
        DataLesController().changeDataLes(dataLes, keyLes, listJadwal)
    }

    private fun uploadImage(imageBitmap: Bitmap, keyLes: String) {
        keyPembayaran = PembayaranController().getNewKey()
        val pembayaran = Pembayaran(
            keyLes,
            "",
            "",
            Autentikasi.auth.currentUser?.uid!!,
            intent.getStringExtra(EXTRA_IDSISWA).toString(),
            binding.textBiaya.text.toString().substringAfter(": ").toInt(),
            PembayaranController().getCurrentDateTime(),
            false
        )
        PembayaranController().uploadImage(imageBitmap, keyLes, keyPembayaran, pembayaran)
        Database.database.getReference("jumlah_data/les_siswa").setValue(ServerValue.increment(1))
        Database.database.getReference("jumlah_data/pembayaran").setValue(ServerValue.increment(1))
    }

    private fun goToLes() {
        Intent(this, LesActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }
}