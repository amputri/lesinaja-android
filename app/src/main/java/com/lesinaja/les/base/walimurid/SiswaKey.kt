package com.lesinaja.les.base.walimurid

data class SiswaKey(
    var id_siswa: String,
    var id_jenjangkelas: String,
    var id_walimurid: String,
    var nama: String,
    var sekolah: String
) {
    constructor() : this("", "", "", "","")
}

