package com.lesinaja.les.ui.tutor.les

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.R
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.tutor.LesGaji
import com.lesinaja.les.ui.tutor.les.presensi.PresensiTutorActivity
import java.text.SimpleDateFormat

data class LesTutorAdapter(val mCtx : Context, val layoutResId : Int, val lesList : List<LesGaji>)
    : ArrayAdapter<LesGaji>(mCtx, layoutResId, lesList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)

        val view: View = layoutInflater.inflate(layoutResId, null)

        val les = lesList[position]

        val masterLes = Database.database.getReference("master_les/${les.id_les}")
        masterLes.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotLes: DataSnapshot) {
                val mapel = Database.database.getReference("master_mapel/${dataSnapshotLes.child("mapel").value}/nama")
                mapel.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshotMapel: DataSnapshot) {
                        val jenjang = Database.database.getReference("master_jenjangkelas/${dataSnapshotLes.child("jenjangkelas").value}/nama")
                        jenjang.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshotJenjang: DataSnapshot) {
                                view.findViewById<TextView>(R.id.tvJumlahPertemuan).text = "${les.preferensi_tutor} pertemuan"
                                view.findViewById<TextView>(R.id.tvNamaLes).text = "${dataSnapshotMapel.value} ${dataSnapshotJenjang.value}"
                            }
                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                    }
                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        val siswa = Database.database.getReference("les_siswa/${les.id_lessiswa}/id_siswa")
        siswa.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotSiswa: DataSnapshot) {
                if (dataSnapshotSiswa.exists()) {
                    val nama = Database.database.getReference("siswa/${dataSnapshotSiswa.value}/nama")
                    nama.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshotNama: DataSnapshot) {
                            if (dataSnapshotNama.exists()) {
                                view.findViewById<Button>(R.id.tvTutor).text = "Siswa: ${dataSnapshotNama.value}"
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        view.findViewById<TextView>(R.id.tvTanggalMulai).text = "Mulai: "+SimpleDateFormat("EEEE, dd MMMM yyyy").format(les.waktu_mulai[0])

        view.findViewById<Button>(R.id.tvTutor).setOnClickListener {
            val ref = Database.database.getReference("siswa/${les.id_siswa}/nama")
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshotSiswa: DataSnapshot) {
                    goToDetailLes(
                        les,
                        view.findViewById<TextView>(R.id.tvNamaLes).text.toString(),
                        view.findViewById<TextView>(R.id.tvJumlahPertemuan).text.toString().substringBefore(" pertemuan"),
                        dataSnapshotSiswa.value.toString()
                    )
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

        view.findViewById<TextView>(R.id.tvpembayaranjudul).text = "Gaji"
        view.findViewById<ImageView>(R.id.btnUbah).setOnClickListener {
            val ref = Database.database.getReference("siswa/${les.id_siswa}/nama")
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshotSiswa: DataSnapshot) {
                    goToGaji(
                        les,
                        view.findViewById<TextView>(R.id.tvNamaLes).text.toString(),
                        view.findViewById<TextView>(R.id.tvJumlahPertemuan).text.toString().substringBefore(" pertemuan"),
                        dataSnapshotSiswa.value.toString()
                    )
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

        view.findViewById<ImageView>(R.id.btnPresensi).setOnClickListener {
            val ref = Database.database.getReference("siswa/${les.id_siswa}/nama")
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshotSiswa: DataSnapshot) {
                    goToPresensi(
                        les.id_lessiswa,
                        view.findViewById<TextView>(R.id.tvNamaLes).text.toString(),
                        view.findViewById<TextView>(R.id.tvJumlahPertemuan).text.toString().substringBefore(" pertemuan"),
                        dataSnapshotSiswa.value.toString()
                    )
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

        return view
    }

    private fun goToDetailLes(les: LesGaji, namaLes: String, jumlahPertemuan: String, namaSiswa: String) {
        Intent(mCtx, DetailLesActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(DetailLesActivity.EXTRA_IDLESSISWA, les.id_lessiswa)
            it.putExtra(DetailLesActivity.EXTRA_GAJITUTOR, les.gaji_tutor.toString())
            it.putExtra(DetailLesActivity.EXTRA_IDSISWA, les.id_siswa)
            it.putExtra(DetailLesActivity.EXTRA_NAMASISWA, namaSiswa)
            it.putExtra(DetailLesActivity.EXTRA_NAMALES, namaLes)
            it.putExtra(DetailLesActivity.EXTRA_JUMLAHPERTEMUAN, jumlahPertemuan)
            it.putExtra(DetailLesActivity.EXTRA_TANGGALMULAI, SimpleDateFormat("EEEE, dd MMMM yyyy").format(les.waktu_mulai[0]))
            mCtx.startActivity(it)
        }
    }

    private fun goToPresensi(idLesSiswa: String, namaLes: String, jumlahPertemuan: String, namaSiswa: String) {
        Intent(mCtx, PresensiTutorActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(PresensiTutorActivity.EXTRA_IDLESSISWA, idLesSiswa)
            it.putExtra(PresensiTutorActivity.EXTRA_NAMASISWA, namaSiswa)
            it.putExtra(PresensiTutorActivity.EXTRA_NAMALES, namaLes)
            it.putExtra(PresensiTutorActivity.EXTRA_JUMLAHPERTEMUAN, jumlahPertemuan)
            mCtx.startActivity(it)
        }
    }

    private fun goToGaji(les: LesGaji, namaLes: String, jumlahPertemuan: String, namaSiswa: String) {
        Intent(mCtx, GajiTutorActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(GajiTutorActivity.EXTRA_IDLESSISWATUTOR, les.id_lessiswatutor)
            it.putExtra(GajiTutorActivity.EXTRA_NAMASISWA, namaSiswa)
            it.putExtra(GajiTutorActivity.EXTRA_NAMALES, namaLes)
            it.putExtra(GajiTutorActivity.EXTRA_JUMLAHPERTEMUAN, jumlahPertemuan)
            it.putExtra(GajiTutorActivity.EXTRA_GAJITUTOR, les.gaji_tutor.toString())
            it.putExtra(GajiTutorActivity.EXTRA_TANGGALMULAI, SimpleDateFormat("EEEE, dd MMMM yyyy").format(les.waktu_mulai[0]))
            mCtx.startActivity(it)
        }
    }
}

