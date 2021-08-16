package com.lesinaja.les.ui.walimurid.akun

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils.indexOf
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.R
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.umum.Kontak
import com.lesinaja.les.base.umum.Wilayah
import com.lesinaja.les.base.walimurid.DataWaliMurid
import com.lesinaja.les.controller.umum.UserController
import com.lesinaja.les.controller.umum.WilayahController
import com.lesinaja.les.controller.walimurid.akun.DataWaliMuridController
import com.lesinaja.les.databinding.ActivityAkunWaliMuridBinding
import com.lesinaja.les.ui.umum.akun.AkunUmumActivity

class AkunWaliMuridActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAkunWaliMuridBinding

    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var idDesa: String
    private lateinit var listReferensi: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAkunWaliMuridBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnKeluar.setOnClickListener {
            signOut()
            goToAkunUmum()
        }
        listDataAuth()
        listDataKontak()
        listDataWaliMurid()

        binding.btnKeluar.setOnClickListener {
            signOut()
        }

        binding.btnUbahProfil.setOnClickListener {
            updateProfile()
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

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun listDataWaliMurid() {
        val ref = Database.database.getReference("user_role/wali_murid/${Autentikasi.auth.currentUser?.uid}")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val pekerjaan = resources.getStringArray(R.array.pekerjaan)
                for (i in 0 until pekerjaan.size) {
                    if (pekerjaan[i] == dataSnapshot.child("pekerjaan").value.toString()) {
                        binding.spinPekerjaan.setSelection(i)
                    }
                }

                if (dataSnapshot.exists()) {
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
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun updateProfile() {
        val kontak = Kontak(idDesa, binding.etAlamat.text.toString(), binding.etTelepon.text.toString())

        val dataWaliMurid = DataWaliMurid(binding.spinPekerjaan.selectedItem.toString(), binding.spinReferensiBimbel.selectedItem.toString())

        UserController().changeDisplayName(binding.etNama.text.toString())
        UserController().changeContact(Autentikasi.auth.currentUser?.uid!!, kontak)
        DataWaliMuridController().changeDataWaliMurid(Autentikasi.auth.currentUser?.uid!!, dataWaliMurid, listReferensi[0])
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
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

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
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

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
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

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
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val selectedObject = binding.spinDesa.selectedItem as Wilayah
                idDesa = selectedObject.id
            }
        }
    }

    private fun goToAkunUmum() {
        Intent(this@AkunWaliMuridActivity, AkunUmumActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }

    private fun signOut() {
        Autentikasi.auth.signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

        goToAkunUmum()
    }
}