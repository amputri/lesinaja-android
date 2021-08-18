package com.lesinaja.les.ui.tutor.les.presensi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.base.Database
import com.lesinaja.les.databinding.ActivityLaporanTutorBinding
import com.lesinaja.les.ui.header.ToolbarFragment
import com.lesinaja.les.ui.tutor.les.LesTutorActivity

class LaporanTutorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaporanTutorBinding

    companion object {
        const val EXTRA_IDLESSISWATUTOR = "id_les_siswa_tutor"
        const val EXTRA_IDPRESENSI = "id_presensi"
        const val EXTRA_NAMASISWA = "nama_siswa"
        const val EXTRA_NAMALES = "nama_les"
        const val EXTRA_JUMLAHPERTEMUAN = "jumlah_pertemuan"
        const val EXTRA_TANGGAL = "tanggal"
        const val EXTRA_JAM = "jam"
        const val EXTRA_NAMATUTOR = "nama_tutor"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanTutorBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        binding.btnTanggal.text = "${intent.getStringExtra(EXTRA_TANGGAL)} (${intent.getStringExtra(EXTRA_JAM).toString().substringBefore(" (").substringAfter("Jam ")})"
        binding.tvPertemuan.text = "${intent.getStringExtra(EXTRA_NAMATUTOR)}"

        loadDataLaporan()
    }

    private fun loadDataLaporan() {
        val laporan = Database.database.getReference("les_laporan/${intent.getStringExtra(EXTRA_IDLESSISWATUTOR)}/${intent.getStringExtra(EXTRA_IDPRESENSI)}")
        laporan.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("materi").exists()) {
                        binding.tvMateri.setText(dataSnapshot.child("materi").value.toString())
                        binding.tvLaporanTutor.setText(dataSnapshot.child("laporan_tutor").value.toString())
                    }
                    if (dataSnapshot.child("komentar_walimurid").exists()) {
                        binding.etLaporanWaliMurid.text = dataSnapshot.child("komentar_walimurid").value.toString()
                        binding.ratingTutor.rating = dataSnapshot.child("rating_tutor").value.toString().toFloat()
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun updateLaporan() {
        if (binding.tvMateri.text.toString().trim() != "" && binding.tvLaporanTutor.text.toString().trim() != "") {
            Database.database.getReference("les_laporan/${intent.getStringExtra(EXTRA_IDLESSISWATUTOR)}/${intent.getStringExtra(EXTRA_IDPRESENSI)}/materi").setValue(binding.tvMateri.text.toString().trim())
            Database.database.getReference("les_laporan/${intent.getStringExtra(EXTRA_IDLESSISWATUTOR)}/${intent.getStringExtra(EXTRA_IDPRESENSI)}/laporan_tutor").setValue(binding.tvLaporanTutor.text.toString().trim())
            Toast.makeText(this, "berhasil input laporan", Toast.LENGTH_SHORT).show()
            goToLes()
        } else {
            Toast.makeText(this, "data belum valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToLes() {
        Intent(this, LesTutorActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }
}