package com.lesinaja.les.controller.umum

import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.umum.Kontak

class UserController {
    fun changeDisplayName(nama: String) {
        val profileUpdates = userProfileChangeRequest {
            displayName = nama
        }
        Autentikasi.auth.currentUser!!.updateProfile(profileUpdates)
    }

    fun changeContact(idUser: String, kontak: Kontak) {
        Database.database.getReference("user/${idUser}/kontak").setValue(kontak)
        Database.database.getReference("user/${idUser}/nama").setValue(Autentikasi.auth.currentUser?.displayName)
        Database.database.getReference("user/${idUser}/email").setValue(Autentikasi.auth.currentUser?.email)
    }

    fun changeSession(idUser: String, lastLogin: String) {
        Database.database.getReference("user/${idUser}/login_terakhir").setValue(lastLogin)
    }
}