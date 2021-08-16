package com.lesinaja.les.base

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Database {
    companion object {
        val database = Firebase.database
    }
}