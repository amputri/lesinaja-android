package com.lesinaja.les.ui.tutor.les.presensi

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.notifikasi.NotificationData
import com.lesinaja.les.base.notifikasi.PushNotification
import com.lesinaja.les.base.notifikasi.RetrofitInstance
import com.lesinaja.les.databinding.ActivityUbahJadwalTutorBinding
import com.lesinaja.les.ui.header.LoadingDialog
import com.lesinaja.les.ui.header.ToolbarFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UbahJadwalTutorActivity: AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    private lateinit var binding: ActivityUbahJadwalTutorBinding

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

    companion object {
        const val EXTRA_IDLESSISWA = "id_les_siswa"
        const val EXTRA_IDLESSISWATUTOR = "id_les_siswa_tutor"
        const val EXTRA_IDPRESENSI = "id_presensi"
        const val EXTRA_NAMASISWA = "nama_siswa"
        const val EXTRA_NAMALES = "nama_les"
        const val EXTRA_JUMLAHPERTEMUAN = "jumlah_pertemuan"
        const val EXTRA_TANGGAL = "tanggal"
        const val EXTRA_JAM = "jam"
        const val EXTRA_NAMATUTOR = "nama_tutor"
    }

    val TAG = "UJTActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUbahJadwalTutorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnKembali.setOnClickListener {
            onBackPressed()
        }
        setToolbar("Ubah Jadwal Les")

        updateUI()

        binding.ivKalender.setOnClickListener {
            showCalendar()
        }

        binding.btnKirimUbahJadwal.setOnClickListener {
            updateJadwal()
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
        binding.tvNamaSiswa.text = "${intent.getStringExtra(EXTRA_NAMASISWA)}"
        binding.tvNamaLes.text = "${intent.getStringExtra(EXTRA_NAMALES)}"
        binding.tvJumlahPertemuan.text = "${intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN)}"
        binding.tvTanggal.text = "${intent.getStringExtra(EXTRA_TANGGAL)} (${intent.getStringExtra(EXTRA_JAM).toString().substringBefore(" (").substringAfter("Jam ")})"
        binding.tvPertemuan.text = "${intent.getStringExtra(EXTRA_NAMATUTOR)}"
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

        binding.tvJadwalBaru.text = SimpleDateFormat("EEEE, dd MMMM yyyy").format(dateTime)+" Jam "+SimpleDateFormat("hh:mm aaa").format(dateTime)
    }

    private fun updateJadwal() {
        if (dateTime > 0) {
            val loading = LoadingDialog(this@UbahJadwalTutorActivity)
            loading.startLoading()

            Database.database.getReference("les_presensi/${intent.getStringExtra(EXTRA_IDLESSISWATUTOR)}/${intent.getStringExtra(EXTRA_IDPRESENSI)}/waktu").setValue(dateTime)
                .addOnSuccessListener {
                    loading.isDismiss()
                    pushNotifikasi()
                    Toast.makeText(this, "berhasil ubah jadwal", Toast.LENGTH_SHORT).show()
                    goToPresensi()
                }
                .addOnFailureListener {
                    loading.isDismiss()
                    Toast.makeText(this, "gagal ubah jadwal", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "data belum valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToPresensi() {
        Intent(this, PresensiTutorActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(PresensiTutorActivity.EXTRA_IDLESSISWA, intent.getStringExtra(EXTRA_IDLESSISWA))
            it.putExtra(PresensiTutorActivity.EXTRA_NAMASISWA, intent.getStringExtra(EXTRA_NAMASISWA).toString().substringAfter("Siswa: "))
            it.putExtra(PresensiTutorActivity.EXTRA_NAMALES, intent.getStringExtra(EXTRA_NAMALES).toString().substringAfter("Les: "))
            it.putExtra(PresensiTutorActivity.EXTRA_JUMLAHPERTEMUAN, intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN).toString().substringAfter("Jumlah Pertemuan: "))
            startActivity(it)
        }
    }

    private fun pushNotifikasi() {
        val siswa = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/id_siswa")
        siswa.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshotSiswa: DataSnapshot) {
                if (dataSnapshotSiswa.exists()) {
                    val walmur = Database.database.getReference("siswa/${dataSnapshotSiswa.value}/id_walimurid")
                    walmur.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshotWalmur: DataSnapshot) {
                            if (dataSnapshotWalmur.exists()) {
                                val token = Database.database.getReference("user/${dataSnapshotWalmur.value}/token")
                                token.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshotToken: DataSnapshot) {
                                        if (dataSnapshotToken.exists()) {
                                            PushNotification(
                                                NotificationData("Tutor mengubah Jadwal Les", "${intent.getStringExtra(EXTRA_NAMALES)} ${intent.getStringExtra(EXTRA_NAMASISWA)} ${binding.tvPertemuan.text}"),
                                                dataSnapshotToken.value.toString()
                                            ).also {
                                                sendNotification(it)
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {}
                                })
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
        }
    }
}