package com.lesinaja.les.ui.walimurid.siswa

import android.R
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
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
import com.lesinaja.les.base.walimurid.Pendaftaran
import com.lesinaja.les.controller.walimurid.akun.DataSiswaController
import com.lesinaja.les.controller.walimurid.akun.PendaftaranController
import com.lesinaja.les.databinding.ActivityTambahSiswaBinding
import com.lesinaja.les.ui.header.ToolbarFragment
import com.lesinaja.les.ui.tutor.akun.AkunTutorActivity

class TambahSiswaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTambahSiswaBinding
    private lateinit var imageBitmap: Bitmap

    var keySiswa = ""
    var keyPembayaran = ""
    var id_jenjangkelas = ""

    companion object {
        const val REQUEST_GALLERY = 100
        const val REQUEST_CAMERA = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahSiswaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setToolbar("Tambah Siswa")

        getBiayaDaftar()

        setJenjangKelasAdapter()

        binding.ivBukti.setOnClickListener {
            openPhotoDialog()
        }

        binding.btnTambahSiswa.setOnClickListener {
            if (validateInputData()) {
                addSiswa()
                uploadImage(imageBitmap, keySiswa)
                Toast.makeText(this, "berhasil daftar siswa", Toast.LENGTH_SHORT).show()
                goToSiswa()
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
                                            binding.textBiaya.text = "Biaya Pendaftaran: ${dataSnapshotBiaya.value.toString()}"
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
            if (requestCode == AkunTutorActivity.REQUEST_GALLERY) {
                binding.ivBukti.setImageURI(data?.data)
                imageBitmap = (binding.ivBukti.drawable as BitmapDrawable).bitmap
            } else if (requestCode == AkunTutorActivity.REQUEST_CAMERA) {
                imageBitmap = data?.extras?.get("data") as Bitmap
                binding.ivBukti.setImageBitmap(imageBitmap)
            }

            binding.btnTambahSiswa.setEnabled(true)
        }
    }

    private fun validateInputData(): Boolean {
        var status = true

        if (binding.etNamaSiswa.text.toString().trim() == "") status = false
        if (binding.etNamaSekolah.text.toString().trim() == "") status = false
        if (id_jenjangkelas == "0") status = false
        if (binding.textBiaya.text == "") status = false

        return status
    }

    private fun addSiswa() {
        keySiswa = DataSiswaController().getNewKey()
        val dataSiswa = DataSiswa(
            id_jenjangkelas,
            Autentikasi.auth.currentUser?.uid!!,
            binding.etNamaSiswa.text.toString().trim(),
            binding.etNamaSekolah.text.toString().trim()
        )
        DataSiswaController().changeDataSiswa(dataSiswa, keySiswa)
    }

    private fun uploadImage(imageBitmap: Bitmap, idSiswa: String) {
        keyPembayaran = PendaftaranController().getNewKey()
        val pendaftaran = Pendaftaran(
            keySiswa,
            "",
            "",
            Autentikasi.auth.currentUser?.uid!!,
            binding.textBiaya.text.toString().substringAfter(": ").toInt(),
            PendaftaranController().getCurrentDateTime(),
            false
        )
        PendaftaranController().uploadImage(imageBitmap, idSiswa, keyPembayaran, pendaftaran)
        Database.database.getReference("jumlah_data/siswa").setValue(ServerValue.increment(1))
        Database.database.getReference("jumlah_data/pembayaran").setValue(ServerValue.increment(1))
    }

    private fun goToSiswa() {
        Intent(this, SiswaActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }
}