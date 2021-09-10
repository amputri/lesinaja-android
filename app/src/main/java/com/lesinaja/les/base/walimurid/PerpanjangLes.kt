package com.lesinaja.les.base.walimurid

data class PerpanjangLes(
    var gaji_tutor: Int,
    var id_les: String,
    var id_siswa: String,
    var preferensi_tutor: String,
    var wilayah_preferensi: String,
    var biaya_les: Int,
    var status_bayar: Boolean,
    var id_tutor: Any?
)
