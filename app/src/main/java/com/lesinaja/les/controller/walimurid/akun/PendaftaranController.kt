package com.lesinaja.les.controller.walimurid.akun

import android.graphics.Bitmap
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.Pendaftaran
import java.io.ByteArrayOutputStream

class PendaftaranController {
    fun getNewKey(): String {
        return Database.database.getReference("pembayaran").push().key!!
    }

    fun getCurrentDateTime(): Long {
        return System.currentTimeMillis()
    }

    fun uploadImage(imageBitmap: Bitmap, idSiswa: String, key: String, pendaftaran: Pendaftaran) {
        var imageBit = imageBitmap

        if ((imageBitmap.getHeight() * imageBitmap.getWidth() / 360000) > 1) {
            val ratio = Math.sqrt(((imageBitmap.getHeight() * imageBitmap.getWidth() / 360000).toDouble()))
            imageBit = Bitmap.createScaledBitmap(
                imageBitmap,
                Math.round(imageBitmap.getWidth() / ratio).toInt(),
                Math.round(imageBitmap.getHeight() / ratio).toInt(),
                true
            )
        }

        val baos = ByteArrayOutputStream()
        imageBit.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

        val ref = FirebaseStorage.getInstance().reference.child("bukti_daftar/${idSiswa}")
        ref.putBytes(image)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener {
                        it.result?.let {
                            pendaftaran.bukti = it.toString()
                            changePendaftaran(pendaftaran, key)
                        }
                    }
                }
            }
    }

    fun changePendaftaran(pendaftaran: Pendaftaran, key: String) {
        Database.database.getReference("pembayaran/${key}").setValue(pendaftaran)

        val admin = Database.database.getReference("user").orderByChild("roles/admin").equalTo(true)
        admin.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotAdmin: DataSnapshot) {
                if (dataSnapshotAdmin.exists()) {
                    for (h in dataSnapshotAdmin.children) {
                        Database.database.getReference("pembayaran/${key}/id_penerima").setValue(h.key)
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun uploadImageChange(imageBitmap: Bitmap, idSiswa: String, key: String) {
        var imageBit = imageBitmap

        if ((imageBitmap.getHeight() * imageBitmap.getWidth() / 360000) > 1) {
            val ratio = Math.sqrt(((imageBitmap.getHeight() * imageBitmap.getWidth() / 360000).toDouble()))
            imageBit = Bitmap.createScaledBitmap(
                imageBitmap,
                Math.round(imageBitmap.getWidth() / ratio).toInt(),
                Math.round(imageBitmap.getHeight() / ratio).toInt(),
                true
            )
        }

        val baos = ByteArrayOutputStream()
        imageBit.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

        val ref = FirebaseStorage.getInstance().reference.child("bukti_daftar/${idSiswa}")
        ref.putBytes(image)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener {
                        it.result?.let {
                            changePendaftaranUpdate(it.toString(), key)
                        }
                    }
                }
            }
    }

    fun changePendaftaranUpdate(bukti: String, key: String) {
        Database.database.getReference("pembayaran/${key}/bukti").setValue(bukti)
        Database.database.getReference("pembayaran/${key}/waktu_transfer").setValue(getCurrentDateTime())
    }
}