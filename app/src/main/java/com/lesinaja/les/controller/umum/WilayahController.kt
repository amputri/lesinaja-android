package com.lesinaja.les.controller.umum

import com.google.firebase.database.*
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.umum.Wilayah

class WilayahController {
    fun getProvinsiList(idProvinsiUser: String, namaProvinsiUser: String): ArrayList<Wilayah> {
        val provinsi = Database.database.getReference("wilayah_provinsi")
        return assignToObject(provinsi, idProvinsiUser, namaProvinsiUser)
    }

    fun getKabupatenList(_idProvinsi: String, idKabupatenUser: String, namaKabupatenUser: String): ArrayList<Wilayah> {
        val kabupaten = Database.database.getReference("wilayah_kabupaten/${_idProvinsi}")
        return assignToObject(kabupaten,idKabupatenUser, namaKabupatenUser)
    }

    fun getKecamatanList(_idKabupaten: String, idKecamatanUser: String, namaKecamatanUser: String): ArrayList<Wilayah> {
        val kecamatan = Database.database.getReference("wilayah_kecamatan/${_idKabupaten.substring(0,2)}/${_idKabupaten}")
        return assignToObject(kecamatan,idKecamatanUser, namaKecamatanUser)
    }

    fun getDesaList(_idKecamatan: String, idDesaUser: String, namaDesaUser: String): ArrayList<Wilayah> {
        val desa = Database.database.getReference("wilayah_desa/${_idKecamatan.substring(0,2)}/${_idKecamatan.substring(0,4)}/${_idKecamatan.substring(0,7)}")
        return assignToObject(desa,idDesaUser, namaDesaUser)
    }

    private fun assignToObject(ref: DatabaseReference, idWilayah: String, namaWilayahUser: String): ArrayList<Wilayah> {
        val wilayah = ArrayList<Wilayah>()
        wilayah.add(Wilayah(idWilayah, namaWilayahUser))

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (h in dataSnapshot.children) {
                    if (idWilayah != h.key) {
                        wilayah.add(Wilayah(h.key!!, h.child("nama").getValue() as String))
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        return wilayah
    }
}