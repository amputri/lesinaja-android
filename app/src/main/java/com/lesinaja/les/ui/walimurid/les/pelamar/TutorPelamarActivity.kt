package com.lesinaja.les.ui.walimurid.les.pelamar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.R
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.HeaderLes
import com.lesinaja.les.databinding.ActivityTutorPelamarBinding
import com.lesinaja.les.ui.header.ToolbarFragment
import com.lesinaja.les.ui.walimurid.les.LesActivity

class TutorPelamarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorPelamarBinding
    private lateinit var idPelamarList : MutableList<HeaderLes>

    companion object {
        const val EXTRA_IDLESSISWA = "id_les_siswa"
        const val EXTRA_NAMASISWA = "nama_siswa"
        const val EXTRA_NAMALES = "nama_les"
        const val EXTRA_JUMLAHPERTEMUAN = "jumlah_pertemuan"
        const val EXTRA_TANGGALMULAI = "tanggal_mulai"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorPelamarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        idPelamarList = mutableListOf()

        binding.btnKembali.setOnClickListener {
            onBackPressed()
        }
        setToolbar("Daftar Tutor Pelamar")

        updateUI()

        setListView()
    }

    private fun setToolbar(judul: String) {
        val toolbarFragment = ToolbarFragment()
        val bundle = Bundle()

        bundle.putString("judul", judul)
        toolbarFragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(binding.header.id, toolbarFragment).commit()
    }

    private fun updateUI() {
        binding.tvNamaSiswa.text = "Siswa: ${intent.getStringExtra(EXTRA_NAMASISWA)}"
        binding.tvNamaLes.text = "Les: ${intent.getStringExtra(EXTRA_NAMALES)}"
        binding.tvJumlahPertemuan.text = "Jumlah Pertemuan: ${intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN)}"
        binding.tvTanggalMulai.text = "Tanggal Mulai: ${intent.getStringExtra(EXTRA_TANGGALMULAI)}"
    }

    private fun setListView() {
        val ref = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/id_tutorpelamar")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                idPelamarList.clear()
                if (snapshot.exists()) {
                    for (h in 0 until snapshot.childrenCount) {
                        idPelamarList.add(HeaderLes(
                            intent.getStringExtra(EXTRA_IDLESSISWA).toString(),
                            intent.getStringExtra(EXTRA_NAMASISWA).toString(),
                            intent.getStringExtra(EXTRA_NAMALES).toString(),
                            intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN).toString().toInt(),
                            intent.getStringExtra(EXTRA_TANGGALMULAI).toString(),
                            snapshot.child("${h}").value.toString()
                        ))
                    }

                    val adapter = TutorPelamarAdapter(this@TutorPelamarActivity, R.layout.item_tutor_pelamar, idPelamarList)
                    binding.lvTutorPelamar.adapter = adapter
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}