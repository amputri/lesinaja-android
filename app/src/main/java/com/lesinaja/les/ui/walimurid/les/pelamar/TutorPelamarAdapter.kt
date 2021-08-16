package com.lesinaja.les.ui.walimurid.les.pelamar

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.R
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.HeaderLes

data class TutorPelamarAdapter(val mCtx : Context, val layoutResId : Int, val idPelamarList : List<HeaderLes>)
    : ArrayAdapter<HeaderLes>(mCtx, layoutResId, idPelamarList) {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)

        val view: View = layoutInflater.inflate(layoutResId, null)

        val pelamar = idPelamarList[position]

        val namaTutor = Database.database.getReference("user/${pelamar.id_transaksi}/nama")
        namaTutor.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotNamaTutor: DataSnapshot) {

                val perguruanTinggi = Database.database.getReference("user_role/tutor/${pelamar.id_transaksi}/perguruan_tinggi")
                perguruanTinggi.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshotPerguruanTinggi: DataSnapshot) {

                        val jurusan = Database.database.getReference("user_role/tutor/${pelamar.id_transaksi}/jurusan")
                        jurusan.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshotJurusan: DataSnapshot) {

                                view.findViewById<TextView>(R.id.tvNamaTutor).text = "${dataSnapshotNamaTutor.value}"
                                view.findViewById<TextView>(R.id.tvPerguruanTinggi).text = "${dataSnapshotPerguruanTinggi.value}"
                                view.findViewById<TextView>(R.id.tvJurusan).text = "${dataSnapshotJurusan.value}"
                            }
                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                    }
                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        view.findViewById<ImageView>(R.id.btnDetailTutor).setOnClickListener {
            goToDetailTutorPelamar(pelamar)
        }

        return view
    }

    private fun goToDetailTutorPelamar(headerLes: HeaderLes) {
        Intent(mCtx, DetailTutorPelamarActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(DetailTutorPelamarActivity.EXTRA_IDLESSISWA, headerLes.id_lessiswa)
            it.putExtra(DetailTutorPelamarActivity.EXTRA_NAMASISWA, headerLes.nama_siswa)
            it.putExtra(DetailTutorPelamarActivity.EXTRA_NAMALES, headerLes.nama_les)
            it.putExtra(DetailTutorPelamarActivity.EXTRA_JUMLAHPERTEMUAN, headerLes.jumlah_pertemuan.toString())
            it.putExtra(DetailTutorPelamarActivity.EXTRA_TANGGALMULAI, headerLes.tanggal_mulai)
            it.putExtra(DetailTutorPelamarActivity.EXTRA_IDPELAMAR, headerLes.id_transaksi)
            mCtx.startActivity(it)
        }
    }

}
