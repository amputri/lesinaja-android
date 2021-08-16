package com.lesinaja.les.controller.walimurid.akun

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.umum.Wilayah
import com.lesinaja.les.base.walimurid.DataSiswa

class DataSiswaController {
    fun getNewKey(): String {
        return Database.database.getReference("siswa").push().key!!
    }

    fun changeDataSiswa(dataSiswa: DataSiswa, key: String) {
        Database.database.getReference("siswa/${key}").setValue(dataSiswa)
        Database.database.getReference("siswa/${key}/walimurid_status").setValue("${dataSiswa.id_walimurid}_daftar")
    }

    fun changeDataSiswaUpdate(dataSiswa: DataSiswa, key: String) {
        Database.database.getReference("siswa/${key}/id_jenjangkelas").setValue(dataSiswa.id_jenjangkelas)
        Database.database.getReference("siswa/${key}/id_walimurid").setValue(dataSiswa.id_walimurid)
        Database.database.getReference("siswa/${key}/nama").setValue(dataSiswa.nama)
        Database.database.getReference("siswa/${key}/sekolah").setValue(dataSiswa.sekolah)
    }

    fun getJenjang(idJenjang: String, namaJenjang: String): ArrayList<Wilayah> {
        val jenjang = ArrayList<Wilayah>()
        jenjang.add(Wilayah(idJenjang, namaJenjang))

        val jenjangKelas = Database.database.getReference("master_jenjangkelas")
        jenjangKelas.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (h in dataSnapshot.children) {
                    if (h.key != idJenjang) {
                        jenjang.add(Wilayah(h.key!!, h.child("nama").getValue() as String))
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        return jenjang
    }
}