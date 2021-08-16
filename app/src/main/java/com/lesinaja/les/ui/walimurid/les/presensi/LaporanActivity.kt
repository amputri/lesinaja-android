package com.lesinaja.les.ui.walimurid.les.presensi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.base.Database
import com.lesinaja.les.databinding.ActivityLaporanBinding
import com.lesinaja.les.ui.header.ToolbarFragment

class LaporanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaporanBinding

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
        binding = ActivityLaporanBinding.inflate(layoutInflater)
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
        binding.tvNamaTutor.text = "${intent.getStringExtra(EXTRA_NAMATUTOR)}"
        binding.btnTanggal.text = "${intent.getStringExtra(EXTRA_TANGGAL)} (${intent.getStringExtra(EXTRA_JAM).toString().substringBefore(" (").substringAfter("Jam ")})"
        binding.tvPertemuan.text = "${intent.getStringExtra(EXTRA_JAM).toString().substringAfter("(").substringBefore(")")}"

        loadDataLaporan()
    }

    private fun loadDataLaporan() {
        val laporan = Database.database.getReference("les_laporan/${intent.getStringExtra(EXTRA_IDLESSISWATUTOR)}/${intent.getStringExtra(EXTRA_IDPRESENSI)}")
        laporan.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("komentar_walimurid").exists()) {
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
        Database.database.getReference("les_laporan/${intent.getStringExtra(EXTRA_IDLESSISWATUTOR)}/${intent.getStringExtra(EXTRA_IDPRESENSI)}/komentar_walimurid").setValue(binding.etLaporanWaliMurid.text.toString())
        Database.database.getReference("les_laporan/${intent.getStringExtra(EXTRA_IDLESSISWATUTOR)}/${intent.getStringExtra(EXTRA_IDPRESENSI)}/rating_tutor").setValue(binding.ratingTutor.rating)
    }
}