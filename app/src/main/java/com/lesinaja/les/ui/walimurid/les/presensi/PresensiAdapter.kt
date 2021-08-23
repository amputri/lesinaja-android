package com.lesinaja.les.ui.walimurid.les.presensi

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.R
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.presensi.Presensi
import java.text.SimpleDateFormat

data class PresensiAdapter(val mCtx : Context, val layoutResId : Int, val presensiList : List<Presensi>)
    : ArrayAdapter<Presensi>(mCtx, layoutResId, presensiList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)

        val view: View = layoutInflater.inflate(layoutResId, null)

        val presensi = presensiList[position]

        view.findViewById<TextView>(R.id.tvTanggal).text = SimpleDateFormat("EEEE, dd MMMM yyyy").format(presensi.waktu)
        view.findViewById<TextView>(R.id.tvJam).text = "Jam "+SimpleDateFormat("hh:mm aaa").format(presensi.waktu)+" (Pertemuan ke-"+(position+1)+")"

        val tutor = Database.database.getReference("les_siswa/${presensi.id_lessiswa}/id_tutor")
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

        view.findViewById<TextView>(R.id.btnLaporan).setOnClickListener {
            goToLaporan(
                presensi,
                view.findViewById<TextView>(R.id.tvTanggal).text.toString(),
                view.findViewById<TextView>(R.id.tvJam).text.toString(),
                view.findViewById<TextView>(R.id.tvTutor).text.toString()
            )
        }

        view.findViewById<TextView>(R.id.btnUbahJadwal).setOnClickListener {
            goToUbahJadwal(
                presensi,
                view.findViewById<TextView>(R.id.tvTanggal).text.toString(),
                view.findViewById<TextView>(R.id.tvJam).text.toString(),
                view.findViewById<TextView>(R.id.tvTutor).text.toString()
            )
        }

        return view
    }

    private fun goToLaporan(presensi: Presensi, tanggal: String, jam: String, namaTutor: String) {
        Intent(mCtx, LaporanActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(UbahJadwalActivity.EXTRA_IDLESSISWA, presensi.id_lessiswa)
            it.putExtra(LaporanActivity.EXTRA_IDLESSISWATUTOR, presensi.id_lessiswatutor)
            it.putExtra(LaporanActivity.EXTRA_IDPRESENSI, presensi.id_presensi)
            it.putExtra(LaporanActivity.EXTRA_NAMASISWA, presensi.nama_siswa)
            it.putExtra(LaporanActivity.EXTRA_NAMALES, presensi.nama_les)
            it.putExtra(LaporanActivity.EXTRA_JUMLAHPERTEMUAN, presensi.jumlah_pertemuan)
            it.putExtra(LaporanActivity.EXTRA_TANGGAL, tanggal)
            it.putExtra(LaporanActivity.EXTRA_JAM, jam)
            it.putExtra(LaporanActivity.EXTRA_NAMATUTOR, namaTutor)
            mCtx.startActivity(it)
        }
    }

    private fun goToUbahJadwal(presensi: Presensi, tanggal: String, jam: String, namaTutor: String) {
        Intent(mCtx, UbahJadwalActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(UbahJadwalActivity.EXTRA_IDLESSISWA, presensi.id_lessiswa)
            it.putExtra(UbahJadwalActivity.EXTRA_IDLESSISWATUTOR, presensi.id_lessiswatutor)
            it.putExtra(UbahJadwalActivity.EXTRA_IDPRESENSI, presensi.id_presensi)
            it.putExtra(UbahJadwalActivity.EXTRA_NAMASISWA, presensi.nama_siswa)
            it.putExtra(UbahJadwalActivity.EXTRA_NAMALES, presensi.nama_les)
            it.putExtra(UbahJadwalActivity.EXTRA_JUMLAHPERTEMUAN, presensi.jumlah_pertemuan)
            it.putExtra(UbahJadwalActivity.EXTRA_TANGGAL, tanggal)
            it.putExtra(UbahJadwalActivity.EXTRA_JAM, jam)
            it.putExtra(UbahJadwalActivity.EXTRA_NAMATUTOR, namaTutor)
            mCtx.startActivity(it)
        }
    }
}

