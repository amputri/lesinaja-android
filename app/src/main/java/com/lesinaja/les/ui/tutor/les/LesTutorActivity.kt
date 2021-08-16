package com.lesinaja.les.ui.tutor.les

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.R
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.LesKey
import com.lesinaja.les.databinding.ActivityLesTutorBinding
import com.lesinaja.les.ui.walimurid.les.LesAdapter

class LesTutorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLesTutorBinding
    private lateinit var lesList : MutableList<LesKey>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLesTutorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lesList = mutableListOf()

        setListView()
    }

    private fun setListView() {
        val ref = Database.database.getReference("les_siswatutor").orderByChild("id_tutor").equalTo(Autentikasi.auth.currentUser?.uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshotLes: DataSnapshot) {
                if (snapshotLes.exists()) {
                    lesList.clear()
                    for (i in snapshotLes.children) {
                        val lesTutor = Database.database.getReference("les_siswa/${i.child("id_lessiswa").value}")
                        lesTutor.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(h: DataSnapshot) {
                                if (h.exists()) {
                                    var waktuMulai: Array<Long> = arrayOf()
                                    for (j in 0 until h.child("waktu_mulai").childrenCount) {
                                        waktuMulai = waktuMulai.plus(h.child("waktu_mulai/${j}").value.toString().toLong())
                                    }
                                    val les = LesKey(
                                        h.key!!,
                                        h.child("gaji_tutor").value.toString().toInt(),
                                        h.child("id_les").value.toString(),
                                        h.child("id_siswa").value.toString(),
                                        h.child("preferensi_tutor").value.toString(),
                                        waktuMulai
                                    )
                                    if (les != null) {
                                        lesList.add(les)
                                    }

                                    val adapter = LesTutorAdapter(this@LesTutorActivity, R.layout.item_les, lesList)
                                    binding.lvLes.adapter = adapter
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}