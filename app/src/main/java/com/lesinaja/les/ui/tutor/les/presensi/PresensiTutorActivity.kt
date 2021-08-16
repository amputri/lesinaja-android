package com.lesinaja.les.ui.tutor.les.presensi

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.R
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.presensi.Presensi
import com.lesinaja.les.databinding.ActivityPresensiTutorBinding
import com.lesinaja.les.ui.header.ToolbarFragment
import com.lesinaja.les.ui.walimurid.les.presensi.PresensiAdapter

class PresensiTutorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPresensiTutorBinding
    private lateinit var presensiList : MutableList<Presensi>

    companion object {
        const val EXTRA_IDLESSISWA = "id_les_siswa"
        const val EXTRA_NAMASISWA = "nama_siswa"
        const val EXTRA_NAMALES = "nama_les"
        const val EXTRA_JUMLAHPERTEMUAN = "jumlah_pertemuan"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresensiTutorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presensiList = mutableListOf()

        setToolbar("Presensi Les")

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
    }

    private fun setListView() {
        val ref = Database.database.getReference("les_siswatutor").orderByChild("id_lessiswa").equalTo(intent.getStringExtra(EXTRA_IDLESSISWA))
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    presensiList.clear()
                    for (h in snapshot.children) {
                        if (h.child("id_tutor").value == Autentikasi.auth.currentUser?.uid) {
                            val presensi = Database.database.getReference("les_presensi/${h.key}")
                            presensi.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshotPresensi: DataSnapshot) {
                                    for (i in snapshotPresensi.children) {
                                        val presensiObject = Presensi(
                                            h.key!!,
                                            h.child("id_lessiswa").value.toString(),
                                            binding.tvNamaSiswa.text.toString(),
                                            binding.tvNamaLes.text.toString(),
                                            binding.tvJumlahPertemuan.text.toString(),
                                            h.child("id_tutor").value.toString(),
                                            i.key!!,
                                            i.child("waktu").value.toString().toLong()
                                        )
                                        if (presensiObject != null) {
                                            presensiList.add(presensiObject)
                                        }
                                    }
                                    binding.lvPresensi.adapter = PresensiTutorAdapter(this@PresensiTutorActivity, R.layout.item_presensi, presensiList)
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}