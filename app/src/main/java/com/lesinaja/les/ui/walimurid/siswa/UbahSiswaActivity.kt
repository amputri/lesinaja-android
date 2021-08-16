package com.lesinaja.les.ui.walimurid.siswa

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
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.R
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.umum.Wilayah
import com.lesinaja.les.base.walimurid.DataSiswa
import com.lesinaja.les.controller.walimurid.akun.DataSiswaController
import com.lesinaja.les.controller.walimurid.akun.PendaftaranController
import com.lesinaja.les.databinding.ActivityTambahSiswaBinding
import com.lesinaja.les.ui.header.ToolbarFragment
import com.lesinaja.les.ui.tutor.akun.AkunTutorActivity
import com.squareup.picasso.Picasso

class UbahSiswaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTambahSiswaBinding
    private lateinit var imageBitmap: Bitmap

    var changeImage = "false"
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

        setToolbar("Ubah Siswa")

        binding.etNamaSiswa.setText(intent.getStringExtra(EXTRA_NAMA))
        binding.etNamaSekolah.setText(intent.getStringExtra(EXTRA_SEKOLAH))

        binding.btnTambahSiswa.setOnClickListener {
            addSiswa()
            if (binding.textUnggah.text != "pembayaran sudah diverifikasi admin" && changeImage == "true") {
                uploadImage(imageBitmap, intent.getStringExtra(EXTRA_IDSISWA).toString())
            }
            goToSiswa()
        }

        binding.ivBukti.setOnClickListener {
            if (binding.textUnggah.text != "pembayaran sudah diverifikasi admin") {
                openPhotoDialog()
            }
        }

        setJenjangKelasAdapter()

        getBiayaDaftar()

        binding.btnTambahSiswa.setEnabled(true)
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
            changeImage = "true"
        }
    }

    fun uploadImage(imageBitmap: Bitmap, idSiswa: String) {
        PendaftaranController().uploadImageChange(
            imageBitmap,
            idSiswa,
            binding.textBiaya.text.toString().substringAfter("-- ")
        )
    }

    fun addSiswa() {
        val dataSiswa = DataSiswa(
            id_jenjangkelas,
            Autentikasi.auth.currentUser?.uid!!,
            binding.etNamaSiswa.text.toString(),
            binding.etNamaSekolah.text.toString()
        )
        DataSiswaController().changeDataSiswaUpdate(dataSiswa, intent.getStringExtra(EXTRA_IDSISWA).toString())
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
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val selectedObject = binding.spinJenjangKelas.selectedItem as Wilayah
                id_jenjangkelas = selectedObject.id
            }
        }
    }

    private fun getBiayaDaftar() {
        val biaya = Database.database.getReference("pembayaran").orderByChild("id_pengirim").equalTo(intent.getStringExtra(EXTRA_IDSISWA))
        biaya.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (h in dataSnapshot.children) {
                    binding.textBiaya.text = "Biaya Pendaftaran: ${h.child("nominal").value.toString()} -- ${h.key}"
                    if (h.child("id_penerima").value.toString() != "null") {
                        binding.textUnggah.text = "pembayaran sudah diverifikasi admin"
                    }
                    Picasso.get().load(h.child("bukti").value.toString()).into(binding.ivBukti)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }
}