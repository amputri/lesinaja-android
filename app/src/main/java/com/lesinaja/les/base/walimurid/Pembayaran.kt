package com.lesinaja.les.base.walimurid

data class Pembayaran(
    var bukti: String,
    var id_penerima: String,
    var id_pengirim: String,
    var id_siswa: String,
    var id_lessiswa: String,
    var biaya_daftar: Int,
    var biaya_les: Int,
    var waktu_transfer: Long,
    var sudah_dikonfirmasi: Boolean
)
