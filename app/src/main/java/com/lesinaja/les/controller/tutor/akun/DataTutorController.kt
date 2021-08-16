package com.lesinaja.les.controller.tutor.akun

import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.tutor.DataTutor

class DataTutorController {
    fun changeDataTutor(idUser: String, dataTutor: DataTutor, mapelAhli: Array<String>, jenjangAhli: Array<String>) {
        Database.database.getReference("user_role/tutor/${idUser}").setValue(dataTutor)
        Database.database.getReference("user_role/tutor/${idUser}/link_foto").setValue(Autentikasi.auth.currentUser?.photoUrl.toString())
        Database.database.getReference("user/${idUser}/roles/tutor").setValue(true)

        for (i in 0 until mapelAhli.size) {
            Database.database.getReference("user_role/tutor/${idUser}/mapel_ahli/${i}").setValue(mapelAhli[i])
        }

        for (i in 0 until jenjangAhli.size) {
            Database.database.getReference("user_role/tutor/${idUser}/jenjang_ahli/${i}").setValue(jenjangAhli[i])
        }
    }
}