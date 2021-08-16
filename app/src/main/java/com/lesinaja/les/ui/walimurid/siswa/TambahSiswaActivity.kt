package com.lesinaja.les.ui.walimurid.siswa

import android.R
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.umum.Wilayah
import com.lesinaja.les.base.walimurid.DataSiswa
import com.lesinaja.les.base.walimurid.Pendaftaran
import com.lesinaja.les.controller.umum.WilayahController
import com.lesinaja.les.controller.walimurid.akun.DataSiswaController
import com.lesinaja.les.controller.walimurid.akun.DataWaliMuridController
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

        binding.btnTambahSiswa.setOnClickListener {
            addSiswa()
            uploadImage(imageBitmap, keySiswa)
            goToSiswa()
        }

        binding.ivBukti.setOnClickListener {
            openPhotoDialog()
        }

        setJenjangKelasAdapter()

        getBiayaDaftar()
    }

    private fun setToolbar(judul: String) {
        val toolbarFragment = ToolbarFragment()
        val bundle = Bundle()

        bundle.putString("judul", judul)
        toolbarFragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(binding.header.id, toolbarFragment).commit()
    }

    private fun goToSiswa() {
        Intent(this, SiswaActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
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
        startActivityForResult(intent, TambahSiswaActivity.REQUEST_GALLERY)
    }

    private fun openCameraForImage() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(packageManager).also {
                startActivityForResult(intent, TambahSiswaActivity.REQUEST_CAMERA)
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

    fun uploadImage(imageBitmap: Bitmap, idSiswa: String) {
        keyPembayaran = PendaftaranController().getNewKey()
        val pendaftaran = Pendaftaran(
            true,
            "",
            "admin",
            keySiswa,
            binding.textBiaya.text.toString().substringAfter(": ").toInt(),
            PendaftaranController().getCurrentDateTime()
        )
        PendaftaranController().uploadImage(imageBitmap, idSiswa, keyPembayaran, pendaftaran)
    }

    fun addSiswa() {
        keySiswa = DataSiswaController().getNewKey()
        val dataSiswa = DataSiswa(
            id_jenjangkelas,
            Autentikasi.auth.currentUser?.uid!!,
            binding.etNamaSiswa.text.toString(),
            binding.etNamaSekolah.text.toString()
        )
        DataSiswaController().changeDataSiswa(dataSiswa, keySiswa)
    }

    private fun setJenjangKelasAdapter() {
        binding.spinJenjangKelas.adapter = ArrayAdapter(
            this,
            R.layout.simple_list_item_1,
            DataSiswaController().getJenjang("0", "pilih jenjang")
        )

        binding.spinJenjangKelas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val selectedObject = binding.spinJenjangKelas.selectedItem as Wilayah
                id_jenjangkelas = selectedObject.id
            }
        }
    }

    private fun getBiayaDaftar() {
        val alamat = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid}/kontak/id_desa")
        alamat.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val wilayah = Database.database.getReference("wilayah_provinsi/${dataSnapshot.value.toString().substring(0,2)}/id_wilayah")
                wilayah.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshotWilayah: DataSnapshot) {
                        val biayaDaftar = Database.database.getReference("master_wilayah/${dataSnapshotWilayah.value.toString()}/biaya_daftar")
                        biayaDaftar.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshotBiaya: DataSnapshot) {
                                binding.textBiaya.text = "Biaya Pendaftaran: ${dataSnapshotBiaya.value.toString()}"
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        })
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }
}