package com.lesinaja.les.ui.tutor.akun

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.tutor.DataTutor
import com.lesinaja.les.base.umum.Wilayah
import com.lesinaja.les.base.umum.Kontak
import com.lesinaja.les.controller.tutor.akun.DataTutorController
import com.lesinaja.les.controller.tutor.akun.FotoProfilController
import com.lesinaja.les.controller.umum.UserController
import com.lesinaja.les.controller.umum.WilayahController
import com.lesinaja.les.databinding.ActivityAkunTutorBinding
import com.lesinaja.les.ui.umum.akun.AkunUmumActivity
import com.squareup.picasso.Picasso

class AkunTutorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAkunTutorBinding
    private lateinit var imageBitmap: Bitmap
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var idDesa: String
    private lateinit var idMapelAhli: Array<String>
    private lateinit var idJenjangAhli: Array<String>

    companion object {
        const val REQUEST_GALLERY = 100
        const val REQUEST_CAMERA = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAkunTutorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idMapelAhli = arrayOf()
        idJenjangAhli = arrayOf()

        listDataAuth()
        listDataKontak()
        listDataTutor()

        binding.btnKeluar.setOnClickListener {
            signOut()
        }

        binding.ivFoto.setOnClickListener {
            openPhotoDialog()
        }

        binding.btnUbahFoto.setOnClickListener {
            uploadImage(imageBitmap)
        }

        binding.btnUbahProfil.setOnClickListener {
            updateProfile()
        }

        binding.btnMapelAhli.setOnClickListener {
            showMapelDialog()
        }

        binding.btnJenjangAhli.setOnClickListener {
            showJenjangDialog()
        }
    }

    private fun listDataAuth() {
        binding.etEmail.setText(Autentikasi.auth.currentUser?.email)
        binding.etNama.setText(Autentikasi.auth.currentUser?.displayName)

        if (Autentikasi.auth.currentUser?.photoUrl != null) {
            Picasso.get().load(Autentikasi.auth.currentUser?.photoUrl).into(binding.ivFoto)
        }
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

    private fun setGenderChecked(gender: String) {
        if (gender == "laki-laki") binding.laki.isChecked = true
        else binding.perempuan.isChecked = true
    }

    private fun getGenderChecked(): String {
        if (binding.laki.isChecked) return "laki-laki"
        else return "perempuan"
    }

    private fun listDataTutor() {
        val ref = Database.database.getReference("user_role/tutor/${Autentikasi.auth.currentUser?.uid}")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    setGenderChecked(dataSnapshot.child("jenis_kelamin").getValue().toString())
                    binding.etUniversitas.setText(dataSnapshot.child("perguruan_tinggi").getValue().toString())
                    binding.etJurusan.setText(dataSnapshot.child("jurusan").getValue().toString())
                    binding.etPengalamanMengajar.setText(dataSnapshot.child("pengalaman_mengajar").getValue().toString())
                    binding.etLinkMicroteaching.setText(dataSnapshot.child("link_microteaching").getValue().toString())
                    binding.etBank.setText(dataSnapshot.child("bank").getValue().toString())
                    binding.etNomorRekening.setText(dataSnapshot.child("nomor_rekening").getValue().toString())

                    for (h in dataSnapshot.child("mapel_ahli").children) {
                        idMapelAhli = idMapelAhli.plus(h.value.toString())
                    }

                    for (h in dataSnapshot.child("jenjang_ahli").children) {
                        idJenjangAhli = idJenjangAhli.plus(h.value.toString())
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun updateProfile() {
        val kontak = Kontak(idDesa, binding.etAlamat.text.toString(), binding.etTelepon.text.toString())
        val dataTutor = DataTutor(
            getGenderChecked(),
            binding.etUniversitas.text.toString(),
            binding.etJurusan.text.toString(),
            binding.etPengalamanMengajar.text.toString(),
            binding.etLinkMicroteaching.text.toString(),
            binding.etBank.text.toString(),
            binding.etNomorRekening.text.toString()
        )

        UserController().changeDisplayName(binding.etNama.text.toString())
        UserController().changeContact(Autentikasi.auth.currentUser?.uid!!, kontak)
        DataTutorController().changeDataTutor(Autentikasi.auth.currentUser?.uid!!, dataTutor, idMapelAhli, idJenjangAhli)
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
        Intent(this@AkunTutorActivity, AkunUmumActivity::class.java).also {
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

    private fun openPhotoDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Unggah Foto dari")
            .setPositiveButton("kamera",
                DialogInterface.OnClickListener { dialog, id ->
                    openCameraForImage()
                })
            .setNegativeButton("file",
                DialogInterface.OnClickListener { dialog, id ->
                    openGalleryForImage()
                })
        builder.show()
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun openCameraForImage() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(packageManager).also {
                startActivityForResult(intent, REQUEST_CAMERA)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY) {
                binding.ivFoto.setImageURI(data?.data)
                imageBitmap = (binding.ivFoto.drawable as BitmapDrawable).bitmap
            } else if (requestCode == REQUEST_CAMERA) {
                imageBitmap = data?.extras?.get("data") as Bitmap
                binding.ivFoto.setImageBitmap(imageBitmap)
            }

            binding.btnUbahFoto.setEnabled(true)
        }
    }

    private fun uploadImage(imageBitmap: Bitmap) {
        FotoProfilController().uploadImage(imageBitmap)
        binding.btnUbahFoto.setEnabled(false)
    }

    private fun showMapelDialog(){
        var id: Array<String> = arrayOf()
        var nama: Array<String> = arrayOf()
        var ahli: BooleanArray = booleanArrayOf()

        val mapel = Database.database.getReference("master_mapel")
        mapel.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (h in dataSnapshot.children) {
                    id = id.plus(h.key!!)
                    nama = nama.plus(h.child("nama").value.toString())
                    if (h.key!! in idMapelAhli) {
                        ahli = ahli.plus(true)
                    } else {
                        ahli = ahli.plus(false)
                    }
                }

                val builder = AlertDialog.Builder(this@AkunTutorActivity)
                builder.setTitle("Pilih Pelajaran Ahli")
                builder.setMultiChoiceItems(nama, ahli, {dialog,which,isChecked->
                    ahli[which] = isChecked
                })

                builder.setPositiveButton("KIRIM") { _, _ ->
                    idMapelAhli = arrayOf()
                    for (i in 0 until id.size) {
                        if (ahli[i]) {
                            idMapelAhli = idMapelAhli.plus(id[i])
                        }
                    }
                }

                builder.show()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun showJenjangDialog(){
        var id: Array<String> = arrayOf()
        var nama: Array<String> = arrayOf()
        var ahli: BooleanArray = booleanArrayOf()

        val jenjang = Database.database.getReference("master_jenjangkelas")
        jenjang.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (h in dataSnapshot.children) {
                    id = id.plus(h.key!!)
                    nama = nama.plus(h.child("nama").value.toString())
                    if (h.key!! in idJenjangAhli) {
                        ahli = ahli.plus(true)
                    } else {
                        ahli = ahli.plus(false)
                    }
                }

                val builder = AlertDialog.Builder(this@AkunTutorActivity)
                builder.setTitle("Pilih Jenjang Ahli")
                builder.setMultiChoiceItems(nama, ahli, {dialog,which,isChecked->
                    ahli[which] = isChecked
                })

                builder.setPositiveButton("KIRIM") { _, _ ->
                    idJenjangAhli = arrayOf()
                    for (i in 0 until id.size) {
                        if (ahli[i]) {
                            idJenjangAhli = idJenjangAhli.plus(id[i])
                        }
                    }
                }

                builder.show()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }
}