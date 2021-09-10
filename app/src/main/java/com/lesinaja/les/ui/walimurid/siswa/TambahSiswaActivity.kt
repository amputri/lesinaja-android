package com.lesinaja.les.ui.walimurid.siswa

import android.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.umum.Wilayah
import com.lesinaja.les.base.walimurid.DataSiswa
import com.lesinaja.les.controller.walimurid.akun.DataSiswaController
import com.lesinaja.les.databinding.ActivityTambahSiswaBinding
import com.lesinaja.les.ui.header.ToolbarFragment

class TambahSiswaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTambahSiswaBinding

    var id_jenjangkelas = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahSiswaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnKembali.setOnClickListener {
            goToSiswa()
        }
        setToolbar("Tambah Siswa")

        getBiayaDaftar()

        setJenjangKelasAdapter()

        binding.btnTambahSiswa.setOnClickListener {
            if (validateInputData()) {
                addSiswa()
            } else {
                Toast.makeText(this, "data belum valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setToolbar(judul: String) {
        val toolbarFragment = ToolbarFragment()
        val bundle = Bundle()

        bundle.putString("judul", judul)
        toolbarFragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(binding.header.id, toolbarFragment).commit()
    }

    private fun getBiayaDaftar() {
        val alamat = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/kontak/id_desa")
        alamat.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val wilayah = Database.database.getReference("wilayah_provinsi/${dataSnapshot.value.toString().substring(0,2)}/id_wilayah")
                    wilayah.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshotWilayah: DataSnapshot) {
                            if (dataSnapshotWilayah.exists()) {
                                val biayaDaftar = Database.database.getReference("master_wilayah/${dataSnapshotWilayah.value.toString()}/biaya_daftar")
                                biayaDaftar.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshotBiaya: DataSnapshot) {
                                        if (dataSnapshotBiaya.exists()) {
                                            binding.textViewBiayaLes.text = dataSnapshotBiaya.value.toString()
                                        }
                                    }
                                    override fun onCancelled(databaseError: DatabaseError) {}
                                })
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun setJenjangKelasAdapter() {
        binding.spinJenjangKelas.adapter = ArrayAdapter(
            this,
            R.layout.simple_list_item_1,
            DataSiswaController().getJenjang("0", "pilih jenjang")
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
        if (binding.textViewBiayaLes.text == "") status = false

        return status
    }

    private fun addSiswa() {
        val dataSiswa = DataSiswa(
            id_jenjangkelas,
            Autentikasi.auth.currentUser?.uid!!,
            binding.etNamaSiswa.text.toString().trim(),
            binding.etNamaSekolah.text.toString().trim(),
            binding.textViewBiayaLes.text.toString().toInt(),
            false
        )

        val key = Database.database.getReference("siswa").push().key!!
        val updates: MutableMap<String, Any> = HashMap()
        updates["siswa/${key}"] = dataSiswa
        updates["jumlah_data/siswa"] = ServerValue.increment(1)
        updates["user/${Autentikasi.auth.currentUser?.uid}/roles/jumlah_siswa"] = ServerValue.increment(1)
        Database.database.reference.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "berhasil tambah siswa", Toast.LENGTH_SHORT).show()
                goToSiswa()
            }
            .addOnFailureListener {
                Toast.makeText(this, "gagal tambah siswa", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToSiswa() {
        Intent(this, SiswaActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }
}