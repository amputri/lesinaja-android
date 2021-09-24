package com.lesinaja.les.ui.walimurid.les.presensi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.notifikasi.NotificationData
import com.lesinaja.les.base.notifikasi.PushNotification
import com.lesinaja.les.base.notifikasi.RetrofitInstance
import com.lesinaja.les.databinding.ActivityLaporanBinding
import com.lesinaja.les.ui.header.LoadingDialog
import com.lesinaja.les.ui.header.ToolbarFragment
import com.lesinaja.les.ui.tutor.lowongan.DetailLowonganActivity
import com.lesinaja.les.ui.walimurid.les.BayarLesActivity
import com.lesinaja.les.ui.walimurid.les.pelamar.DetailTutorPelamarActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.HashMap

class LaporanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaporanBinding

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

    val TAG = "LAActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnKembali.setOnClickListener {
            onBackPressed()
        }
        setToolbar("Laporan Les")

        updateUI()

        binding.btnKirimLaporan.setOnClickListener {
            updateLaporan()
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
        binding.tvNamaTutor.text = "${intent.getStringExtra(EXTRA_NAMATUTOR)}"
        binding.btnTanggal.text = "${intent.getStringExtra(EXTRA_TANGGAL)} (${intent.getStringExtra(EXTRA_JAM).toString().substringBefore(" (").substringAfter("Jam ")})"
        binding.tvPertemuan.text = "${intent.getStringExtra(EXTRA_JAM).toString().substringAfter("(").substringBefore(")")}"

        loadDataLaporan()
    }

    private fun loadDataLaporan() {
        val laporan = Database.database.getReference("les_laporan/${intent.getStringExtra(EXTRA_IDLESSISWATUTOR)}/${intent.getStringExtra(EXTRA_IDPRESENSI)}")
        laporan.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("komentar_walimurid").exists()) {
                        binding.btnKirimLaporan.text = "Simpan Perubahan"
                        binding.etLaporanWaliMurid.setText(dataSnapshot.child("komentar_walimurid").value.toString())
                        binding.ratingTutor.rating = dataSnapshot.child("rating_tutor").value.toString().toFloat()
                    }
                    if (dataSnapshot.child("materi").exists()) {
                        binding.tvMateri.text = "Materi: ${dataSnapshot.child("materi").value.toString()}"
                        binding.tvLaporanTutor.text = "Laporan: ${dataSnapshot.child("laporan_tutor").value.toString()}"
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun updateLaporan() {
        if (binding.tvMateri.text == "") {
            Toast.makeText(this, "tutor belum mengisi laporan", Toast.LENGTH_SHORT).show()
        }
        else if (binding.etLaporanWaliMurid.text.toString().trim() != "" && binding.ratingTutor.rating > 0) {
            val loading = LoadingDialog(this@LaporanActivity)
            loading.startLoading()

            val admin = Database.database.getReference("user").orderByChild("roles/admin").equalTo(true)
            admin.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshotAdmin: DataSnapshot) {
                    if (dataSnapshotAdmin.exists()) {
                        for (h in dataSnapshotAdmin.children) {
                            val gajiTutor = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/gaji_tutor")
                            gajiTutor.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshotGajiTutor: DataSnapshot) {
                                    val jumlahPertemuan = Database.database.getReference("les_siswatutor/${intent.getStringExtra(EXTRA_IDLESSISWATUTOR)}/jumlah_presensi")
                                    jumlahPertemuan.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(dataSnapshotJumlahPertemuan: DataSnapshot) {
                                            val updates: MutableMap<String, Any> = HashMap()

                                            if (binding.tvJumlahPertemuan.text.toString().substringAfter("Jumlah Pertemuan: ") == binding.tvPertemuan.text.toString().substringAfter("Pertemuan ke-") && binding.btnKirimLaporan.text.toString() != "Simpan Perubahan") {
                                                val keyPembayaran = Database.database.getReference("pembayaran").push().key!!
                                                updates["jumlah_data/pembayaran"] = ServerValue.increment(1)
                                                updates["pembayaran/${keyPembayaran}/idlessiswa"] = intent.getStringExtra(EXTRA_IDLESSISWA).toString()
                                                updates["pembayaran/${keyPembayaran}/gaji_tutor"] = dataSnapshotGajiTutor.value.toString().toInt() * dataSnapshotJumlahPertemuan.value.toString().toInt()
                                                updates["pembayaran/${keyPembayaran}/id_lessiswatutor"] = intent.getStringExtra(EXTRA_IDLESSISWATUTOR).toString()
                                                updates["pembayaran/${keyPembayaran}/id_pengirim"] = h.key!!
                                                updates["pembayaran/${keyPembayaran}/sudah_dikonfirmasi"] = false
                                            }

                                            updates["les_laporan/${intent.getStringExtra(EXTRA_IDLESSISWATUTOR)}/${intent.getStringExtra(EXTRA_IDPRESENSI)}/komentar_walimurid"] = binding.etLaporanWaliMurid.text.toString().trim()
                                            updates["les_laporan/${intent.getStringExtra(EXTRA_IDLESSISWATUTOR)}/${intent.getStringExtra(EXTRA_IDPRESENSI)}/rating_tutor"] = binding.ratingTutor.rating
                                            updates["les_presensi/${intent.getStringExtra(EXTRA_IDLESSISWATUTOR)}/${intent.getStringExtra(EXTRA_IDPRESENSI)}/sudah_laporan"] = true

                                            Database.database.reference.updateChildren(updates)
                                                .addOnSuccessListener {
                                                    loading.isDismiss()
                                                    pushNotifikasi()
                                                    Toast.makeText(this@LaporanActivity, "berhasil input laporan", Toast.LENGTH_SHORT).show()
                                                    goToPresensi()
                                                }
                                                .addOnFailureListener {
                                                    loading.isDismiss()
                                                    Toast.makeText(this@LaporanActivity, "gagal input laporan", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                        override fun onCancelled(databaseError: DatabaseError) {}
                                    })
                                }
                                override fun onCancelled(databaseError: DatabaseError) {}
                            })
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        } else {
            Toast.makeText(this, "data belum valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToPresensi() {
        Intent(this, PresensiActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(PresensiActivity.EXTRA_IDLESSISWA, intent.getStringExtra(EXTRA_IDLESSISWA))
            it.putExtra(PresensiActivity.EXTRA_NAMASISWA, intent.getStringExtra(EXTRA_NAMASISWA).toString().substringAfter("Siswa: "))
            it.putExtra(PresensiActivity.EXTRA_NAMALES, intent.getStringExtra(EXTRA_NAMALES).toString().substringAfter("Les: "))
            it.putExtra(PresensiActivity.EXTRA_JUMLAHPERTEMUAN, intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN).toString().substringAfter("Jumlah Pertemuan: "))
            startActivity(it)
        }
    }

    private fun pushNotifikasi() {
        val tutor = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/id_tutor")
        tutor.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshotTutor: DataSnapshot) {
                if (dataSnapshotTutor.exists()) {
                    val token = Database.database.getReference("user/${dataSnapshotTutor.value}/token")
                    token.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshotToken: DataSnapshot) {
                            if (dataSnapshotToken.exists()) {
                                PushNotification(
                                    NotificationData("Lihat Komentar Wali Murid", "Les ${intent.getStringExtra(EXTRA_NAMALES).toString().substringAfter("Les: ")} ${intent.getStringExtra(EXTRA_NAMASISWA).toString().substringAfter("Siswa: ")} ${binding.tvPertemuan.text.toString().substringAfter("Pertemuan ")}"),
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