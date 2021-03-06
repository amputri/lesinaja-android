package com.lesinaja.les.ui.tutor.lowongan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.*
import com.lesinaja.les.R
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.LesKey
import com.lesinaja.les.databinding.ActivityLowonganBinding
import com.lesinaja.les.ui.tutor.beranda.BerandaTutorActivity
import com.lesinaja.les.ui.walimurid.les.pelamar.DetailTutorPelamarActivity

class LowonganActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLowonganBinding
    private lateinit var lesList : MutableList<LesKey>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLowonganBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setListView()
    }

    private fun setListView() {
        lesList = mutableListOf()

        val desa = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/kontak/id_desa")
        desa.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val gender = Database.database.getReference("user_role/tutor/${Autentikasi.auth.currentUser?.uid}/jenis_kelamin")
                    gender.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshotGender: DataSnapshot) {
                            if (dataSnapshotGender.exists()) {
                                lesList.clear()

                                val refBebas = Database.database.getReference("les_siswa").orderByChild("wilayah_preferensi").equalTo("${dataSnapshot.value.toString().substring(0,4)}_bebas")
                                refBebas.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshotBebas: DataSnapshot) {
                                        if (snapshotBebas.exists()) {
                                            for (h in snapshotBebas.children) {
                                                var waktuMulai: Array<Long> = arrayOf()

                                                val gantiTutor = Database.database.getReference("les_gantitutor/${h.key}/waktu_mulai")
                                                gantiTutor.addListenerForSingleValueEvent(object : ValueEventListener {
                                                    override fun onDataChange(snapshotGantiTutor: DataSnapshot) {
                                                        if (snapshotGantiTutor.exists()) {
                                                            val lesSiswa = Database.database.getReference("les_siswatutor")
                                                                .orderByChild("idlessiswa_idtutor")
                                                                .equalTo("${h.key}_${Autentikasi.auth.currentUser?.uid}")
                                                            lesSiswa.addListenerForSingleValueEvent(object : ValueEventListener {
                                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                                    if (!snapshot.exists()) {
                                                                        for (j in 0 until snapshotGantiTutor.childrenCount) {
                                                                            waktuMulai = waktuMulai.plus(snapshotGantiTutor.child("${j}").value.toString().toLong())
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

                                                                        val adapter = LowonganAdapter(this@LowonganActivity, R.layout.item_lowongan, lesList)
                                                                        binding.lvLowongan.adapter = adapter
                                                                    }
                                                                }
                                                                override fun onCancelled(error: DatabaseError) {}
                                                            })
                                                        } else {
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

                                                            val adapter = LowonganAdapter(this@LowonganActivity, R.layout.item_lowongan, lesList)
                                                            binding.lvLowongan.adapter = adapter
                                                        }
                                                    }
                                                    override fun onCancelled(error: DatabaseError) {}
                                                })
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {}
                                })

                                lateinit var ref: Query
                                if (dataSnapshotGender.value.toString() == "laki-laki") {
                                    ref = Database.database.getReference("les_siswa").orderByChild("wilayah_preferensi").equalTo("${dataSnapshot.value.toString().substring(0,4)}_laki-laki")
                                } else if (dataSnapshotGender.value.toString() == "perempuan") {
                                    ref = Database.database.getReference("les_siswa").orderByChild("wilayah_preferensi").equalTo("${dataSnapshot.value.toString().substring(0,4)}_perempuan")
                                }
                                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            for (h in snapshot.children) {
                                                var waktuMulai: Array<Long> = arrayOf()

                                                val gantiTutor = Database.database.getReference("les_gantitutor/${h.key}/waktu_mulai")
                                                gantiTutor.addListenerForSingleValueEvent(object : ValueEventListener {
                                                    override fun onDataChange(snapshotGantiTutor: DataSnapshot) {
                                                        if (snapshotGantiTutor.exists()) {
                                                            val lesSiswa = Database.database.getReference("les_siswatutor")
                                                                .orderByChild("idlessiswa_idtutor")
                                                                .equalTo("${h.key}_${Autentikasi.auth.currentUser?.uid}")
                                                            lesSiswa.addListenerForSingleValueEvent(object : ValueEventListener {
                                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                                    if (!snapshot.exists()) {
                                                                        for (j in 0 until snapshotGantiTutor.childrenCount) {
                                                                            waktuMulai = waktuMulai.plus(snapshotGantiTutor.child("${j}").value.toString().toLong())
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

                                                                        val adapter = LowonganAdapter(this@LowonganActivity, R.layout.item_lowongan, lesList)
                                                                        binding.lvLowongan.adapter = adapter
                                                                    }
                                                                }
                                                                override fun onCancelled(error: DatabaseError) {}
                                                            })
                                                        } else {
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

                                                            val adapter = LowonganAdapter(this@LowonganActivity, R.layout.item_lowongan, lesList)
                                                            binding.lvLowongan.adapter = adapter
                                                        }
                                                    }
                                                    override fun onCancelled(error: DatabaseError) {}
                                                })
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
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun goToBeranda() {
        Intent(this, BerandaTutorActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }

    override fun onBackPressed() {
        goToBeranda()
    }
}