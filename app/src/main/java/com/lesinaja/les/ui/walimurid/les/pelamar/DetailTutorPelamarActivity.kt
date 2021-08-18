package com.lesinaja.les.ui.walimurid.les.pelamar

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.R
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.databinding.ActivityDetailTutorPelamarBinding
import com.lesinaja.les.ui.header.ToolbarFragment
import com.lesinaja.les.ui.tutor.lowongan.DetailLowonganActivity
import com.lesinaja.les.ui.walimurid.les.LesActivity
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class DetailTutorPelamarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailTutorPelamarBinding

    var tanggalLama: Array<String> = arrayOf()
    var waktu: Array<String> = arrayOf()
    var tanggalBaru: Array<Long> = arrayOf()

    companion object {
        const val EXTRA_IDLESSISWA = "id_les_siswa"
        const val EXTRA_NAMASISWA = "nama_siswa"
        const val EXTRA_NAMALES = "nama_les"
        const val EXTRA_JUMLAHPERTEMUAN = "jumlah_pertemuan"
        const val EXTRA_TANGGALMULAI = "tanggal_mulai"
        const val EXTRA_IDPELAMAR = "id_pelamar"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTutorPelamarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controlButton()

        updateUI()

        binding.btnPilihTutor.setOnClickListener {
            pilihTutor()
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
        binding.tvNamaSiswa.text = "Siswa: ${intent.getStringExtra(EXTRA_NAMASISWA)}"
        binding.tvNamaLes.text = "Les: ${intent.getStringExtra(EXTRA_NAMALES)}"
        binding.tvJumlahPertemuan.text = "Jumlah Pertemuan: ${intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN)}"
        binding.tvTanggalMulai.text = "Tanggal Mulai: ${intent.getStringExtra(EXTRA_TANGGALMULAI)}"

        loadUser()
        loadTutor()

        val jadwal = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/waktu_mulai")
        jadwal.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (h in 0 until dataSnapshot.childrenCount) {
                        tanggalLama = tanggalLama.plus(SimpleDateFormat("yyyy-MM-dd").format(dataSnapshot.child("${h}").value))
                        waktu = waktu.plus(SimpleDateFormat("hh:mm").format(dataSnapshot.child("${h}").value))
                        tanggalBaru = tanggalBaru.plus(dataSnapshot.child("${h}").value.toString().toLong())
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun loadUser() {
        val user = Database.database.getReference("user/${intent.getStringExtra(EXTRA_IDPELAMAR)}")
        user.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotUser: DataSnapshot) {
                if (dataSnapshotUser.exists()) {
                    var idDesa = dataSnapshotUser.child("kontak").child("id_desa").value.toString()
                    var idKecamatan = dataSnapshotUser.child("kontak").child("id_desa").value.toString().substring(0,7)
                    var idKabupaten = dataSnapshotUser.child("kontak").child("id_desa").value.toString().substring(0,4)
                    var idProvinsi = dataSnapshotUser.child("kontak").child("id_desa").value.toString().substring(0,2)

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
                                                                    binding.tvAlamat.text = "${dataSnapshotUser.child("kontak").child("alamat_rumah").value}, ${dataSnapshotDesa.value}, ${dataSnapshotKecamatan.value}, ${dataSnapshotKabupaten.value}, ${dataSnapshotProvinsi.value}"
                                                                    binding.tvTelepon.text = dataSnapshotUser.child("kontak").child("telepon").value.toString()
                                                                    binding.tvNamaTutor.text = dataSnapshotUser.child("nama").value.toString()
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

    private fun loadTutor() {
        val tutor = Database.database.getReference("user_role/tutor/${intent.getStringExtra(EXTRA_IDPELAMAR)}")
        tutor.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotTutor: DataSnapshot) {
                if (dataSnapshotTutor.exists()) {
                    binding.tvPerguruanTinggi.text = dataSnapshotTutor.child("perguruan_tinggi").value.toString()
                    binding.tvJurusan.text = dataSnapshotTutor.child("jurusan").value.toString()
                    binding.tvPengalamanMengajar.text = dataSnapshotTutor.child("pengalaman_mengajar").value.toString()
                    binding.tvLinkMicroteaching.text = dataSnapshotTutor.child("link_microteaching").value.toString()
                    Picasso.get().load(dataSnapshotTutor.child("link_foto").value.toString()).into(binding.ivFoto)


                    binding.tvMapelAhli.text = ""
                    for (i in 0 until dataSnapshotTutor.child("mapel_ahli").childrenCount) {
                        val mapel = Database.database.getReference("master_mapel/${dataSnapshotTutor.child("mapel_ahli").child("${i}").value}/nama")
                        mapel.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshotMapel: DataSnapshot) {
                                if (dataSnapshotMapel.exists()) {
                                    binding.tvMapelAhli.text = "${binding.tvMapelAhli.text}${dataSnapshotMapel.value}, "
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }

                    binding.tvJenjangAhli.text = ""
                    for (i in 0 until dataSnapshotTutor.child("jenjang_ahli").childrenCount) {
                        val jenjang = Database.database.getReference("master_jenjangkelas/${dataSnapshotTutor.child("jenjang_ahli").child("${i}").value}/nama")
                        jenjang.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshotJenjang: DataSnapshot) {
                                if (dataSnapshotJenjang.exists()) {
                                    binding.tvJenjangAhli.text = "${binding.tvJenjangAhli.text}${dataSnapshotJenjang.value}, "
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

    private fun controlButton() {
        val tutor = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/id_tutor")
        tutor.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotTutor: DataSnapshot) {
                if (dataSnapshotTutor.exists()) {
                    if (dataSnapshotTutor.value == intent.getStringExtra(EXTRA_IDPELAMAR)) {
                        binding.btnPilihTutor.visibility = INVISIBLE
                        setToolbar("Detail Tutor")
                    } else {
                        binding.btnPilihTutor.visibility = VISIBLE
                        setToolbar("Detail Tutor Pelamar")
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun addPresensi() {
        while (tanggalBaru.size < intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN).toString().toInt()) {
            for (i in 0..tanggalLama.size-1) {
                if (tanggalBaru.size < intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN).toString().toInt()) {
                    var c = Calendar.getInstance()
                    c.setTime(SimpleDateFormat("yyyy-MM-dd").parse(tanggalLama[i]))
                    c.add(Calendar.DAY_OF_MONTH, 7)

                    var temp = SimpleDateFormat("yyyy-MM-dd").format(c.getTime())

                    tanggalBaru = tanggalBaru.plus(SimpleDateFormat("yyyy-MM-dd HH:mm").parse("${temp} ${waktu[i]}").time)
                    tanggalLama[i] = "${temp}"
                }
            }
        }

        var keyLes = Database.database.getReference("les_siswatutor").push().key
        Database.database.getReference("les_siswatutor/${keyLes}/id_lessiswa").setValue(intent.getStringExtra(EXTRA_IDLESSISWA))
        Database.database.getReference("les_siswatutor/${keyLes}/id_tutor").setValue(intent.getStringExtra(EXTRA_IDPELAMAR))
        for (i in 0 until tanggalBaru.size) {
            var keyPresensi = Database.database.getReference("les_presensi/${keyLes}").push().key
            Database.database.getReference("les_presensi/${keyLes}/${keyPresensi}/waktu").setValue(tanggalBaru[i])
        }
    }

    private fun pilihTutor() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("yakin ingin memilih tutor ${binding.tvNamaTutor.text}?")
        builder.setPositiveButton("Pilih") { p0,p1 ->
            Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/id_tutor").setValue(intent.getStringExtra(EXTRA_IDPELAMAR))
            val ref = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/kontak/id_desa")
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    addPresensi()
                    Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/wilayah_status").setValue("${dataSnapshot.value.toString().substring(0,4)}_les")
                    goToLes()
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
        builder.setNegativeButton("Batal") { p0,p1 -> }
        builder.show()
    }

    private fun goToLes() {
        Intent(this, LesActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }
}