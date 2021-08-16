package com.lesinaja.les.ui.tutor.les.presensi

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.R
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.presensi.Presensi
import com.lesinaja.les.ui.walimurid.les.presensi.LaporanActivity
import com.lesinaja.les.ui.walimurid.les.presensi.UbahJadwalActivity
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

data class PresensiTutorAdapter(val mCtx : Context, val layoutResId : Int, val presensiList : List<Presensi>)
    : ArrayAdapter<Presensi>(mCtx, layoutResId, presensiList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)

        val view: View = layoutInflater.inflate(layoutResId, null)

        val presensi = presensiList[position]

        view.findViewById<TextView>(R.id.tvTanggal).text = SimpleDateFormat("EEEE, dd MMMM yyyy").format(presensi.waktu)
        view.findViewById<TextView>(R.id.tvJam).text = "Jam "+SimpleDateFormat("hh:mm aaa").format(presensi.waktu)

        view.findViewById<TextView>(R.id.tvTutor).text = "Pertemuan ke-"+(position+1)

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
        Intent(mCtx, LaporanTutorActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(LaporanTutorActivity.EXTRA_IDLESSISWATUTOR, presensi.id_lessiswatutor)
            it.putExtra(LaporanTutorActivity.EXTRA_IDPRESENSI, presensi.id_presensi)
            it.putExtra(LaporanTutorActivity.EXTRA_NAMASISWA, presensi.nama_siswa)
            it.putExtra(LaporanTutorActivity.EXTRA_NAMALES, presensi.nama_les)
            it.putExtra(LaporanTutorActivity.EXTRA_JUMLAHPERTEMUAN, presensi.jumlah_pertemuan)
            it.putExtra(LaporanTutorActivity.EXTRA_TANGGAL, tanggal)
            it.putExtra(LaporanTutorActivity.EXTRA_JAM, jam)
            it.putExtra(LaporanTutorActivity.EXTRA_NAMATUTOR, namaTutor)
            mCtx.startActivity(it)
        }
    }

    private fun goToUbahJadwal(presensi: Presensi, tanggal: String, jam: String, namaTutor: String) {
        Intent(mCtx, UbahJadwalTutorActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(UbahJadwalTutorActivity.EXTRA_IDLESSISWATUTOR, presensi.id_lessiswatutor)
            it.putExtra(UbahJadwalTutorActivity.EXTRA_IDPRESENSI, presensi.id_presensi)
            it.putExtra(UbahJadwalTutorActivity.EXTRA_NAMASISWA, presensi.nama_siswa)
            it.putExtra(UbahJadwalTutorActivity.EXTRA_NAMALES, presensi.nama_les)
            it.putExtra(UbahJadwalTutorActivity.EXTRA_JUMLAHPERTEMUAN, presensi.jumlah_pertemuan)
            it.putExtra(UbahJadwalTutorActivity.EXTRA_TANGGAL, tanggal)
            it.putExtra(UbahJadwalTutorActivity.EXTRA_JAM, jam)
            it.putExtra(UbahJadwalTutorActivity.EXTRA_NAMATUTOR, namaTutor)
            mCtx.startActivity(it)
        }
    }
}


