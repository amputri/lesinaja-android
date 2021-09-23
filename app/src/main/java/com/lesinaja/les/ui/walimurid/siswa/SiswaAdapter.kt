package com.lesinaja.les.ui.walimurid.siswa

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.R
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.SiswaKey
import com.lesinaja.les.ui.header.LoadingDialog

data class SiswaAdapter(val mCtx : Context, val layoutResId : Int, val siswaList : List<SiswaKey>)
    : ArrayAdapter<SiswaKey>(mCtx, layoutResId, siswaList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)

        val view: View = layoutInflater.inflate(layoutResId, null)

        val siswa = siswaList[position]

        val jenjang = Database.database.getReference("master_jenjangkelas/${siswa.id_jenjangkelas}/nama")
        jenjang.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    view.findViewById<TextView>(R.id.tvJenjangKelas).text = snapshot.value.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
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
            if (!siswa.status_bayar) {
                val updates: MutableMap<String, Any?> = HashMap()
                updates["siswa/${siswa.id_siswa}"] = null
                updates["jumlah_data/siswa"] = ServerValue.increment(-1)
                updates["user/${Autentikasi.auth.currentUser?.uid}/roles/jumlah_siswa"] = ServerValue.increment(-1)

                lateinit var isdialog: AlertDialog
                val infalter = LayoutInflater.from(mCtx)
                val dialogView = infalter.inflate(R.layout.dialog_loading,null)

                val builder = AlertDialog.Builder(mCtx)
                builder.setView(dialogView)
                isdialog = builder.create()
                isdialog.show()
                isdialog.setCancelable(false)
                isdialog.setCanceledOnTouchOutside(false)

                Database.database.reference.updateChildren(updates)
                    .addOnSuccessListener {
                        isdialog.dismiss()
                        Toast.makeText(mCtx, "berhasil hapus siswa", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        isdialog.dismiss()
                        Toast.makeText(mCtx, "gagal hapus siswa", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(mCtx, "tidak dapat menghapus siswa terverifikasi", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal") { p0,p1 -> }
        builder.show()
    }
}