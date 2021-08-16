package com.lesinaja.les.base.umum

data class Wilayah(
    var id: String,
    var nama: String
){
    override fun toString(): String {
        return nama
    }
}
