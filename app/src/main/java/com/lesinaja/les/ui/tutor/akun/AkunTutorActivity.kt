package com.lesinaja.les.ui.tutor.akun

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.tutor.DataTutor
import com.lesinaja.les.base.umum.Wilayah
import com.lesinaja.les.base.umum.Kontak
import com.lesinaja.les.controller.umum.WilayahController
import com.lesinaja.les.databinding.ActivityAkunTutorBinding
import com.lesinaja.les.ui.umum.akun.AkunUmumActivity
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class AkunTutorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAkunTutorBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var imageBitmap: Bitmap
    private lateinit var idDesa: String
    private lateinit var idMapelAhli: Array<String>
    private lateinit var idJenjangAhli: Array<String>

    private var tutorBaru = false

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

        binding.ivFoto.setOnClickListener {
            openPhotoDialog()
        }

        binding.btnUbahFoto.setOnClickListener {
            if (validateInputData()) {
                uploadImage(imageBitmap)
            } else {
                Toast.makeText(this, "lengkapi data terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnMapelAhli.setOnClickListener {
            showMapelDialog()
        }

        binding.btnJenjangAhli.setOnClickListener {
            showJenjangDialog()
        }

        binding.btnUbahProfil.setOnClickListener {
            if (validateInputData()) {
                updateProfile()
            } else {
                Toast.makeText(this, "data belum valid", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnKeluar.setOnClickListener {
            signOut()
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
            override fun onCancelled(databaseError: DatabaseError) {}
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
                } else {
                    tutorBaru = true
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun openPhotoDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Unggah Foto dari")
        builder.setPositiveButton("kamera") { p0,p1 ->
            openCameraForImage()
        }
        builder.setNegativeButton("file") { p0,p1 ->
            openGalleryForImage()
        }
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
        var imageBit = imageBitmap

        if ((imageBitmap.getHeight() * imageBitmap.getWidth() / 360000) > 1) {
            val ratio = Math.sqrt(((imageBitmap.getHeight() * imageBitmap.getWidth() / 360000).toDouble()))
            imageBit = Bitmap.createScaledBitmap(
                imageBitmap,
                Math.round(imageBitmap.getWidth() / ratio).toInt(),
                Math.round(imageBitmap.getHeight() / ratio).toInt(),
                true
            )
        }

        val baos = ByteArrayOutputStream()
        imageBit.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

        val ref = FirebaseStorage.getInstance().reference.child("foto_tutor/${Autentikasi.auth.currentUser?.uid}")
        ref.putBytes(image)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener {
                        it.result?.let {
                            UserProfileChangeRequest.Builder().setPhotoUri(it).build().also {
                                Autentikasi.auth.currentUser?.updateProfile(it)
                            }
                            Database.database.getReference("user_role/tutor/${Autentikasi.auth.currentUser?.uid}/link_foto").setValue(it.toString())
                                .addOnSuccessListener {
                                    binding.btnUbahFoto.setEnabled(false)
                                    Toast.makeText(this, "berhasil ubah foto", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "gagal ubah foto", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
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
                builder.setMultiChoiceItems(nama, ahli, {dialog, which, isChecked ->
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
            override fun onCancelled(databaseError: DatabaseError) {}
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
                builder.setMultiChoiceItems(nama, ahli, {dialog, which, isChecked ->
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
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun validateInputData(): Boolean {
        var status = true

        if (binding.etNama.text.toString().trim() == "") status = false
        if (binding.etTelepon.text.toString().trim() == "") status = false
        if (idDesa == "0000000000") status = false
        if (binding.etAlamat.text.toString().trim() == "") status = false
        if (binding.etUniversitas.text.toString().trim() == "") status = false
        if (binding.etJurusan.text.toString().trim() == "") status = false
        if (idMapelAhli.size == 0) status = false
        if (idJenjangAhli.size == 0) status = false
        if (binding.etPengalamanMengajar.text.toString().trim() == "") status = false
        if (binding.etLinkMicroteaching.text.toString().trim() == "") status = false
        if (binding.etBank.text.toString().trim() == "") status = false
        if (binding.etNomorRekening.text.toString().trim() == "") status = false

        return status
    }

    private fun updateProfile() {
        val kontak = Kontak(
            idDesa,
            binding.etAlamat.text.toString().trim(),
            binding.etTelepon.text.toString().trim()
        )
        val dataTutor = DataTutor(
            getGenderChecked(),
            binding.etUniversitas.text.toString().trim(),
            binding.etJurusan.text.toString().trim(),
            binding.etPengalamanMengajar.text.toString().trim(),
            binding.etLinkMicroteaching.text.toString().trim(),
            binding.etBank.text.toString().trim(),
            binding.etNomorRekening.text.toString().trim(),
            Autentikasi.auth.currentUser?.photoUrl.toString().trim()
        )

        val profileUpdates = userProfileChangeRequest {
            displayName = binding.etNama.text.toString().trim()
        }

        Autentikasi.auth.currentUser!!.updateProfile(profileUpdates)
            .addOnSuccessListener {
                val updates: MutableMap<String, Any> = HashMap()
                updates["user/${Autentikasi.auth.currentUser?.uid!!}/kontak"] = kontak
                updates["user/${Autentikasi.auth.currentUser?.uid!!}/nama"] = Autentikasi.auth.currentUser?.displayName.toString().trim()
                updates["user/${Autentikasi.auth.currentUser?.uid!!}/email"] = Autentikasi.auth.currentUser?.email.toString().trim()
                updates["user_role/tutor/${Autentikasi.auth.currentUser?.uid!!}"] = dataTutor
                updates["user/${Autentikasi.auth.currentUser?.uid!!}/roles/tutor"] = true

                if (tutorBaru) {
                    updates["jumlah_data/user/tutor"] = ServerValue.increment(1)
                }

                Database.database.reference.updateChildren(updates)
                    .addOnSuccessListener {
                        val secondUpdates: MutableMap<String, Any> = HashMap()
                        for (i in 0 until idMapelAhli.size) {
                            secondUpdates["user_role/tutor/${Autentikasi.auth.currentUser?.uid!!}/mapel_ahli/${i}"] = idMapelAhli[i]
                        }
                        for (i in 0 until idJenjangAhli.size) {
                            secondUpdates["user_role/tutor/${Autentikasi.auth.currentUser?.uid!!}/jenjang_ahli/${i}"] = idJenjangAhli[i]
                        }
                        Database.database.reference.updateChildren(secondUpdates)

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

    private fun goToAkunUmum() {
        Intent(this@AkunTutorActivity, AkunUmumActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }

    private fun signOut() {
        Autentikasi.auth.signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

        goToAkunUmum()
    }
}