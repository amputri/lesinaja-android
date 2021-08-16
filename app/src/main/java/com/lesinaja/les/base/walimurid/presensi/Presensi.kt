package com.lesinaja.les.base.walimurid.presensi

data class Presensi(
    var id_lessiswatutor: String,
    var id_lessiswa: String,
    var nama_siswa: String,
    var nama_les: String,
    var jumlah_pertemuan: String,
    var id_tutor: String,
    var id_presensi: String,
    var waktu: Long
)

