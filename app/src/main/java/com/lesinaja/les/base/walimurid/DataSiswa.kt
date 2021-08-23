package com.lesinaja.les.base.walimurid

data class DataSiswa(
    var id_jenjangkelas: String,
    var id_walimurid: String,
    var nama: String,
    var sekolah: String,
    var biaya_daftar: Int,
    var status_bayar: Boolean
)

