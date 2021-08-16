package com.lesinaja.les.controller.walimurid.akun

import android.graphics.Bitmap
import com.google.firebase.storage.FirebaseStorage
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.Pendaftaran
import java.io.ByteArrayOutputStream

class PendaftaranController {
    fun uploadImage(imageBitmap: Bitmap, idSiswa: String, key: String, pendaftaran: Pendaftaran) {
        val ref = FirebaseStorage.getInstance().reference.child("bukti_daftar/${idSiswa}")

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

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

    fun uploadImageChange(imageBitmap: Bitmap, idSiswa: String, key: String) {
        val ref = FirebaseStorage.getInstance().reference.child("bukti_daftar/${idSiswa}")

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

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

    fun getNewKey(): String {
        return Database.database.getReference("pembayaran").push().key!!
    }

    fun changePendaftaran(pendaftaran: Pendaftaran, key: String) {
        Database.database.getReference("pembayaran/${key}").setValue(pendaftaran)
    }

    fun changePendaftaranUpdate(bukti: String, key: String) {
        Database.database.getReference("pembayaran/${key}/bukti").setValue(bukti)
        Database.database.getReference("pembayaran/${key}/waktu_transfer").setValue(getCurrentDateTime())
    }

    fun getCurrentDateTime(): Long {
        return System.currentTimeMillis()
    }
}