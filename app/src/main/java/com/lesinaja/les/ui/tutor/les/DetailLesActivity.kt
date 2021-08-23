package com.lesinaja.les.ui.tutor.les

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.base.Database
import com.lesinaja.les.databinding.ActivityDetailLowonganBinding
import java.text.SimpleDateFormat

class DetailLesActivity : AppCompatActivity() {
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

        binding.tvJudul.text = "Detail Les"
        binding.btnKembali.setOnClickListener {
            goToLes()
        }

        listJadwal = arrayOf()

        binding.tvJadwal.text = ""

        updateUI()

        binding.btnAmbilLowongan.visibility = GONE
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
                val waliMurid = Database.database.getReference("user/${dataSnapshotSiswa.value}/kontak")
                waliMurid.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshotWaliMurid: DataSnapshot) {
                        var idDesa = dataSnapshotWaliMurid.child("id_desa").value.toString()
                        var idKecamatan = dataSnapshotWaliMurid.child("id_desa").value.toString().substring(0,7)
                        var idKabupaten = dataSnapshotWaliMurid.child("id_desa").value.toString().substring(0,4)
                        var idProvinsi = dataSnapshotWaliMurid.child("id_desa").value.toString().substring(0,2)

                        val desa = Database.database.getReference("wilayah_desa/${idProvinsi}/${idKabupaten}/${idKecamatan}/${idDesa}/nama")
                        desa.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshotDesa: DataSnapshot) {

                                val kecamatan = Database.database.getReference("wilayah_kecamatan/${idProvinsi}/${idKabupaten}/${idKecamatan}/nama")
                                kecamatan.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshotKecamatan: DataSnapshot) {

                                        val kabupaten = Database.database.getReference("wilayah_kabupaten/${idProvinsi}/${idKabupaten}/nama")
                                        kabupaten.addValueEventListener(object :
                                            ValueEventListener {
                                            override fun onDataChange(dataSnapshotKabupaten: DataSnapshot) {

                                                val provinsi = Database.database.getReference("wilayah_provinsi/${idProvinsi}/nama")
                                                provinsi.addValueEventListener(object :
                                                    ValueEventListener {
                                                    override fun onDataChange(dataSnapshotProvinsi: DataSnapshot) {
                                                        binding.tvAlamat.text = "${dataSnapshotWaliMurid.child("alamat_rumah").value}, ${dataSnapshotDesa.value}, ${dataSnapshotKecamatan.value}, ${dataSnapshotKabupaten.value}, ${dataSnapshotProvinsi.value}"
                                                        binding.tvTelepon.text = dataSnapshotWaliMurid.child("telepon").value.toString()
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {}
                                                })
                                            }

                                            override fun onCancelled(error: DatabaseError) {}
                                        })
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadDataPribadiWaliMurid() {
        val siswa = Database.database.getReference("siswa/${intent.getStringExtra(EXTRA_IDSISWA)}/id_walimurid")
        siswa.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotSiswa: DataSnapshot) {
                val nama = Database.database.getReference("user/${dataSnapshotSiswa.value}/nama")
                nama.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshotNama: DataSnapshot) {
                        binding.tvNamaWaliMurid.text = dataSnapshotNama.value.toString()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadJadwal() {
        val jadwal = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/waktu_mulai")
        jadwal.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (i in 0 until dataSnapshot.childrenCount) {
                    listJadwal = listJadwal.plus(dataSnapshot.child("${i}").value.toString().toLong())
                    binding.tvJadwal.text = binding.tvJadwal.text.toString()+ SimpleDateFormat("EEEE").format(dataSnapshot.child("${i}").value.toString().toLong())+", jam "+ SimpleDateFormat("hh:mm aaa").format(dataSnapshot.child("${i}").value.toString().toLong())+"\n"
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun goToLes() {
        Intent(this, LesTutorActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }
}