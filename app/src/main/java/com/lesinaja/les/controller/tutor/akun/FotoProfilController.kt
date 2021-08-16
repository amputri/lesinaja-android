package com.lesinaja.les.controller.tutor.akun

import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import java.io.ByteArrayOutputStream

class FotoProfilController {
    fun uploadImage(imageBitmap: Bitmap) {
        val ref = FirebaseStorage.getInstance().reference.child("foto_tutor/${Autentikasi.auth.currentUser?.uid}")

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

        ref.putBytes(image)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener {
                        it.result?.let {
                            changePhotoUrl(it)
                        }
                    }
                }
            }
    }

    fun changePhotoUrl(imageUri: Uri) {
        UserProfileChangeRequest.Builder()
            .setPhotoUri(imageUri)
            .build().also {
                Autentikasi.auth.currentUser?.updateProfile(it)
            }

        Database.database.getReference("user_role/tutor/${Autentikasi.auth.currentUser?.uid}/link_foto").setValue(imageUri.toString())
    }
}