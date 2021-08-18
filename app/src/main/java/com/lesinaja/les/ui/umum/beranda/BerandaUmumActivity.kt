package com.lesinaja.les.ui.umum.beranda

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lesinaja.les.databinding.ActivityBerandaUmumBinding

class BerandaUmumActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBerandaUmumBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBerandaUmumBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}