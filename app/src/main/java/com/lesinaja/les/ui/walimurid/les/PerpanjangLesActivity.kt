package com.lesinaja.les.ui.walimurid.les

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.PerpanjangLes
import com.lesinaja.les.databinding.ActivityPerpanjangLesBinding
import com.lesinaja.les.ui.header.ToolbarFragment
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PerpanjangLesActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    private lateinit var binding: ActivityPerpanjangLesBinding

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
        const val EXTRA_IDLESSISWA = "id_les_siswa"
        const val EXTRA_NAMASISWA = "nama_siswa"
        const val EXTRA_NAMALES = "nama_les"
        const val EXTRA_JUMLAHPERTEMUAN = "jumlah_pertemuan"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerpanjangLesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnKembali.setOnClickListener {
            goToLes()
        }
        setToolbar("Perpanjang Les")

        updateUI()

        listJadwal = arrayOf()

        binding.textSiswa.text = "Siswa: ${intent.getStringExtra(EXTRA_NAMASISWA)}"
        binding.tvLes.text = "${intent.getStringExtra(EXTRA_NAMALES)} (${intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN)}x)"

        binding.btnJadwal.setOnClickListener {
            showCalendar()
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
        val les = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/biaya_les")
        les.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshotLes: DataSnapshot) {
                if (dataSnapshotLes.exists()) {
                    binding.tvBiayaDaftar.text = "-"
                    binding.tvBiayaDaftarRupiah.text = "-"
                    binding.tvBiayaLes.text = dataSnapshotLes.value.toString()
                    binding.tvBiayaLesRupiah.text = "Rp ${NumberFormat.getNumberInstance(Locale("in", "ID")).format(dataSnapshotLes.value.toString().toInt())}"
                    binding.tvTotal.text = binding.tvBiayaLesRupiah.text.toString()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
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

        binding.tvJadwal.text = binding.tvJadwal.text.toString()+ SimpleDateFormat("EEEE, dd MMMM yyyy").format(dateTime)+" Jam "+ SimpleDateFormat("hh:mm aaa").format(dateTime)+"\n"
    }

    private fun getGenderChecked(): String {
        if (binding.laki.isChecked) return "laki-laki"
        else if (binding.perempuan.isChecked) return "perempuan"
        else if (binding.bebas.isChecked) return "bebas"
        else return "tetap"
    }

    private fun validateInputData(): Boolean {
        var status = true
        if (listJadwal.size == 0) status = false
        return status
    }

    private fun addLes() {
        val les = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}")
        les.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshotLes: DataSnapshot) {
                if (dataSnapshotLes.exists()) {
                    val alamat = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/kontak/id_desa")
                    alamat.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                var gender = ""
                                var wilayahPreferensi = "les"
                                var idtutor:Any? = null
                                if (getGenderChecked() == "tetap") {
                                    gender = dataSnapshotLes.child("preferensi_tutor").value.toString()
                                    idtutor = dataSnapshotLes.child("id_tutor").value.toString()
                                } else {
                                    gender = getGenderChecked()
                                    wilayahPreferensi = "${dataSnapshot.value.toString().substring(0,4)}_${gender}"
                                }

                                val dataLes = PerpanjangLes(
                                    dataSnapshotLes.child("gaji_tutor").value.toString().toInt(),
                                    dataSnapshotLes.child("id_les").value.toString(),
                                    dataSnapshotLes.child("id_siswa").value.toString(),
                                    gender,
                                    wilayahPreferensi,
                                    binding.tvBiayaLes.text.toString().toInt(),
                                    false,
                                    idtutor
                                )
                                val key = Database.database.getReference("les_siswa").push().key!!
                                Database.database.getReference("les_siswa/${key}").setValue(dataLes)
                                    .addOnSuccessListener {
                                        val updates: MutableMap<String, Any> = HashMap()
                                        updates["jumlah_data/les_siswa"] = ServerValue.increment(1)
                                        updates["siswa/${dataSnapshotLes.child("id_siswa").value}/jumlah_les"] = ServerValue.increment(1)
                                        for (i in 0 until listJadwal.size) {
                                            updates["les_siswa/${key}/waktu_mulai/${i}"] = listJadwal[i]
                                        }
                                        Database.database.reference.updateChildren(updates)
                                            .addOnSuccessListener {
                                                Toast.makeText(this@PerpanjangLesActivity, "berhasil ambil les", Toast.LENGTH_SHORT).show()
                                                goToLes()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(this@PerpanjangLesActivity, "gagal tambah jadwal les", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this@PerpanjangLesActivity, "gagal ambil les", Toast.LENGTH_SHORT).show()
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

    private fun goToLes() {
        Intent(this, LesActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }
}