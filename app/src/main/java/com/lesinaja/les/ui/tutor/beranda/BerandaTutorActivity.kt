package com.lesinaja.les.ui.tutor.beranda

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lesinaja.les.databinding.ActivityBerandaTutorBinding

class BerandaTutorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBerandaTutorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBerandaTutorBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}