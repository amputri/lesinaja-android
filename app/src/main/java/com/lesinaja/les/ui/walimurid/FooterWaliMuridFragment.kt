package com.lesinaja.les.ui.walimurid

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.databinding.FragmentFooterWaliMuridBinding
import com.lesinaja.les.ui.walimurid.akun.AkunWaliMuridActivity
import com.lesinaja.les.ui.walimurid.beranda.BerandaWaliMuridActivity
import com.lesinaja.les.ui.walimurid.les.LesActivity
import com.lesinaja.les.ui.walimurid.siswa.SiswaActivity

class FooterWaliMuridFragment : Fragment() {
    private var _binding: FragmentFooterWaliMuridBinding? = null
    private val binding get() = _binding!!

    private val halamanSiswa = arrayOf("SiswaActivity", "TambahSiswaActivity", "UbahSiswaActivity")
    private val halamanLes = arrayOf(
        "LesActivity", "TambahLesActivity", "BayarLesActivity",
        "TutorPelamarActivity", "DetailTutorPelamarActivity",
        "PresensiActivity", "LaporanActivity", "UbahJadwalActivity"
    )

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFooterWaliMuridBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if (activity?.javaClass?.simpleName == "BerandaWaliMuridActivity") {
            setVisibleBeranda()
        } else if (activity?.javaClass?.simpleName in halamanSiswa) {
            setVisibleSiswa()
        } else if (activity?.javaClass?.simpleName in halamanLes) {
            setVisibleLes()
        } else {
            setVisibleAkun()
        }

        binding.btnBeranda.setOnClickListener {
            goToBeranda()
        }

        binding.btnSiswa.setOnClickListener {
            goToSiswa()
        }

        binding.btnLes.setOnClickListener {
            goToLes()
        }

        binding.btnAkun.setOnClickListener {
            goToAkun()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        val ref = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/roles/wali_murid")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.getValue() != true && activity?.javaClass?.simpleName != "AkunWaliMuridActivity") {
                    goToAkun()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun goToBeranda() {
        Intent(activity, BerandaWaliMuridActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }

    private fun goToSiswa() {
        Intent(activity, SiswaActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }

    private fun goToLes() {
        Intent(activity, LesActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }

    private fun goToAkun() {
        Intent(activity, AkunWaliMuridActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setVisibleBeranda() {
        binding.btnBeranda.compoundDrawableTintList = ColorStateList.valueOf(Color.WHITE)
        binding.btnBeranda.setTextColor(ColorStateList.valueOf(Color.WHITE))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setVisibleSiswa() {
        binding.btnSiswa.compoundDrawableTintList = ColorStateList.valueOf(Color.WHITE)
        binding.btnSiswa.setTextColor(ColorStateList.valueOf(Color.WHITE))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setVisibleLes() {
        binding.btnLes.compoundDrawableTintList = ColorStateList.valueOf(Color.WHITE)
        binding.btnLes.setTextColor(ColorStateList.valueOf(Color.WHITE))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setVisibleAkun() {
        binding.btnAkun.compoundDrawableTintList = ColorStateList.valueOf(Color.WHITE)
        binding.btnAkun.setTextColor(ColorStateList.valueOf(Color.WHITE))
    }
}