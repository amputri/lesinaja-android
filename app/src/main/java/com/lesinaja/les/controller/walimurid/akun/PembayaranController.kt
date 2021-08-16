package com.lesinaja.les.controller.walimurid.akun

import android.graphics.Bitmap
import com.google.firebase.storage.FirebaseStorage
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.Pembayaran
import java.io.ByteArrayOutputStream

class PembayaranController {
    fun uploadImage(imageBitmap: Bitmap, idLes: String, key: String, pembayaran: Pembayaran) {
        val ref = FirebaseStorage.getInstance().reference.child("bukti_bayar/${idLes}")

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

        ref.putBytes(image)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener {
                        it.result?.let {
                            pembayaran.bukti = it.toString()
                            changePembayaran(pembayaran, key)
                        }
                    }
                }
            }
    }

    fun getNewKey(): String {
        return Database.database.getReference("pembayaran").push().key!!
    }

    fun changePembayaran(pembayaran: Pembayaran, key: String) {
        Database.database.getReference("pembayaran/${key}").setValue(pembayaran)
    }

    fun getCurrentDateTime(): Long {
        return System.currentTimeMillis()
    }
}