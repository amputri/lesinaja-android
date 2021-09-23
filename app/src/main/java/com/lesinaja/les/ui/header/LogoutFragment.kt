package com.lesinaja.les.ui.header

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.databinding.FragmentLogoutBinding
import com.lesinaja.les.ui.umum.akun.AkunUmumActivity

class LogoutFragment : Fragment() {
    private var _binding: FragmentLogoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLogoutBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnKeluar.setOnClickListener {
            signOut()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun goToAkunUmum() {
        Intent(activity, AkunUmumActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }

    private fun signOut() {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage("yakin ingin keluar ?")
        builder.setPositiveButton("Yakin") { p0,p1 ->
            Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid!!}/token").removeValue()
            Autentikasi.auth.signOut()

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
            googleSignInClient.signOut()

            goToAkunUmum()
        }
        builder.setNegativeButton("Batal") { p0,p1 -> }
        builder.show()
    }
}