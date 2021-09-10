package com.lesinaja.les.ui.walimurid.les.pelamar

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.VISIBLE
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.R
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.presensi.Presensi
import com.lesinaja.les.databinding.ActivityDetailTutorPelamarBinding
import com.lesinaja.les.ui.header.ToolbarFragment
import com.lesinaja.les.ui.tutor.les.presensi.PresensiTutorActivity
import com.lesinaja.les.ui.walimurid.les.BayarLesActivity
import com.lesinaja.les.ui.walimurid.les.LesActivity
import com.lesinaja.les.ui.walimurid.les.presensi.LaporanActivity
import com.lesinaja.les.ui.walimurid.les.presensi.PresensiActivity
import com.lesinaja.les.ui.walimurid.les.presensi.PresensiAdapter
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class DetailTutorPelamarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailTutorPelamarBinding

    var tanggalLama: Array<String> = arrayOf()
    var waktu: Array<String> = arrayOf()
    var tanggalBaru: Array<Long> = arrayOf()

    var tanggalGantiTutor: Array<Long> = arrayOf()
    var hariGantiTutor: Array<String> = arrayOf()
    var tanggalBelumLes: Array<String> = arrayOf()
    var preferensiTutor: Array<String> = arrayOf()

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

        binding.btnKembali.setOnClickListener {
            if (binding.btnPilihTutor.visibility == VISIBLE) {
                goToTutorPelamar()
            } else {
                goToLes()
            }
        }
        setToolbar("Detail Tutor Pelamar")

        updateUI()

        binding.tvLinkMicroteaching.setOnClickListener {
            openLink()
        }

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

    private fun openLink() {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse(binding.tvLinkMicroteaching.text.toString())
        startActivity(openURL)
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

                    val prefTutor = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/preferensi_tutor")
                    prefTutor.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshotPrefTutor: DataSnapshot) {
                            if (snapshotPrefTutor.exists()) {
                                preferensiTutor = preferensiTutor.plus("${idKabupaten}_${snapshotPrefTutor.value}")
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })

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
                                                                    binding.tvTelepon.text = dataSnapshotUser.child("kontak").child("telepon").value.toString()
                                                                    binding.tvNamaTutor.text = dataSnapshotUser.child("nama").value.toString()
                                                                    binding.tvAlamat.text = "${dataSnapshotUser.child("kontak").child("alamat_rumah").value}, ${dataSnapshotDesa.value}, ${dataSnapshotKecamatan.value}, ${dataSnapshotKabupaten.value}, ${dataSnapshotProvinsi.value}"

                                                                    val tutor = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/id_tutor")
                                                                    tutor.addListenerForSingleValueEvent(object : ValueEventListener {
                                                                        override fun onDataChange(dataSnapshotTutor: DataSnapshot) {
                                                                            if (dataSnapshotTutor.exists()) {
                                                                                if (dataSnapshotTutor.value == intent.getStringExtra(EXTRA_IDPELAMAR)) {
                                                                                    binding.btnPilihTutor.text = "Ganti Tutor"
                                                                                    setToolbar("Detail Tutor")
                                                                                }
                                                                            } else {
                                                                                binding.btnPilihTutor.visibility = VISIBLE
                                                                                setToolbar("Detail Tutor Pelamar")
                                                                            }

                                                                            val statusBayar = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/status_bayar")
                                                                            statusBayar.addListenerForSingleValueEvent(object : ValueEventListener {
                                                                                override fun onDataChange(dataSnapshotBayar: DataSnapshot) {
                                                                                    if (dataSnapshotBayar.exists()) {
                                                                                        if (dataSnapshotBayar.value.toString() != "true") {
                                                                                            binding.tvTelepon.text = "xxxxxxxx"
                                                                                            binding.tvAlamat.text = "${dataSnapshotDesa.value}, ${dataSnapshotKecamatan.value}, ${dataSnapshotKabupaten.value}, ${dataSnapshotProvinsi.value}"
                                                                                        }
                                                                                    }
                                                                                }
                                                                                override fun onCancelled(databaseError: DatabaseError) {}
                                                                            })
                                                                        }
                                                                        override fun onCancelled(databaseError: DatabaseError) {}
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

    private fun goToBayarLes() {
        Intent(this, BayarLesActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(BayarLesActivity.EXTRA_IDLESSISWA, intent.getStringExtra(EXTRA_IDLESSISWA))
            it.putExtra(BayarLesActivity.EXTRA_NAMASISWA, intent.getStringExtra(EXTRA_NAMASISWA))
            it.putExtra(BayarLesActivity.EXTRA_NAMALES, intent.getStringExtra(EXTRA_NAMALES))
            it.putExtra(BayarLesActivity.EXTRA_JUMLAHPERTEMUAN, intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN))
            it.putExtra(BayarLesActivity.EXTRA_NAMATUTOR, "ada tutor")
            startActivity(it)
        }
    }

    private fun addTutor() {
        val updates: MutableMap<String, Any> = HashMap()
        updates["les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/id_tutor"] = intent.getStringExtra(EXTRA_IDPELAMAR).toString()
        updates["les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/wilayah_preferensi"] = "les"
        Database.database.reference.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "berhasil pilih tutor", Toast.LENGTH_SHORT).show()
                goToBayarLes()
            }
            .addOnFailureListener {
                Toast.makeText(this, "gagal pilih tutor", Toast.LENGTH_SHORT).show()
                goToLes()
            }
    }

    private fun getListPresensi() {
        val ref = Database.database.getReference("les_siswatutor")
            .orderByChild("idlessiswa_idtutor")
            .equalTo("${intent.getStringExtra(EXTRA_IDLESSISWA)}_${intent.getStringExtra(EXTRA_IDPELAMAR)}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (h in snapshot.children) {
                        tanggalBelumLes = tanggalBelumLes.plus(h.key!!)
                        val presensi = Database.database.getReference("les_presensi/${h.key}")
                            .orderByChild("sudah_laporan")
                            .equalTo(false)
                        presensi.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshotPresensi: DataSnapshot) {
                                if (snapshotPresensi.exists()) {
                                    for (i in snapshotPresensi.children) {
                                        var hari = SimpleDateFormat("EEEE").format(i.child("waktu").value.toString().toLong())
                                        if (hari !in hariGantiTutor) {
                                            hariGantiTutor = hariGantiTutor.plus(hari)
                                            tanggalGantiTutor = tanggalGantiTutor.plus(i.child("waktu").value.toString().toLong())
                                        }
                                        tanggalBelumLes = tanggalBelumLes.plus(i.key!!)
                                    }

                                    changeTutor()
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

    private fun changeTutor() {
        val admin = Database.database.getReference("user").orderByChild("roles/admin").equalTo(true)
        admin.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshotAdmin: DataSnapshot) {
                if (dataSnapshotAdmin.exists()) {
                    for (h in dataSnapshotAdmin.children) {
                        val updates: MutableMap<String, Any?> = HashMap()

                        for (i in 1 until tanggalBelumLes.size) {
                            updates["les_presensi/${tanggalBelumLes[0]}/${tanggalBelumLes[i]}"] = null
                        }
                        updates["les_siswatutor/${tanggalBelumLes[0]}/jumlah_presensi"] = ServerValue.increment(-1 * (tanggalBelumLes.size.toDouble() - 1))

                        val keyPembayaran = Database.database.getReference("pembayaran").push().key!!
                        updates["jumlah_data/pembayaran"] = ServerValue.increment(1)
                        updates["pembayaran/${keyPembayaran}/idlessiswa"] = intent.getStringExtra(EXTRA_IDLESSISWA).toString()
                        updates["pembayaran/${keyPembayaran}/id_lessiswatutor"] = tanggalBelumLes[0]
                        updates["pembayaran/${keyPembayaran}/id_pengirim"] = h.key!!
                        updates["pembayaran/${keyPembayaran}/sudah_dikonfirmasi"] = false

                        updates["les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/id_tutor"] = null
                        updates["les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/id_tutorpelamar"] = null
                        updates["les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/wilayah_preferensi"] = preferensiTutor[0]

                        updates["les_gantitutor/${intent.getStringExtra(EXTRA_IDLESSISWA)}/jumlah_pertemuan"] = tanggalBelumLes.size-1
                        for (i in 0 until tanggalGantiTutor.size) {
                            updates["les_gantitutor/${intent.getStringExtra(EXTRA_IDLESSISWA)}/waktu_mulai/${i}"] = tanggalGantiTutor[i]
                        }

                        Database.database.reference.updateChildren(updates)
                            .addOnSuccessListener {
                                goToLes()
                                Toast.makeText(this@DetailTutorPelamarActivity, "berhasil ganti tutor, tunggu pelamar", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@DetailTutorPelamarActivity, "gagal ganti tutor", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun pilihTutor() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Apakah Anda Yakin ?")
        builder.setPositiveButton("Yakin") { p0,p1 ->
            if (binding.btnPilihTutor.text == "Ganti Tutor") getListPresensi()
            else addTutor()
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

    private fun goToTutorPelamar() {
        Intent(this, TutorPelamarActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(TutorPelamarActivity.EXTRA_IDLESSISWA, intent.getStringExtra(EXTRA_IDLESSISWA))
            it.putExtra(TutorPelamarActivity.EXTRA_NAMASISWA, intent.getStringExtra(EXTRA_NAMASISWA))
            it.putExtra(TutorPelamarActivity.EXTRA_NAMALES, intent.getStringExtra(EXTRA_NAMALES))
            it.putExtra(TutorPelamarActivity.EXTRA_JUMLAHPERTEMUAN, intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN))
            it.putExtra(TutorPelamarActivity.EXTRA_TANGGALMULAI, intent.getStringExtra(EXTRA_TANGGALMULAI))
            startActivity(it)
        }
    }
}