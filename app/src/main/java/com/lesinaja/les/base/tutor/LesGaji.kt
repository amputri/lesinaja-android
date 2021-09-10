package com.lesinaja.les.base.tutor

data class LesGaji(
    var id_lessiswatutor: String,
    var id_lessiswa: String,
    var gaji_tutor: Int,
    var id_les: String,
    var id_siswa: String,
    var preferensi_tutor: String,
    var waktu_mulai: Array<Long>
)
