package com.lesinaja.les.base.walimurid

data class Pendaftaran(
    var bayar_pendaftaran: Boolean,
    var bukti: String,
    var id_penerima: String,
    var id_pengirim: String,
    var nominal: Int,
    var waktu_transfer: Long
)
