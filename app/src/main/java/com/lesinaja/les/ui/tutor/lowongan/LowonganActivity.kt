package com.lesinaja.les.ui.tutor.lowongan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.R
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.umum.Wilayah
import com.lesinaja.les.base.walimurid.LesKey
import com.lesinaja.les.controller.umum.WilayahController
import com.lesinaja.les.databinding.ActivityLowonganBinding
import com.lesinaja.les.ui.walimurid.les.LesAdapter

class LowonganActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLowonganBinding
    private lateinit var lesList : MutableList<LesKey>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLowonganBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        lesList = mutableListOf()

        val desa = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/kontak/id_desa")
        desa.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val gender = Database.database.getReference("user_role/tutor/${Autentikasi.auth.currentUser?.uid}/jenis_kelamin")
                gender.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshotGender: DataSnapshot) {

                        val ref = Database.database.getReference("les_siswa").orderByChild("wilayah_status").equalTo("${dataSnapshot.value.toString().substring(0,7)}_apply")
                        ref.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    lesList.clear()
                                    for (h in snapshot.children) {

                                        if (dataSnapshotGender.value.toString() == h.child("preferensi_tutor").value.toString() || h.child("preferensi_tutor").value.toString() == "bebas") {
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
                                        }
                                    }

                                    val adapter = LowonganAdapter(this@LowonganActivity, R.layout.item_lowongan, lesList)
                                    binding.lvLowongan.adapter = adapter

                                }

                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}