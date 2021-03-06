package com.lesinaja.les.ui.walimurid.siswa

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.R
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.SiswaKey
import com.lesinaja.les.databinding.ActivitySiswaBinding
import com.lesinaja.les.ui.walimurid.beranda.BerandaWaliMuridActivity

class SiswaActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySiswaBinding
    private lateinit var siswaList : MutableList<SiswaKey>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySiswaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTambahSiswa.setOnClickListener {
            goToTambahSiswa()
        }

        setListViewSiswa()
    }

    private fun goToTambahSiswa() {
        Intent(this, TambahSiswaActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }

    private fun setListViewSiswa() {
        siswaList = mutableListOf()

        val ref = Database.database.getReference("siswa").orderByChild("id_walimurid").equalTo(Autentikasi.auth.currentUser?.uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                siswaList.clear()
                if (snapshot.exists()) {
                    for (h in snapshot.children) {
                        val siswa = SiswaKey(
                            h.key!!,
                            h.child("id_jenjangkelas").value.toString(),
                            h.child("nama").value.toString(),
                            h.child("sekolah").value.toString(),
                            h.child("status_bayar").value.toString().toBoolean()
                        )
                        siswaList.add(siswa)
                    }

                    val adapter = SiswaAdapter(this@SiswaActivity, R.layout.item_siswa, siswaList)
                    binding.lvSiswa.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun goToBeranda() {
        Intent(this, BerandaWaliMuridActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }

    override fun onBackPressed() {
        goToBeranda()
    }
}