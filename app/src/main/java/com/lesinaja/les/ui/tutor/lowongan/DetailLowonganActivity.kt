package com.lesinaja.les.ui.tutor.lowongan

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.databinding.ActivityDetailLowonganBinding
import com.lesinaja.les.ui.header.ToolbarFragment
import java.text.SimpleDateFormat

class DetailLowonganActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailLowonganBinding

    private lateinit var listJadwal: Array<Long>

    companion object {
        const val EXTRA_IDLESSISWA = "id_les_siswa"
        const val EXTRA_GAJITUTOR = "gaji_tutor"
        const val EXTRA_IDSISWA = "id_siswa"
        const val EXTRA_NAMASISWA = "nama_siswa"
        const val EXTRA_NAMALES = "nama_les"
        const val EXTRA_JUMLAHPERTEMUAN = "jumlah_pertemuan"
        const val EXTRA_TANGGALMULAI = "waktu_mulai"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLowonganBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setToolbar("Detail Lowongan")

        listJadwal = arrayOf()
        binding.tvJadwal.text = ""

        updateUI()
        getLastChild()

        binding.btnAmbilLowongan.setOnClickListener {
            ambilLowongan()
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
        binding.tvNamaLes.text = "${intent.getStringExtra(EXTRA_NAMALES)}"
        binding.tvNamaSiswa.text = "${intent.getStringExtra(EXTRA_NAMASISWA)}"
        binding.tvJumlahPertemuan.text = "${intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN)} Pertemuan"

        loadAlamat()
        loadDataPribadiWaliMurid()

        binding.tvGajiTutor.text = "${intent.getStringExtra(EXTRA_GAJITUTOR)}"

        loadJadwal()

        binding.tvTanggalMulai.text = "${intent.getStringExtra(EXTRA_TANGGALMULAI)}"
    }

    private fun loadAlamat() {
        val siswa = Database.database.getReference("siswa/${intent.getStringExtra(EXTRA_IDSISWA)}/id_walimurid")
        siswa.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotSiswa: DataSnapshot) {
                if (dataSnapshotSiswa.exists()) {
                    val waliMurid = Database.database.getReference("user/${dataSnapshotSiswa.value}/kontak")
                    waliMurid.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshotWaliMurid: DataSnapshot) {
                            if (dataSnapshotWaliMurid.exists()) {
                                var idDesa = dataSnapshotWaliMurid.child("id_desa").value.toString()
                                var idKecamatan = dataSnapshotWaliMurid.child("id_desa").value.toString().substring(0,7)
                                var idKabupaten = dataSnapshotWaliMurid.child("id_desa").value.toString().substring(0,4)
                                var idProvinsi = dataSnapshotWaliMurid.child("id_desa").value.toString().substring(0,2)

                                val desa = Database.database.getReference("wilayah_desa/${idProvinsi}/${idKabupaten}/${idKecamatan}/${idDesa}/nama")
                                desa.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshotDesa: DataSnapshot) {
                                        if (dataSnapshotDesa.exists()) {
                                            val kecamatan = Database.database.getReference("wilayah_kecamatan/${idProvinsi}/${idKabupaten}/${idKecamatan}/nama")
                                            kecamatan.addValueEventListener(object : ValueEventListener {
                                                override fun onDataChange(dataSnapshotKecamatan: DataSnapshot) {
                                                    if (dataSnapshotKecamatan.exists()) {
                                                        val kabupaten = Database.database.getReference("wilayah_kabupaten/${idProvinsi}/${idKabupaten}/nama")
                                                        kabupaten.addValueEventListener(object : ValueEventListener {
                                                            override fun onDataChange(dataSnapshotKabupaten: DataSnapshot) {
                                                                if (dataSnapshotKabupaten.exists()) {
                                                                    val provinsi = Database.database.getReference("wilayah_provinsi/${idProvinsi}/nama")
                                                                    provinsi.addValueEventListener(object : ValueEventListener {
                                                                        override fun onDataChange(dataSnapshotProvinsi: DataSnapshot) {
                                                                            if (dataSnapshotProvinsi.exists()) {
                                                                                binding.tvAlamat.text = "${dataSnapshotDesa.value}, ${dataSnapshotKecamatan.value}, ${dataSnapshotKabupaten.value}, ${dataSnapshotProvinsi.value}"
                                                                                binding.tvTelepon.text = "xxxxxxxx"
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

    private fun loadDataPribadiWaliMurid() {
        val siswa = Database.database.getReference("siswa/${intent.getStringExtra(EXTRA_IDSISWA)}/id_walimurid")
        siswa.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotSiswa: DataSnapshot) {
                if (dataSnapshotSiswa.exists()) {
                    val nama = Database.database.getReference("user/${dataSnapshotSiswa.value}/nama")
                    nama.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshotNama: DataSnapshot) {
                            if (dataSnapshotNama.exists()) {
                                binding.tvNamaWaliMurid.text = dataSnapshotNama.value.toString()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadJadwal() {
        val jadwal = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/waktu_mulai")
        jadwal.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (i in 0 until dataSnapshot.childrenCount) {
                        listJadwal = listJadwal.plus(dataSnapshot.child("${i}").value.toString().toLong())
                        binding.tvJadwal.text = binding.tvJadwal.text.toString()+ SimpleDateFormat("EEEE").format(dataSnapshot.child("${i}").value.toString().toLong())+", jam "+ SimpleDateFormat("hh:mm aaa").format(dataSnapshot.child("${i}").value.toString().toLong())+"\n"
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getLastChild() {
        val lesSiswa = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/id_tutorpelamar")
        lesSiswa.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotLesSiswa: DataSnapshot) {
                if (dataSnapshotLesSiswa.exists()) {
                    binding.tvStatus.text = "true_${dataSnapshotLesSiswa.childrenCount}"
                    for (h in 0 until dataSnapshotLesSiswa.childrenCount) {
                        if (dataSnapshotLesSiswa.child("${h}").value == Autentikasi.auth.currentUser?.uid) {
                            binding.tvStatus.text = "false"
                        }
                    }
                } else {
                    binding.tvStatus.text = "true_0"
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun ambilLowongan() {
        if (binding.tvStatus.text == "false") {
            Toast.makeText(applicationContext, "sudah apply lowongan, harap menunggu wali keputusan wali murid", Toast.LENGTH_SHORT).show()
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("yakin ingin ambil lowongan?")
            builder.setPositiveButton("Ambil") { p0,p1 ->
                Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/id_tutorpelamar/${binding.tvStatus.text.toString().substringAfter("_")}").setValue(Autentikasi.auth.currentUser?.uid)
                Toast.makeText(applicationContext, "berhasil ambil lowongan", Toast.LENGTH_SHORT).show()
                goToLowongan()
            }
            builder.setNegativeButton("Batal") { p0,p1 -> }
            builder.show()
        }
    }

    private fun goToLowongan() {
        Intent(this, LowonganActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }
}