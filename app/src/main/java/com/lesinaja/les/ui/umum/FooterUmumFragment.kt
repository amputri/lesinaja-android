package com.lesinaja.les.ui.umum

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
import com.lesinaja.les.databinding.FragmentFooterUmumBinding
import com.lesinaja.les.ui.tutor.akun.AkunTutorActivity
import com.lesinaja.les.ui.tutor.beranda.BerandaTutorActivity
import com.lesinaja.les.ui.umum.akun.AkunUmumActivity
import com.lesinaja.les.ui.umum.beranda.BerandaUmumActivity
import com.lesinaja.les.ui.walimurid.akun.AkunWaliMuridActivity
import com.lesinaja.les.ui.walimurid.beranda.BerandaWaliMuridActivity

class FooterUmumFragment : Fragment() {
  private var _binding: FragmentFooterUmumBinding? = null
  private val binding get() = _binding!!

  @RequiresApi(Build.VERSION_CODES.M)
  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = FragmentFooterUmumBinding.inflate(inflater, container, false)
    val root: View = binding.root

    if (activity?.javaClass?.simpleName == "BerandaUmumActivity") {
      setVisibleBeranda()
    } else {
      setVisibleAkun()
    }

    binding.btnBeranda.setOnClickListener {
      goToBeranda()
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

  private fun goToBeranda() {
    Intent(activity, BerandaUmumActivity::class.java).also {
      it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      startActivity(it)
    }
  }

  private fun goToAkun() {
    Intent(activity, AkunUmumActivity::class.java).also {
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
  private fun setVisibleAkun() {
    binding.btnAkun.compoundDrawableTintList = ColorStateList.valueOf(Color.WHITE)
    binding.btnAkun.setTextColor(ColorStateList.valueOf(Color.WHITE))
  }
}