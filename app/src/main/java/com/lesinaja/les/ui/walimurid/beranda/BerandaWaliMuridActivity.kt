package com.lesinaja.les.ui.walimurid.beranda

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lesinaja.les.databinding.ActivityBerandaWaliMuridBinding

class BerandaWaliMuridActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBerandaWaliMuridBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBerandaWaliMuridBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}