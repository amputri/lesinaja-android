package com.lesinaja.les.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.databinding.ActivityMainBinding
import com.lesinaja.les.ui.tutor.akun.AkunTutorActivity
import com.lesinaja.les.ui.tutor.beranda.BerandaTutorActivity
import com.lesinaja.les.ui.umum.akun.AkunUmumActivity
import com.lesinaja.les.ui.walimurid.akun.AkunWaliMuridActivity
import com.lesinaja.les.ui.walimurid.beranda.BerandaWaliMuridActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Autentikasi.auth.currentUser != null) {
            val ref = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/login_terakhir")
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.getValue().toString() == "wali_murid") {
                        val profileWaliMurid = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/roles/wali_murid")
                        profileWaliMurid.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshotWaliMurid: DataSnapshot) {
                                if (dataSnapshotWaliMurid.getValue() != true) {
                                    goToAkunWaliMurid()
                                } else {
                                    goToBerandaWaliMurid()
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                    } else if (dataSnapshot.getValue().toString() == "tutor") {
                        val profileTutor = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/roles/tutor")
                        profileTutor.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshotTutor: DataSnapshot) {
                                if (dataSnapshotTutor.getValue() != true) {
                                    goToAkunTutor()
                                } else {
                                    goToBerandaTutor()
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        } else {
            goToAkunUmum()
        }
    }

    private fun goToBerandaWaliMurid() {
        Intent(this, BerandaWaliMuridActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
            finish()
        }
    }

    private fun goToBerandaTutor() {
        Intent(this, BerandaTutorActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
            finish()
        }
    }

    private fun goToAkunWaliMurid() {
        Intent(this, AkunWaliMuridActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
            finish()
        }
    }

    private fun goToAkunTutor() {
        Intent(this, AkunTutorActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
            finish()
        }
    }

    private fun goToAkunUmum() {
        Intent(this, AkunUmumActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
            finish()
        }
    }
}