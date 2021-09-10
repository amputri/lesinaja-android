package com.lesinaja.les.ui.tutor.lowongan

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
import java.text.SimpleDateFormat

data class LowonganAdapter(val mCtx : Context, val layoutResId : Int, val lesList : List<LesKey>)
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
                                            val gantiTutor = Database.database.getReference("les_gantitutor/${les.id_lessiswa}/jumlah_pertemuan")
                                            gantiTutor.addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(snapshotGantiTutor: DataSnapshot) {
                                                    if (snapshotGantiTutor.exists()) {
                                                        view.findViewById<TextView>(R.id.tvJumlahPertemuan).text = "${snapshotGantiTutor.value} pertemuan"
                                                        view.findViewById<TextView>(R.id.tvNamaLes).text = "${dataSnapshotMapel.value} ${dataSnapshotJenjang.value}"
                                                    } else {
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
                                                override fun onCancelled(error: DatabaseError) {}
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

        view.findViewById<TextView>(R.id.tvTanggalMulai).text = "Mulai: "+ SimpleDateFormat("EEEE, dd MMMM yyyy").format(les.waktu_mulai[0])

        view.findViewById<ImageView>(R.id.btnDetailLowongan).setOnClickListener {
            val ref = Database.database.getReference("siswa/${les.id_siswa}/nama")
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshotSiswa: DataSnapshot) {
                    if (dataSnapshotSiswa.exists()) {
                        goToDetailLowongan(
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

        return view
    }

    private fun goToDetailLowongan(les: LesKey, namaLes: String, jumlahPertemuan: String, namaSiswa: String) {
        Intent(mCtx, DetailLowonganActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(DetailLowonganActivity.EXTRA_IDLESSISWA, les.id_lessiswa)
            it.putExtra(DetailLowonganActivity.EXTRA_GAJITUTOR, les.gaji_tutor.toString())
            it.putExtra(DetailLowonganActivity.EXTRA_IDSISWA, les.id_siswa)
            it.putExtra(DetailLowonganActivity.EXTRA_NAMASISWA, namaSiswa)
            it.putExtra(DetailLowonganActivity.EXTRA_NAMALES, namaLes)
            it.putExtra(DetailLowonganActivity.EXTRA_JUMLAHPERTEMUAN, jumlahPertemuan)
            it.putExtra(DetailLowonganActivity.EXTRA_TANGGALMULAI, SimpleDateFormat("EEEE, dd MMMM yyyy").format(les.waktu_mulai[0]))
            mCtx.startActivity(it)
        }
    }
}
