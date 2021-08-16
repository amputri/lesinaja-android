package com.lesinaja.les.controller.walimurid.akun

import com.google.firebase.database.ServerValue
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.DataWaliMurid

class DataWaliMuridController {
    fun changeDataWaliMurid(idUser: String, dataWaliMurid: DataWaliMurid, referensiLama: String) {
        Database.database.getReference("user_role/wali_murid/${idUser}").setValue(dataWaliMurid)
        Database.database.getReference("user/${idUser}/roles/wali_murid").setValue(true)

        if (referensiLama != "") {
            Database.database.getReference("referensi_bimbel/${referensiLama}").setValue(ServerValue.increment(-1))
        }

        Database.database.getReference("referensi_bimbel/${dataWaliMurid.referensi_bimbel}").setValue(ServerValue.increment(1))

    }
}