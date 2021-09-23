package com.lesinaja.les.ui.tutor.les

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.base.Database
import com.lesinaja.les.databinding.ActivityGajiTutorBinding
import com.lesinaja.les.ui.header.ToolbarFragment
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class GajiTutorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGajiTutorBinding

    companion object {
        const val EXTRA_IDLESSISWATUTOR = "id_les_siswa_tutor"
        const val EXTRA_NAMASISWA = "nama_siswa"
        const val EXTRA_NAMALES = "nama_les"
        const val EXTRA_JUMLAHPERTEMUAN = "jumlah_pertemuan"
        const val EXTRA_GAJITUTOR = "gaji_tutor"
        const val EXTRA_TANGGALMULAI = "tanggal_mulai"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGajiTutorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnKembali.setOnClickListener {
            onBackPressed()
        }
        setToolbar("Gaji Tutor")

        updateUI()
    }

    private fun setToolbar(judul: String) {
        val toolbarFragment = ToolbarFragment()
        val bundle = Bundle()

        bundle.putString("judul", judul)
        toolbarFragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(binding.header.id, toolbarFragment).commit()
    }

    private fun updateUI() {
        binding.tvLes.text = intent.getStringExtra(EXTRA_NAMALES)
        binding.tvGajiTutor.text =  "Rp ${NumberFormat.getNumberInstance(Locale("in", "ID")).format(intent.getStringExtra(EXTRA_GAJITUTOR).toString().toInt())}"
        binding.tvJumlahPertemuan.text = intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN)
        binding.tvSiswa.text = intent.getStringExtra(EXTRA_NAMASISWA)
        binding.tvTanggalMulai.text = intent.getStringExtra(EXTRA_TANGGALMULAI)

        val gaji = intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN).toString().toInt() * intent.getStringExtra(EXTRA_GAJITUTOR).toString().toInt()
        binding.tvJumlahGaji.text = "Rp ${NumberFormat.getNumberInstance(Locale("in", "ID")).format(gaji)}"

        val transfer = Database.database.getReference("pembayaran").orderByChild("id_lessiswatutor").equalTo(intent.getStringExtra(EXTRA_IDLESSISWATUTOR))
        transfer.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshotTransfer: DataSnapshot) {
                if (dataSnapshotTransfer.exists()) {
                    for (h in dataSnapshotTransfer.children) {
                        if (h.child("sudah_dikonfirmasi").value == true)
                            binding.tvTanggalTransfer.text =  SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm aaa").format(h.child("waktu_transfer").value.toString().toLong())
                        else
                            binding.tvTanggalTransfer.text = "belum ditransfer"
                    }
                } else {
                    binding.tvTanggalTransfer.text = "les belum selesai"
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}