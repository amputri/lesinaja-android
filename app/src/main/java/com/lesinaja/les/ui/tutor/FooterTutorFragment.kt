package com.lesinaja.les.ui.tutor

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
import com.lesinaja.les.databinding.FragmentFooterTutorBinding
import com.lesinaja.les.ui.tutor.akun.AkunTutorActivity
import com.lesinaja.les.ui.tutor.beranda.BerandaTutorActivity
import com.lesinaja.les.ui.tutor.les.LesTutorActivity
import com.lesinaja.les.ui.tutor.lowongan.LowonganActivity

class FooterTutorFragment : Fragment() {
    private var _binding: FragmentFooterTutorBinding? = null
    private val binding get() = _binding!!

    private val halamanLowongan = arrayOf("LowonganActivity", "DetailLowonganActivity")
    private val halamanLes = arrayOf(
        "LesTutorActivity", "DetailLesActivity",
        "PresensiTutorActivity", "LaporanTutorActivity", "UbahJadwalTutorActivity"
    )

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFooterTutorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if (activity?.javaClass?.simpleName == "BerandaTutorActivity") {
            setVisibleBeranda()
        } else if (activity?.javaClass?.simpleName in halamanLowongan) {
            setVisibleLowongan()
        } else if (activity?.javaClass?.simpleName in halamanLes) {
            setVisibleLes()
        } else {
            setVisibleAkun()
        }

        binding.btnBeranda.setOnClickListener {
            goToBeranda()
        }

        binding.btnLowongan.setOnClickListener {
            goToLowongan()
        }

        binding.btnLes.setOnClickListener {
            goToLes()
        }

        binding.btnAkun.setOnClickListener {
            goToAkun()
        }

        return root
    }

    override fun onStart() {
        super.onStart()
        val ref = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/roles/tutor")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.getValue() != true && activity?.javaClass?.simpleName != "AkunTutorActivity") {
                    goToAkun()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun goToBeranda() {
        Intent(activity, BerandaTutorActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }

    private fun goToLowongan() {
        Intent(activity, LowonganActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }

    private fun goToLes() {
        Intent(activity, LesTutorActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }

    private fun goToAkun() {
        Intent(activity, AkunTutorActivity::class.java).also {
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
    private fun setVisibleLowongan() {
        binding.btnLowongan.compoundDrawableTintList = ColorStateList.valueOf(Color.WHITE)
        binding.btnLowongan.setTextColor(ColorStateList.valueOf(Color.WHITE))
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