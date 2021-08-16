package com.lesinaja.les.base

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Autentikasi {
    companion object {
        var auth = Firebase.auth
    }
}