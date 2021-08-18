package com.lesinaja.les.ui.walimurid.les

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
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

data class LesAdapter(val mCtx : Context, val layoutResId : Int, val lesList : List<LesKey>)
    : ArrayAdapter<LesKey>(mCtx, layoutResId, lesList) {

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
                            les,
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
        }

        return view
    }

    private fun goToUbahLes(les: LesKey, namaLes: String, jumlahPertemuan: String, namaSiswa: String) {
        Intent(mCtx, UbahLesActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(UbahLesActivity.EXTRA_IDLESSISWA, les.id_lessiswa)
            it.putExtra(UbahLesActivity.EXTRA_GAJITUTOR, les.gaji_tutor.toString())
            it.putExtra(UbahLesActivity.EXTRA_IDLES, les.id_les)
            it.putExtra(UbahLesActivity.EXTRA_IDSISWA, les.id_siswa)
            it.putExtra(UbahLesActivity.EXTRA_PREFERENSITUTOR, les.preferensi_tutor)
            it.putExtra(UbahLesActivity.EXTRA_NAMASISWA, namaSiswa)
            it.putExtra(UbahLesActivity.EXTRA_NAMALES, namaLes)
            it.putExtra(UbahLesActivity.EXTRA_JUMLAHPERTEMUAN, jumlahPertemuan)
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
