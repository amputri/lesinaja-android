package com.lesinaja.les.ui.walimurid.akun

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.R
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.umum.Kontak
import com.lesinaja.les.base.umum.Wilayah
import com.lesinaja.les.base.walimurid.DataWaliMurid
import com.lesinaja.les.controller.umum.WilayahController
import com.lesinaja.les.databinding.ActivityAkunWaliMuridBinding

class AkunWaliMuridActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAkunWaliMuridBinding

    private lateinit var idDesa: String
    private lateinit var listReferensi: Array<String>

    private var waliMuridBaru = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAkunWaliMuridBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listDataAuth()
        listDataKontak()
        listDataWaliMurid()

        binding.btnUbahProfil.setOnClickListener {
            if (validateInputData()) {
                updateProfile()
            } else {
                Toast.makeText(this, "data belum valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun listDataAuth() {
        binding.etEmail.setText(Autentikasi.auth.currentUser?.email)
        binding.etNama.setText(Autentikasi.auth.currentUser?.displayName)
    }

    private fun listDataKontak() {
        val ref = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/kontak")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    binding.etTelepon.setText(dataSnapshot.child("telepon").getValue().toString())
                    binding.etAlamat.setText(dataSnapshot.child("alamat_rumah").getValue().toString())
                    setProvinsiAdapter(dataSnapshot.child("id_desa").getValue().toString())
                } else {
                    setProvinsiAdapter("0000000000")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun listDataWaliMurid() {
        val ref = Database.database.getReference("user_role/wali_murid/${Autentikasi.auth.currentUser?.uid}")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val pekerjaan = resources.getStringArray(R.array.pekerjaan)
                    for (i in 0 until pekerjaan.size) {
                        if (pekerjaan[i] == dataSnapshot.child("pekerjaan").value.toString()) {
                            binding.spinPekerjaan.setSelection(i)
                        }
                    }

                    val referensi = resources.getStringArray(R.array.referensi_bimbel)
                    listReferensi = arrayOf()
                    for (i in 0 until referensi.size) {
                        if (referensi[i] == dataSnapshot.child("referensi_bimbel").value.toString()) {
                            binding.spinReferensiBimbel.setSelection(i)
                            listReferensi = listReferensi.plus(referensi[i])
                        }
                    }
                } else {
                    listReferensi = arrayOf("")
                    waliMuridBaru = true
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun validateInputData(): Boolean {
        var status = true

        if (binding.etNama.text.toString().trim() == "") status = false
        if (binding.etTelepon.text.toString().trim() == "") status = false
        if (idDesa == "0000000000") status = false
        if (binding.etAlamat.text.toString().trim() == "") status = false

        return status
    }

    private fun updateProfile() {
        val kontak = Kontak(idDesa,
                        binding.etAlamat.text.toString().trim(),
                        binding.etTelepon.text.toString().trim())
        val dataWaliMurid = DataWaliMurid(binding.spinPekerjaan.selectedItem.toString(), binding.spinReferensiBimbel.selectedItem.toString())

        val profileUpdates = userProfileChangeRequest {
            displayName = binding.etNama.text.toString().trim()
        }

        Autentikasi.auth.currentUser!!.updateProfile(profileUpdates)
            .addOnSuccessListener {
                val updates: MutableMap<String, Any> = HashMap()
                updates["user/${Autentikasi.auth.currentUser?.uid!!}/kontak"] = kontak
                updates["user/${Autentikasi.auth.currentUser?.uid!!}/nama"] = Autentikasi.auth.currentUser?.displayName.toString()
                updates["user/${Autentikasi.auth.currentUser?.uid!!}/email"] = Autentikasi.auth.currentUser?.email.toString()
                updates["user_role/wali_murid/${Autentikasi.auth.currentUser?.uid!!}"] = dataWaliMurid
                updates["user/${Autentikasi.auth.currentUser?.uid!!}/roles/wali_murid"] = true

                if (listReferensi[0] == "") {
                    updates["referensi_bimbel/${dataWaliMurid.referensi_bimbel}"] = ServerValue.increment(1)
                } else if (listReferensi[0] != dataWaliMurid.referensi_bimbel) {
                    updates["referensi_bimbel/${dataWaliMurid.referensi_bimbel}"] = ServerValue.increment(1)
                    updates["referensi_bimbel/${listReferensi[0]}"] = ServerValue.increment(-1)
                }

                if (waliMuridBaru) {
                    updates["jumlah_data/user/wali_murid"] = ServerValue.increment(1)
                }

                Database.database.reference.updateChildren(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "berhasil ubah data", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "gagal ubah data user, coba lagi", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "gagal ubah data auth, coba lagi", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setProvinsiAdapter(idDesaUser: String) {
        if (idDesaUser != "0000000000") {
            val provinsiUser = Database.database.getReference("wilayah_provinsi/${idDesaUser.substring(0,2)}")
            provinsiUser.get().addOnSuccessListener {
                binding.spinProvinsi.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    WilayahController().getProvinsiList(it.key!!, it.child("nama").value.toString())
                )
            }
        } else {
            binding.spinProvinsi.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                WilayahController().getProvinsiList("00", "pilih provinsi")
            )
        }

        binding.spinProvinsi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val selectedObject = binding.spinProvinsi.selectedItem as Wilayah
                setKabupatenAdapter(selectedObject.id, idDesaUser)
            }
        }
    }

    private fun setKabupatenAdapter(_idProvinsi: String, idDesaUser: String) {
        if (_idProvinsi == idDesaUser.substring(0,2) && idDesaUser != "0000000000") {
            val kabupatenUser = Database.database.getReference("wilayah_kabupaten/${idDesaUser.substring(0,2)}/${idDesaUser.substring(0,4)}")
            kabupatenUser.get().addOnSuccessListener {
                binding.spinKabupaten.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    WilayahController().getKabupatenList(_idProvinsi, it.key!!, it.child("nama").value.toString())
                )
            }
        } else {
            binding.spinKabupaten.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                WilayahController().getKabupatenList(_idProvinsi, "0000", "pilih kabupaten")
            )
        }

        binding.spinKabupaten.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val selectedObject = binding.spinKabupaten.selectedItem as Wilayah
                setKecamatanAdapter(selectedObject.id, idDesaUser)
            }
        }
    }

    private fun setKecamatanAdapter(_idKabupaten: String, idDesaUser: String) {
        if (_idKabupaten == idDesaUser.substring(0,4) && idDesaUser != "0000000000") {
            val kecamatanUser = Database.database.getReference("wilayah_kecamatan/${idDesaUser.substring(0,2)}/${idDesaUser.substring(0,4)}/${idDesaUser.substring(0,7)}")
            kecamatanUser.get().addOnSuccessListener {
                binding.spinKecamatan.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    WilayahController().getKecamatanList(_idKabupaten, it.key!!, it.child("nama").value.toString())
                )
            }
        } else {
            binding.spinKecamatan.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                WilayahController().getKecamatanList(_idKabupaten, "0000000", "pilih kecamatan")
            )
        }

        binding.spinKecamatan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val selectedObject = binding.spinKecamatan.selectedItem as Wilayah
                setDesaAdapter(selectedObject.id, idDesaUser)
            }
        }
    }

    private fun setDesaAdapter(_idKecamatan: String, idDesaUser: String) {
        if (_idKecamatan == idDesaUser.substring(0,7) && idDesaUser != "0000000000") {
            val desaUser = Database.database.getReference("wilayah_desa/${idDesaUser.substring(0,2)}/${idDesaUser.substring(0,4)}/${idDesaUser.substring(0,7)}/${idDesaUser}")
            desaUser.get().addOnSuccessListener {
                binding.spinDesa.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    WilayahController().getDesaList(_idKecamatan, it.key!!, it.child("nama").value.toString())
                )
            }
        } else {
            binding.spinDesa.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                WilayahController().getDesaList(_idKecamatan, "0000000000", "pilih desa")
            )
        }

        binding.spinDesa.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val selectedObject = binding.spinDesa.selectedItem as Wilayah
                idDesa = selectedObject.id
            }
        }
    }
}