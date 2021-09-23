package com.lesinaja.les.ui.walimurid.les

import android.R
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
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
import com.lesinaja.les.databinding.ActivityTambahLesBinding
import com.lesinaja.les.ui.header.LoadingDialog
import com.lesinaja.les.ui.header.ToolbarFragment
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TambahLesActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    private lateinit var binding: ActivityTambahLesBinding

    var idLes = ""
    var gajiTutor = 0
    var idKabupaten = ""
    var biayaDaftar = 0
    var biayaLes = 0
    var total = 0
    var jumlahPertemuan = 0

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
        const val EXTRA_BIAYADAFTAR = "biaya_daftar"
        const val EXTRA_NAMASISWA = "nama_siswa"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahLesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnKembali.setOnClickListener {
            onBackPressed()
        }
        setToolbar("Tambah Les")

        updateUI()

        listJadwal = arrayOf()

        binding.textSiswa.text = "Siswa: ${intent.getStringExtra(EXTRA_NAMASISWA)}"

        setLesAdapter()

        binding.btnJadwal.setOnClickListener {
            if (listJadwal.size < jumlahPertemuan && jumlahPertemuan > 0)
                showCalendar()
        }

        binding.btnResetJadwal.setOnClickListener {
            listJadwal = arrayOf()
            binding.tvJadwal.text = ""
        }

        binding.btnTambahLes.setOnClickListener {
            if (validateInputData()) {
                addLes()
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

    private fun updateUI() {
        val statusBayar = Database.database.getReference("siswa/${intent.getStringExtra(EXTRA_IDSISWA)}/status_bayar")
        statusBayar.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotBayar: DataSnapshot) {
                if (dataSnapshotBayar.exists()) {
                    if (dataSnapshotBayar.value.toString() != "true") {
                        binding.tvBiayaDaftar.text = intent.getStringExtra(EXTRA_BIAYADAFTAR).toString()
                        binding.tvBiayaDaftarRupiah.text = "Rp ${NumberFormat.getNumberInstance(Locale("in", "ID")).format(intent.getStringExtra(EXTRA_BIAYADAFTAR).toString().toInt())}"
                    } else {
                        binding.tvBiayaDaftar.text = "-"
                        binding.tvBiayaDaftarRupiah.text = "-"
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
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
                                                                                        "${h.key}//${h.child("biaya").value.toString()}**${h.child("gaji_tutor").value.toString()}#${dataSnapshot.value.toString().substring(0,4)}",
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
                gajiTutor = selectedObject.id.substringAfter("**").substringBefore("#").toInt()
                idKabupaten =  selectedObject.id.substringAfter("#")

                if (selectedObject.nama != "pilih les") {
                    jumlahPertemuan = selectedObject.nama.substringAfter("(").substringBefore("x)").toInt()
                }

                if (binding.tvBiayaDaftar.text.toString() != "-") {
                    biayaDaftar = binding.tvBiayaDaftar.text.toString().toInt()
                }
                biayaLes = selectedObject.id.substringAfter("//").substringBefore("**").toInt()
                total = biayaDaftar + biayaLes
                binding.tvBiayaLes.text = biayaLes.toString()
                binding.tvBiayaLesRupiah.text = "Rp ${NumberFormat.getNumberInstance(Locale("in", "ID")).format(biayaLes)}"
                binding.tvTotal.text = "Rp ${NumberFormat.getNumberInstance(Locale("in", "ID")).format(total)}"
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

    private fun getGenderChecked(): String {
        if (binding.laki.isChecked) return "laki-laki"
        else if (binding.perempuan.isChecked) return "perempuan"
        else return "bebas"
    }

    private fun validateInputData(): Boolean {
        var status = true

        if (idLes == "0") status = false
        if (listJadwal.size == 0) status = false

        return status
    }

    private fun addLes() {
        val dataLes = DataLes(
            gajiTutor,
            idLes,
            intent.getStringExtra(EXTRA_IDSISWA).toString(),
            getGenderChecked(),
            "${idKabupaten}_${getGenderChecked()}",
            binding.tvBiayaLes.text.toString().toInt(),
            false
        )

        val loading = LoadingDialog(this@TambahLesActivity)
        loading.startLoading()

        val key = Database.database.getReference("les_siswa").push().key!!
        Database.database.getReference("les_siswa/${key}").setValue(dataLes)
            .addOnSuccessListener {
                val updates: MutableMap<String, Any> = HashMap()
                updates["jumlah_data/les_siswa"] = ServerValue.increment(1)
                updates["siswa/${intent.getStringExtra(EXTRA_IDSISWA)}/jumlah_les"] = ServerValue.increment(1)
                for (i in 0 until listJadwal.size) {
                    updates["les_siswa/${key}/waktu_mulai/${i}"] = listJadwal[i]
                }

                Database.database.reference.updateChildren(updates)
                    .addOnSuccessListener {
                        loading.isDismiss()
                        Toast.makeText(this, "berhasil ambil les", Toast.LENGTH_SHORT).show()
                        goToLes()
                    }
                    .addOnFailureListener {
                        loading.isDismiss()
                        Toast.makeText(this, "gagal tambah jadwal les", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                loading.isDismiss()
                Toast.makeText(this, "gagal ambil les", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToLes() {
        Intent(this, LesActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }
}