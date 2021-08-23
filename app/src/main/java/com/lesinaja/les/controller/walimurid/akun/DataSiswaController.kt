package com.lesinaja.les.controller.walimurid.akun

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.umum.Wilayah
import com.lesinaja.les.base.walimurid.DataSiswa

class DataSiswaController {
    fun getJenjang(idJenjang: String, namaJenjang: String): ArrayList<Wilayah> {
        val jenjang = ArrayList<Wilayah>()
        jenjang.add(Wilayah(idJenjang, namaJenjang))

        val jenjangKelas = Database.database.getReference("master_jenjangkelas")
        jenjangKelas.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (h in dataSnapshot.children) {
                        if (h.key != idJenjang) {
                            jenjang.add(Wilayah(h.key!!, h.child("nama").getValue() as String))
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        return jenjang
    }
}