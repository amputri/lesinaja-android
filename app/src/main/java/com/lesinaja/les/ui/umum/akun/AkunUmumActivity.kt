package com.lesinaja.les.ui.umum.akun

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.R
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.controller.umum.UserController
import com.lesinaja.les.databinding.ActivityAkunUmumBinding
import com.lesinaja.les.ui.tutor.akun.AkunTutorActivity
import com.lesinaja.les.ui.tutor.beranda.BerandaTutorActivity
import com.lesinaja.les.ui.walimurid.akun.AkunWaliMuridActivity
import com.lesinaja.les.ui.walimurid.beranda.BerandaWaliMuridActivity

class AkunUmumActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAkunUmumBinding

    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var role: String

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAkunUmumBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnMasukWaliMurid.setOnClickListener(this)

        binding.btnMasukTutor.setOnClickListener(this)
    }

    private fun goToBerandaWaliMurid() {
        Intent(this@AkunUmumActivity, BerandaWaliMuridActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }

    private fun goToBerandaTutor() {
        Intent(this@AkunUmumActivity, BerandaTutorActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }

    private fun goToAkunWaliMurid() {
        Intent(this@AkunUmumActivity, AkunWaliMuridActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }

    private fun goToAkunTutor() {
        Intent(this@AkunUmumActivity, AkunTutorActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(applicationContext, "gagal masuk dengan google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Autentikasi.auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if (role == "wali_murid") {
                        UserController().changeSession(Autentikasi.auth.currentUser?.uid!!, "wali_murid")
                        val profileWaliMurid = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/roles/wali_murid")
                        profileWaliMurid.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshotWaliMurid: DataSnapshot) {
                                if (dataSnapshotWaliMurid.getValue() != true) {
                                    goToAkunWaliMurid()
                                } else {
                                    goToBerandaWaliMurid()
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        })
                    } else {
                        UserController().changeSession(Autentikasi.auth.currentUser?.uid!!, "tutor")
                        val profileTutor = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/roles/tutor")
                        profileTutor.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshotTutor: DataSnapshot) {
                                if (dataSnapshotTutor.getValue() != true) {
                                    goToAkunTutor()
                                } else {
                                    goToBerandaTutor()
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        })
                    }
                } else {
                    Toast.makeText(applicationContext, "gagal mendapat kredensial", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onClick(p0: View?) {
        if (p0?.id == binding.btnMasukWaliMurid.id) {
            role = "wali_murid"
        } else {
            role = "tutor"
        }
        signIn()
    }
}