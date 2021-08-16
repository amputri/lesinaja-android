package com.lesinaja.les.ui.walimurid.siswa

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.lesinaja.les.R
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.DataSiswa
import com.lesinaja.les.base.walimurid.SiswaKey
import com.lesinaja.les.databinding.ActivitySiswaBinding
import com.lesinaja.les.databinding.FragmentFooterWaliMuridBinding
import com.lesinaja.les.databinding.ItemSiswaBinding
import com.squareup.picasso.Picasso

data class SiswaAdapter(val mCtx : Context, val layoutResId : Int, val siswaList : List<SiswaKey>)
    : ArrayAdapter<SiswaKey>(mCtx, layoutResId, siswaList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)

        val view: View = layoutInflater.inflate(layoutResId, null)

        val siswa = siswaList[position]

        val jenjang = Database.database.getReference("master_jenjangkelas/${siswa.id_jenjangkelas}/nama")
        jenjang.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                view.findViewById<TextView>(R.id.tvJenjangKelas).text = snapshot.value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        view.findViewById<TextView>(R.id.tvNamaSiswa).text = siswa.nama
        view.findViewById<TextView>(R.id.tvNamaSekolah).text = siswa.sekolah

        view.findViewById<ImageView>(R.id.btnUbah).setOnClickListener {
            goToUbahSiswa(siswa, view.findViewById<TextView>(R.id.tvJenjangKelas).text.toString())
        }

        view.findViewById<ImageView>(R.id.btnHapus).setOnClickListener {
            showDeleteDialog(siswa)
        }

        return view
    }

    private fun goToUbahSiswa(siswa: SiswaKey, jenjangKelas: String) {
        Intent(mCtx, UbahSiswaActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(UbahSiswaActivity.EXTRA_IDSISWA, siswa.id_siswa)
            it.putExtra(UbahSiswaActivity.EXTRA_IDJENJANG, siswa.id_jenjangkelas)
            it.putExtra(UbahSiswaActivity.EXTRA_JENJANG, jenjangKelas)
            it.putExtra(UbahSiswaActivity.EXTRA_NAMA, siswa.nama)
            it.putExtra(UbahSiswaActivity.EXTRA_SEKOLAH, siswa.sekolah)
            mCtx.startActivity(it)
        }
    }

    fun showDeleteDialog(siswa: SiswaKey) {
        val builder = AlertDialog.Builder(mCtx)
        builder.setMessage("yakin ingin menghapus ${siswa.nama}?")

        builder.setPositiveButton("Hapus") { p0,p1 ->
            val biaya = Database.database.getReference("pembayaran").orderByChild("id_pengirim").equalTo(siswa.id_siswa)
            biaya.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (h in dataSnapshot.children) {
                        if (h.child("id_penerima").value.toString() == "null") {
                            Database.database.getReference("pembayaran/${h.key}").removeValue()
                            Database.database.getReference("siswa/${siswa.id_siswa}").removeValue()
                            FirebaseStorage.getInstance().reference.child("bukti_daftar/${siswa.id_siswa}").delete()

                            Toast.makeText(mCtx, "data berhasil dihapus", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(mCtx, "tidak dapat menghapus siswa terverifikasi", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }

        builder.setNegativeButton("Batal") { p0,p1 ->
        }

        builder.show()
    }
}