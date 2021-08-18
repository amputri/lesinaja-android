package com.lesinaja.les.base.walimurid

data class Pembayaran(
    var bayar_lessiswa: String,
    var bukti: String,
    var id_penerima: String,
    var id_pengirim: String,
    var id_siswa: String,
    var nominal: Int,
    var waktu_transfer: Long,
    var sudah_dikonfirmasi: Boolean
)
