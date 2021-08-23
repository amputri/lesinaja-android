package com.lesinaja.les.ui.walimurid.les

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.R
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.LesKey
import com.lesinaja.les.ui.walimurid.les.pelamar.DetailTutorPelamarActivity
import com.lesinaja.les.ui.walimurid.les.pelamar.TutorPelamarActivity
import com.lesinaja.les.ui.walimurid.les.presensi.PresensiActivity
import java.text.SimpleDateFormat
import java.util.*

data class LesAdapter(val mCtx : Context, val layoutResId : Int, val lesList : List<LesKey>)
    : ArrayAdapter<LesKey>(mCtx, layoutResId, lesList) {

    var tanggalLama: Array<String> = arrayOf()
    var waktu: Array<String> = arrayOf()
    var tanggalBaru: Array<Long> = arrayOf()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)

        val view: View = layoutInflater.inflate(layoutResId, null)

        val les = lesList[position]

        val masterLes = Database.database.getReference("master_les/${les.id_les}")
        masterLes.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotLes: DataSnapshot) {
                if (dataSnapshotLes.exists()) {
                    val mapel = Database.database.getReference("master_mapel/${dataSnapshotLes.child("mapel").value}/nama")
                    mapel.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshotMapel: DataSnapshot) {
                            if (dataSnapshotMapel.exists()) {
                                val jenjang = Database.database.getReference("master_jenjangkelas/${dataSnapshotLes.child("jenjangkelas").value}/nama")
                                jenjang.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshotJenjang: DataSnapshot) {
                                        if (dataSnapshotJenjang.exists()) {
                                            val paket = Database.database.getReference("master_paket/${dataSnapshotLes.child("paket").value}/jumlah_pertemuan")
                                            paket.addValueEventListener(object : ValueEventListener {
                                                override fun onDataChange(dataSnapshotPaket: DataSnapshot) {
                                                    if (dataSnapshotPaket.exists()) {
                                                        view.findViewById<TextView>(R.id.tvJumlahPertemuan).text = "${dataSnapshotPaket.value} pertemuan"
                                                        view.findViewById<TextView>(R.id.tvNamaLes).text = "${dataSnapshotMapel.value} ${dataSnapshotJenjang.value}"
                                                    }
                                                }
                                                override fun onCancelled(databaseError: DatabaseError) {}
                                            })
                                        }
                                    }
                                    override fun onCancelled(databaseError: DatabaseError) {}
                                })
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        val tutor = Database.database.getReference("les_siswa/${les.id_lessiswa}/id_tutor")
        tutor.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotTutor: DataSnapshot) {
                if (dataSnapshotTutor.exists()) {
                    val nama = Database.database.getReference("user/${dataSnapshotTutor.value}/nama")
                    nama.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshotNama: DataSnapshot) {
                            if (dataSnapshotNama.exists()) {
                                view.findViewById<TextView>(R.id.tvTutor).text = "Tutor: ${dataSnapshotNama.value}"
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        view.findViewById<TextView>(R.id.tvTanggalMulai).text = "Mulai: "+ SimpleDateFormat("EEEE, dd MMMM yyyy").format(les.waktu_mulai[0])

        view.findViewById<ImageView>(R.id.btnUbah).setOnClickListener {
            val ref = Database.database.getReference("siswa/${les.id_siswa}/nama")
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshotSiswa: DataSnapshot) {
                    if (dataSnapshotSiswa.exists()) {
                        goToUbahLes(
                            les.id_lessiswa,
                            view.findViewById<TextView>(R.id.tvNamaLes).text.toString(),
                            view.findViewById<TextView>(R.id.tvJumlahPertemuan).text.toString().substringBefore(" pertemuan"),
                            dataSnapshotSiswa.value.toString()
                        )
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

        view.findViewById<TextView>(R.id.tvTutor).setOnClickListener {
            val ref = Database.database.getReference("siswa/${les.id_siswa}/nama")
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshotSiswa: DataSnapshot) {
                    if (dataSnapshotSiswa.exists()) {
                        val tutor = Database.database.getReference("les_siswa/${les.id_lessiswa}/id_tutor")
                        tutor.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshotTutor: DataSnapshot) {
                                if (dataSnapshotTutor.exists()) {
                                    goToDetailTutorPelamar(
                                        les.id_lessiswa,
                                        view.findViewById<TextView>(R.id.tvNamaLes).text.toString(),
                                        view.findViewById<TextView>(R.id.tvJumlahPertemuan).text.toString().substringBefore(" pertemuan"),
                                        dataSnapshotSiswa.value.toString(),
                                        SimpleDateFormat("EEEE, dd MMMM yyyy").format(les.waktu_mulai[0]),
                                        dataSnapshotTutor.value.toString()
                                    )
                                } else {
                                    goToTutorPelamar(
                                        les.id_lessiswa,
                                        view.findViewById<TextView>(R.id.tvNamaLes).text.toString(),
                                        view.findViewById<TextView>(R.id.tvJumlahPertemuan).text.toString().substringBefore(" pertemuan"),
                                        dataSnapshotSiswa.value.toString(),
                                        SimpleDateFormat("EEEE, dd MMMM yyyy").format(les.waktu_mulai[0])
                                    )
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

        view.findViewById<ImageView>(R.id.btnPresensi).setOnClickListener {
            val ref = Database.database.getReference("siswa/${les.id_siswa}/nama")
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshotSiswa: DataSnapshot) {
                    if (dataSnapshotSiswa.exists()) {
                        val statusBayar = Database.database.getReference("les_siswa/${les.id_lessiswa}/status_bayar")
                        statusBayar.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshotStatusBayar: DataSnapshot) {
                                if (dataSnapshotStatusBayar.value == true) {
                                    val idTutor = Database.database.getReference("les_siswa/${les.id_lessiswa}/id_tutor")
                                    idTutor.addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(dataSnapshotIdTutor: DataSnapshot) {
                                            if (dataSnapshotIdTutor.exists()) {
                                                val cekPresensi = Database.database.getReference("les_siswatutor").orderByChild("id_lessiswa").equalTo(les.id_lessiswa)
                                                cekPresensi.addValueEventListener(object : ValueEventListener {
                                                    override fun onDataChange(dataSnapshotCekPresensi: DataSnapshot) {
                                                        if (dataSnapshotCekPresensi.exists() == false) {
                                                            addPresensi(
                                                                les,
                                                                dataSnapshotIdTutor.value.toString(),
                                                                view.findViewById<TextView>(R.id.tvJumlahPertemuan).text.toString().substringBefore(" pertemuan")
                                                            )
                                                            goToPresensi(
                                                                les.id_lessiswa,
                                                                view.findViewById<TextView>(R.id.tvNamaLes).text.toString(),
                                                                view.findViewById<TextView>(R.id.tvJumlahPertemuan).text.toString().substringBefore(" pertemuan"),
                                                                dataSnapshotSiswa.value.toString()
                                                            )
                                                        } else {
                                                            goToPresensi(
                                                                les.id_lessiswa,
                                                                view.findViewById<TextView>(R.id.tvNamaLes).text.toString(),
                                                                view.findViewById<TextView>(R.id.tvJumlahPertemuan).text.toString().substringBefore(" pertemuan"),
                                                                dataSnapshotSiswa.value.toString()
                                                            )
                                                        }
                                                    }
                                                    override fun onCancelled(databaseError: DatabaseError) {}
                                                })
                                            } else {
                                                Toast.makeText(mCtx, "Belum ada/pilih tutor", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        override fun onCancelled(databaseError: DatabaseError) {}
                                    })
                                } else {
                                    Toast.makeText(mCtx, "Belum bayar/verifikasi oleh admin", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

        return view
    }

    private fun addPresensi(les: LesKey, idTutor: String, jumlahPertemuan: String) {
        for (h in 0..les.waktu_mulai.size-1) {
            tanggalLama = tanggalLama.plus(SimpleDateFormat("yyyy-MM-dd").format(les.waktu_mulai[h]))
            waktu = waktu.plus(SimpleDateFormat("hh:mm").format(les.waktu_mulai[h]))
            tanggalBaru = tanggalBaru.plus(les.waktu_mulai[h].toString().toLong())
        }

        while (tanggalBaru.size < jumlahPertemuan.toInt()) {
            for (i in 0..tanggalLama.size-1) {
                if (tanggalBaru.size < jumlahPertemuan.toInt()) {
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
        Database.database.getReference("les_siswatutor/${keyLes}/id_lessiswa").setValue(les.id_lessiswa)
        Database.database.getReference("les_siswatutor/${keyLes}/id_tutor").setValue(idTutor)
        for (i in 0 until tanggalBaru.size) {
            var keyPresensi = Database.database.getReference("les_presensi/${keyLes}").push().key
            Database.database.getReference("les_presensi/${keyLes}/${keyPresensi}/waktu").setValue(tanggalBaru[i])
        }
    }

    private fun goToUbahLes(les: String, namaLes: String, jumlahPertemuan: String, namaSiswa: String) {
        Intent(mCtx, BayarLesActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(BayarLesActivity.EXTRA_IDLESSISWA, les)
            it.putExtra(BayarLesActivity.EXTRA_NAMASISWA, namaSiswa)
            it.putExtra(BayarLesActivity.EXTRA_NAMALES, namaLes)
            it.putExtra(BayarLesActivity.EXTRA_JUMLAHPERTEMUAN, jumlahPertemuan)
            mCtx.startActivity(it)
        }
    }

    private fun goToTutorPelamar(idLesSiswa: String, namaLes: String, jumlahPertemuan: String, namaSiswa: String, tanggalMulai: String) {
        Intent(mCtx, TutorPelamarActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(TutorPelamarActivity.EXTRA_IDLESSISWA, idLesSiswa)
            it.putExtra(TutorPelamarActivity.EXTRA_NAMASISWA, namaSiswa)
            it.putExtra(TutorPelamarActivity.EXTRA_NAMALES, namaLes)
            it.putExtra(TutorPelamarActivity.EXTRA_JUMLAHPERTEMUAN, jumlahPertemuan)
            it.putExtra(TutorPelamarActivity.EXTRA_TANGGALMULAI, tanggalMulai)
            mCtx.startActivity(it)
        }
    }

    private fun goToDetailTutorPelamar(idLesSiswa: String, namaLes: String, jumlahPertemuan: String, namaSiswa: String, tanggalMulai: String, idTutor: String) {
        Intent(mCtx, DetailTutorPelamarActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(DetailTutorPelamarActivity.EXTRA_IDLESSISWA, idLesSiswa)
            it.putExtra(DetailTutorPelamarActivity.EXTRA_NAMASISWA, namaSiswa)
            it.putExtra(DetailTutorPelamarActivity.EXTRA_NAMALES, namaLes)
            it.putExtra(DetailTutorPelamarActivity.EXTRA_JUMLAHPERTEMUAN, jumlahPertemuan)
            it.putExtra(DetailTutorPelamarActivity.EXTRA_TANGGALMULAI, tanggalMulai)
            it.putExtra(DetailTutorPelamarActivity.EXTRA_IDPELAMAR, idTutor)
            mCtx.startActivity(it)
        }
    }

    private fun goToPresensi(idLesSiswa: String, namaLes: String, jumlahPertemuan: String, namaSiswa: String) {
        Intent(mCtx, PresensiActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(PresensiActivity.EXTRA_IDLESSISWA, idLesSiswa)
            it.putExtra(PresensiActivity.EXTRA_NAMASISWA, namaSiswa)
            it.putExtra(PresensiActivity.EXTRA_NAMALES, namaLes)
            it.putExtra(PresensiActivity.EXTRA_JUMLAHPERTEMUAN, jumlahPertemuan)
            mCtx.startActivity(it)
        }
    }
}
