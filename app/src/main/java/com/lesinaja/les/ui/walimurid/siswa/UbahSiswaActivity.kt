package com.lesinaja.les.ui.walimurid.siswa

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.R
import android.widget.*
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.umum.Wilayah
import com.lesinaja.les.controller.walimurid.akun.DataSiswaController
import com.lesinaja.les.databinding.ActivityTambahSiswaBinding

class UbahSiswaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTambahSiswaBinding

    var id_jenjangkelas = ""

    companion object {
        const val EXTRA_IDSISWA = "id_siswa"
        const val EXTRA_IDJENJANG = "id_jenjang"
        const val EXTRA_NAMA = "nama"
        const val EXTRA_SEKOLAH = "sekolah"
        const val EXTRA_JENJANG = "jenjang"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahSiswaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvJudul.text = "Ubah Siswa"
        binding.btnKembali.setOnClickListener {
            goToSiswa()
        }

        binding.etNamaSiswa.setText(intent.getStringExtra(EXTRA_NAMA))
        binding.etNamaSekolah.setText(intent.getStringExtra(EXTRA_SEKOLAH))

        setJenjangKelasAdapter()

        binding.btnTambahSiswa.setOnClickListener {
            if (validateInputData()) {
                addSiswa()
            } else {
                Toast.makeText(this, "data belum valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setJenjangKelasAdapter() {
        binding.spinJenjangKelas.adapter = ArrayAdapter(
            this,
            R.layout.simple_list_item_1,
            DataSiswaController().getJenjang(
                intent.getStringExtra(EXTRA_IDJENJANG)!!,
                intent.getStringExtra(EXTRA_JENJANG)!!
            )
        )

        binding.spinJenjangKelas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val selectedObject = binding.spinJenjangKelas.selectedItem as Wilayah
                id_jenjangkelas = selectedObject.id
            }
        }
    }

    private fun validateInputData(): Boolean {
        var status = true

        if (binding.etNamaSiswa.text.toString().trim() == "") status = false
        if (binding.etNamaSekolah.text.toString().trim() == "") status = false
        if (id_jenjangkelas == "0") status = false

        return status
    }

    private fun addSiswa() {
        val updates: MutableMap<String, Any> = HashMap()
        updates["siswa/${intent.getStringExtra(EXTRA_IDSISWA)}/id_jenjangkelas"] = id_jenjangkelas
        updates["siswa/${intent.getStringExtra(EXTRA_IDSISWA)}/nama"] = binding.etNamaSiswa.text.toString().trim()
        updates["siswa/${intent.getStringExtra(EXTRA_IDSISWA)}/sekolah"] = binding.etNamaSekolah.text.toString().trim()
        Database.database.reference.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "berhasil ubah data siswa", Toast.LENGTH_SHORT).show()
                goToSiswa()
            }
            .addOnFailureListener {
                Toast.makeText(this, "gagal ubah data siswa", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToSiswa() {
        Intent(this, SiswaActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }
}