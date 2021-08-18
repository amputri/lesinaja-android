package com.lesinaja.les.controller.walimurid.akun

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.DataLes

class DataLesController {
    fun getNewKey(): String {
        return Database.database.getReference("les_siswa").push().key!!
    }

    fun changeDataLes(dataLes: DataLes, key: String, listJadwal: Array<Long>) {
        Database.database.getReference("les_siswa/${key}").setValue(dataLes)

        val ref = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/kontak/id_desa")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataLes.preferensi_tutor == "laki-laki") {
                    Database.database.getReference("les_siswa/${key}/wilayah_status").setValue("${dataSnapshot.value.toString().substring(0,4)}_l")
                } else if (dataLes.preferensi_tutor == "perempuan") {
                    Database.database.getReference("les_siswa/${key}/wilayah_status").setValue("${dataSnapshot.value.toString().substring(0,4)}_p")
                } else {
                    Database.database.getReference("les_siswa/${key}/wilayah_status").setValue("${dataSnapshot.value.toString().substring(0,4)}_b")
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        Database.database.getReference("les_siswa/${key}/waktu_mulai").removeValue()

        for (i in 0 until listJadwal.size) {
            Database.database.getReference("les_siswa/${key}/waktu_mulai/${i}").setValue(listJadwal[i])
        }
    }

    fun changeDataLesUpdate(dataLes: DataLes, key: String, listJadwal: Array<Long>) {
        Database.database.getReference("les_siswa/${key}/gaji_tutor").setValue(dataLes.gaji_tutor)
        Database.database.getReference("les_siswa/${key}/id_les").setValue(dataLes.id_les)
        Database.database.getReference("les_siswa/${key}/id_siswa").setValue(dataLes.id_siswa)
        Database.database.getReference("les_siswa/${key}/preferensi_tutor").setValue(dataLes.preferensi_tutor)

        val ref = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/kontak/id_desa")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataLes.preferensi_tutor == "laki-laki") {
                    Database.database.getReference("les_siswa/${key}/wilayah_status").setValue("${dataSnapshot.value.toString().substring(0,4)}_l")
                } else if (dataLes.preferensi_tutor == "perempuan") {
                    Database.database.getReference("les_siswa/${key}/wilayah_status").setValue("${dataSnapshot.value.toString().substring(0,4)}_p")
                } else {
                    Database.database.getReference("les_siswa/${key}/wilayah_status").setValue("${dataSnapshot.value.toString().substring(0,4)}_b")
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        Database.database.getReference("les_siswa/${key}/waktu_mulai").removeValue()

        for (i in 0 until listJadwal.size) {
            Database.database.getReference("les_siswa/${key}/waktu_mulai/${i}").setValue(listJadwal[i])
        }
    }
}