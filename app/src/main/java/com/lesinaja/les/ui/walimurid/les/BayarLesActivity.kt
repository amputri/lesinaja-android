package com.lesinaja.les.ui.walimurid.les

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.Pembayaran
import com.lesinaja.les.base.walimurid.PembayaranLes
import com.lesinaja.les.databinding.ActivityBayarLesBinding
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat

class BayarLesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBayarLesBinding
    private lateinit var imageBitmap: Bitmap

    companion object {
        const val EXTRA_IDLESSISWA = "id_les_siswa"
        const val EXTRA_NAMASISWA = "nama_siswa"
        const val EXTRA_NAMALES = "nama_les"
        const val EXTRA_JUMLAHPERTEMUAN = "jumlah_pertemuan"
        const val REQUEST_GALLERY = 100
        const val REQUEST_CAMERA = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBayarLesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnKembali.setOnClickListener {
            goToLes()
        }

        updateUI()

        binding.btnTambahLes.setEnabled(false)

        binding.ivBukti.setOnClickListener {
            if (binding.textUnggah.text.toString().substringAfter(": ") != "sudah diverifikasi admin") {
                openPhotoDialog()
            }
        }

        binding.btnTambahLes.setOnClickListener {
            uploadBuktiPembayaran(imageBitmap, intent.getStringExtra(EXTRA_IDLESSISWA).toString())
        }

        binding.btnHapus.setOnClickListener {
            hapusLes()
        }
    }

    private fun updateUI() {
        binding.textSiswa.text = "Siswa: ${intent.getStringExtra(EXTRA_NAMASISWA)}"
        binding.tvLes.text = "${intent.getStringExtra(EXTRA_NAMALES)} (${intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN)}x)"

        getPembayaran()
        loadJadwal()
        loadPreferensiTutor()
    }

    private fun getBiayaDaftar() {
        val siswa = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/id_siswa")
        siswa.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotSiswa: DataSnapshot) {
                if (dataSnapshotSiswa.exists()) {
                    val statusBayar = Database.database.getReference("siswa/${dataSnapshotSiswa.value}/status_bayar")
                    statusBayar.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshotBayar: DataSnapshot) {
                            if (dataSnapshotBayar.exists()) {
                                if (dataSnapshotBayar.value.toString() != "true") {
                                    val siswaDaftar = Database.database.getReference("siswa/${dataSnapshotSiswa.value}/biaya_daftar")
                                    siswaDaftar.addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(dataSnapshotSiswaDaftar: DataSnapshot) {
                                            if (dataSnapshotSiswaDaftar.exists()) {
                                                binding.tvBiayaDaftar.text = dataSnapshotSiswaDaftar.value.toString()
                                                getBiayaBayar()
                                            }
                                        }
                                        override fun onCancelled(databaseError: DatabaseError) {}
                                    })
                                } else {
                                    binding.tvBiayaDaftar.text = "-"
                                    getBiayaBayar()
                                }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getBiayaBayar() {
        val les = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/biaya_les")
        les.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotLes: DataSnapshot) {
                if (dataSnapshotLes.exists()) {
                    binding.tvBiayaLes.text = dataSnapshotLes.value.toString()
                    loadTotal()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getPembayaran() {
        val pembayaran = Database.database.getReference("pembayaran").orderByChild("id_lessiswa").equalTo(intent.getStringExtra(EXTRA_IDLESSISWA))
        pembayaran.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotPembayaran: DataSnapshot) {
                if (dataSnapshotPembayaran.exists()) {
                    for (h in dataSnapshotPembayaran.children) {
                        if (h.child("biaya_daftar").exists()) {
                            binding.tvBiayaDaftar.text = h.child("biaya_daftar").value.toString()
                        } else {
                            binding.tvBiayaDaftar.text = "-"
                        }

                        binding.tvBiayaLes.text = h.child("biaya_les").value.toString()

                        if (h.child("sudah_dikonfirmasi").value.toString() == "true") {
                            binding.textUnggah.text = "pembayaran ${h.key} : sudah diverifikasi admin"
                            binding.btnHapus.setEnabled(false)
                        } else {
                            binding.textUnggah.text = "pembayaran ${h.key} : belum dikonfirmasi admin, dapat diubah kembali"
                        }

                        loadTotal()

                        Picasso.get().load(h.child("bukti").value.toString()).into(binding.ivBukti)
                    }
                }
                else {
                    getBiayaDaftar()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun loadJadwal() {
        val jadwal = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/waktu_mulai")
        jadwal.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (i in 0 until dataSnapshot.childrenCount) {
                        binding.tvJadwalLes.text = binding.tvJadwalLes.text.toString()+ SimpleDateFormat("EEEE, dd MMMM yyyy").format(dataSnapshot.child("${i}").value)+" Jam "+ SimpleDateFormat("hh:mm aaa").format(dataSnapshot.child("${i}").value)+"\n"
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun loadPreferensiTutor() {
        val preferensiTutor = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/preferensi_tutor")
        preferensiTutor.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotPreferensiTutor: DataSnapshot) {
                if (dataSnapshotPreferensiTutor.exists()) {
                    binding.tvPreferensiTutor.text = dataSnapshotPreferensiTutor.value.toString()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun loadTotal() {
        if (binding.tvBiayaDaftar.text.toString() == "-") {
            binding.tvTotal.text = binding.tvBiayaLes.text.toString()
        } else {
            val total = binding.tvBiayaLes.text.toString().toInt() + binding.tvBiayaDaftar.text.toString().toInt()
            binding.tvTotal.text = total.toString()
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
            if (requestCode == REQUEST_GALLERY) {
                binding.ivBukti.setImageURI(data?.data)
                imageBitmap = (binding.ivBukti.drawable as BitmapDrawable).bitmap
            } else if (requestCode == REQUEST_CAMERA) {
                imageBitmap = data?.extras?.get("data") as Bitmap
                binding.ivBukti.setImageBitmap(imageBitmap)
            }

            binding.btnTambahLes.setEnabled(true)
        }
    }

    private fun uploadBuktiPembayaran(imageBitmap: Bitmap, keyLes: String) {
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

        val ref = FirebaseStorage.getInstance().reference.child("bukti_bayar/${intent.getStringExtra(EXTRA_IDLESSISWA)}")
        ref.putBytes(image)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener {
                        it.result?.let {
                            var key = ""
                            if (binding.textUnggah.text.toString() == "Unggah Bukti Pembayaran berikut") {
                                key = Database.database.getReference("pembayaran").push().key!!
                                Database.database.getReference("jumlah_data/pembayaran").setValue(ServerValue.increment(1))
                            } else {
                                key = binding.textUnggah.text.toString().substringBefore(" :").substringAfter("pembayaran ")
                            }

                            if (binding.tvBiayaDaftar.text.toString() == "-") {
                                val pembayaran = PembayaranLes(
                                    it.toString(),
                                    "",
                                    Autentikasi.auth.currentUser?.uid!!,
                                    "",
                                    intent.getStringExtra(EXTRA_IDLESSISWA).toString(),
                                    binding.tvBiayaLes.text.toString().toInt(),
                                    System.currentTimeMillis(),
                                    false
                                )
                                Database.database.getReference("pembayaran/${key}").setValue(pembayaran)
                            } else {
                                val pembayaran = Pembayaran(
                                    it.toString(),
                                    "",
                                    Autentikasi.auth.currentUser?.uid!!,
                                    "",
                                    intent.getStringExtra(EXTRA_IDLESSISWA).toString(),
                                    binding.tvBiayaDaftar.text.toString().toInt(),
                                    binding.tvBiayaLes.text.toString().toInt(),
                                    System.currentTimeMillis(),
                                    false
                                )
                                Database.database.getReference("pembayaran/${key}").setValue(pembayaran)
                            }

                            val admin = Database.database.getReference("user").orderByChild("roles/admin").equalTo(true)
                            admin.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshotAdmin: DataSnapshot) {
                                    if (dataSnapshotAdmin.exists()) {
                                        for (h in dataSnapshotAdmin.children) {
                                            Database.database.getReference("pembayaran/${key}/id_penerima").setValue(h.key)
                                        }
                                    }
                                }
                                override fun onCancelled(databaseError: DatabaseError) {}
                            })

                            val idSiswa = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/id_siswa")
                            idSiswa.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshotIdSiswa: DataSnapshot) {
                                    if (dataSnapshotIdSiswa.exists()) {
                                        Database.database.getReference("pembayaran/${key}/id_siswa").setValue(dataSnapshotIdSiswa.value)
                                    }
                                }
                                override fun onCancelled(databaseError: DatabaseError) {}
                            })

                            goToLes()
                        }
                    }
                }
            }
    }

    private fun hapusLes() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("yakin ingin menghapus les ${intent.getStringExtra(EXTRA_NAMALES)}?")
        builder.setPositiveButton("Hapus") { p0,p1 ->
            val biaya = Database.database.getReference("pembayaran").orderByChild("id_lessiswa").equalTo(intent.getStringExtra(EXTRA_IDLESSISWA))
            biaya.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (h in dataSnapshot.children) {
                            if (h.child("sudah_dikonfirmasi").value == false) {
                                Database.database.getReference("pembayaran/${h.key}").removeValue()
                                Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}").removeValue()
                                FirebaseStorage.getInstance().reference.child("bukti_bayar/${intent.getStringExtra(EXTRA_IDLESSISWA)}").delete()
                                Database.database.getReference("jumlah_data/pembayaran").setValue(ServerValue.increment(-1))

                                Toast.makeText(applicationContext, "data berhasil dihapus", Toast.LENGTH_SHORT).show()
                                goToLes()
                            } else {
                                Toast.makeText(applicationContext, "tidak dapat menghapus les terverifikasi", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
        builder.setNegativeButton("Batal") { p0,p1 -> }
        builder.show()
    }

    private fun goToLes() {
        Intent(this, LesActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(it)
        }
    }
}