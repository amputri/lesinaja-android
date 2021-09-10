package com.lesinaja.les.ui.header

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lesinaja.les.databinding.FragmentWhatsappBinding

class WhatsappFragment : Fragment() {
    private var _binding: FragmentWhatsappBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWhatsappBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnWA.setOnClickListener { openLink() }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openLink() {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse("https://wa.me/6281242306969")
        startActivity(openURL)
    }
}