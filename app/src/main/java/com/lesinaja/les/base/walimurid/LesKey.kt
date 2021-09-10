package com.lesinaja.les.base.walimurid

data class LesKey(
    var id_lessiswa: String,
    var gaji_tutor: Int,
    var id_les: String,
    var id_siswa: String,
    var preferensi_tutor: String,
    var waktu_mulai: Array<Long>
)

