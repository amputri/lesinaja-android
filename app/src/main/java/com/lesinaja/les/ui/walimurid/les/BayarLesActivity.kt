package com.lesinaja.les.ui.walimurid.les

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfDocument
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.lesinaja.les.R
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.databinding.ActivityBayarLesBinding
import com.lesinaja.les.ui.header.LoadingDialog
import com.lesinaja.les.ui.header.ToolbarFragment
import com.lesinaja.les.ui.tutor.lowongan.DetailLowonganActivity
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class BayarLesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBayarLesBinding
    private lateinit var imageBitmap: Bitmap
    private var keyPembayaran = ""

    var logo: Bitmap? = null
    var scaleBitmap: Bitmap? = null
    var pageWidth = 1200

    companion object {
        const val EXTRA_IDLESSISWA = "id_les_siswa"
        const val EXTRA_NAMASISWA = "nama_siswa"
        const val EXTRA_NAMALES = "nama_les"
        const val EXTRA_JUMLAHPERTEMUAN = "jumlah_pertemuan"
        const val EXTRA_NAMATUTOR = "nama_tutor"
        const val REQUEST_GALLERY = 100
        const val REQUEST_CAMERA = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBayarLesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvPdfTanggal.text = "tagihan belum dibayar"

        binding.btnKembali.setOnClickListener {
            goToLes()
        }
        setToolbar("Bayar Les")

        updateUI()
        loadAlamat()
        loadDataPribadiWaliMurid()

        binding.btnTambahLes.setEnabled(false)

        binding.ivBukti.setOnClickListener {
            if (binding.textUnggah.text.toString() == "pembayaran sudah diverifikasi admin") {
                Toast.makeText(this, "pembayaran sudah diverifikasi, tidak dapat diubah", Toast.LENGTH_SHORT).show()
            } else if (intent.getStringExtra(EXTRA_NAMATUTOR).toString() == "Lihat Tutor Pelamar") {
                Toast.makeText(this, "belum ada/pilih tutor", Toast.LENGTH_SHORT).show()
            } else {
                openPhotoDialog()
            }
        }

        binding.btnTambahLes.setOnClickListener {
            if (binding.idAdminSiswa.text != "") {
                uploadBuktiPembayaran(imageBitmap, intent.getStringExtra(EXTRA_IDLESSISWA).toString())
            }
        }

        binding.btnHapus.setOnClickListener {
            hapusLes()
        }

        binding.btnInvoice.setOnClickListener {
            logo = BitmapFactory.decodeResource(resources, R.drawable.logoawal)
            scaleBitmap = Bitmap.createScaledBitmap(logo!!, 390, 150, false)

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PackageManager.PERMISSION_GRANTED)
            createPDF()
        }
    }

    private fun setToolbar(judul: String) {
        val toolbarFragment = ToolbarFragment()
        val bundle = Bundle()

        bundle.putString("judul", judul)
        toolbarFragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(binding.header.id, toolbarFragment).commit()
    }

    private fun updateUI() {
        binding.textSiswa.text = "Siswa: ${intent.getStringExtra(EXTRA_NAMASISWA)}"
        binding.tvLes.text = "${intent.getStringExtra(EXTRA_NAMALES)} (${intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN)}x)"

        getPembayaran()
        loadJadwal()
        loadPreferensiTutor()
        getIdAdminSiswa()
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
                                                binding.tvBiayaDaftarRupiah.text = "Rp ${NumberFormat.getNumberInstance(Locale("in", "ID")).format(dataSnapshotSiswaDaftar.value.toString().toInt())}"
                                                getBiayaBayar()
                                            }
                                        }
                                        override fun onCancelled(databaseError: DatabaseError) {}
                                    })
                                } else {
                                    binding.tvBiayaDaftar.text = "-"
                                    binding.tvBiayaDaftarRupiah.text = "-"
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
                    binding.tvBiayaLesRupiah.text = "Rp ${NumberFormat.getNumberInstance(Locale("in", "ID")).format(dataSnapshotLes.value.toString().toInt())}"
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
                            binding.tvBiayaDaftarRupiah.text = "Rp ${NumberFormat.getNumberInstance(Locale("in", "ID")).format(h.child("biaya_daftar").value.toString().toInt())}"
                        } else {
                            binding.tvBiayaDaftar.text = "-"
                            binding.tvBiayaDaftarRupiah.text = "-"
                        }

                        binding.tvBiayaLes.text = h.child("biaya_les").value.toString()
                        binding.tvBiayaLesRupiah.text = "Rp ${NumberFormat.getNumberInstance(Locale("in", "ID")).format(h.child("biaya_les").value.toString().toInt())}"

                        if (h.child("sudah_dikonfirmasi").value.toString() == "true") {
                            binding.textUnggah.text = "pembayaran sudah diverifikasi admin"
                            binding.tvPdfTanggal.text = "tagihan sudah dibayar pada ${SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm aaa").format(h.child("waktu_transfer").value.toString().toLong())}"
                            binding.btnHapus.setEnabled(false)
                        } else {
                            binding.tvPdfTanggal.text = "pembayaran menunggu verifikasi admin"
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
            binding.tvTotal.text = "Rp ${NumberFormat.getNumberInstance(Locale("in", "ID")).format(binding.tvBiayaLes.text.toString().toInt())}"
        } else {
            val total = binding.tvBiayaLes.text.toString().toInt() + binding.tvBiayaDaftar.text.toString().toInt()
            binding.tvTotal.text = "Rp ${NumberFormat.getNumberInstance(Locale("in", "ID")).format(total)}"
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

    private fun getIdAdminSiswa() {
        val admin = Database.database.getReference("user").orderByChild("roles/admin").equalTo(true)
        admin.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotAdmin: DataSnapshot) {
                if (dataSnapshotAdmin.exists()) {
                    for (h in dataSnapshotAdmin.children) {
                        val idSiswa = Database.database.getReference("les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}/id_siswa")
                        idSiswa.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshotIdSiswa: DataSnapshot) {
                                if (dataSnapshotIdSiswa.exists()) {
                                    binding.idAdminSiswa.text = "${h.key}//${dataSnapshotIdSiswa.value.toString()}"
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun uploadBuktiPembayaran(imageBitmap: Bitmap, keyLes: String) {
        val loading = LoadingDialog(this@BayarLesActivity)
        loading.startLoading()

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
                            val updates: MutableMap<String, Any> = HashMap()

                            if (binding.textUnggah.text.toString() == "Unggah Bukti Pembayaran berikut") {
                                keyPembayaran = Database.database.getReference("pembayaran").push().key!!
                                updates["jumlah_data/pembayaran"] = ServerValue.increment(1)
                                updates["pembayaran/${keyPembayaran}/id_penerima"] = binding.idAdminSiswa.text.toString().substringBefore("//")
                                updates["pembayaran/${keyPembayaran}/id_pengirim"] = Autentikasi.auth.currentUser?.uid!!
                                updates["pembayaran/${keyPembayaran}/id_siswa"] = binding.idAdminSiswa.text.toString().substringAfter("//")
                                updates["pembayaran/${keyPembayaran}/id_lessiswa"] = intent.getStringExtra(EXTRA_IDLESSISWA).toString()
                                updates["pembayaran/${keyPembayaran}/biaya_les"] = binding.tvBiayaLes.text.toString().toInt()

                                if (binding.tvBiayaDaftar.text.toString() != "-") {
                                    updates["pembayaran/${keyPembayaran}/biaya_daftar"] = binding.tvBiayaDaftar.text.toString().toInt()
                                }

                                updates["pembayaran/${keyPembayaran}/sudah_dikonfirmasi"] = false
                            } else if (binding.textUnggah.text.toString() != "pembayaran sudah diverifikasi admin") {
                                keyPembayaran = binding.textUnggah.text.toString().substringBefore(" :").substringAfter("pembayaran ")
                            }

                            updates["pembayaran/${keyPembayaran}/bukti"] = it.toString()
                            updates["pembayaran/${keyPembayaran}/waktu_transfer"] = System.currentTimeMillis()

                            Database.database.reference.updateChildren(updates)
                                .addOnSuccessListener {
                                    loading.isDismiss()
                                    Toast.makeText(this, "berhasil unggah pembayaran", Toast.LENGTH_SHORT).show()
                                    goToLes()
                                }
                                .addOnFailureListener {
                                    loading.isDismiss()
                                    Toast.makeText(this, "gagal unggah pembayaran", Toast.LENGTH_SHORT).show()
                                    goToLes()
                                }
                        }
                    }
                }
            }
    }

    private fun hapusLes() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("yakin ingin menghapus les ${intent.getStringExtra(EXTRA_NAMALES)}?")
        builder.setPositiveButton("Hapus") { p0,p1 ->
            if (binding.textUnggah.text.toString() != "pembayaran sudah diverifikasi admin") {
                val delete: MutableMap<String, Any?> = HashMap()
                delete["les_siswa/${intent.getStringExtra(EXTRA_IDLESSISWA)}"] = null
                delete["siswa/${binding.idAdminSiswa.text.toString().substringAfter("//")}/jumlah_les"] = ServerValue.increment(-1)
                delete["jumlah_data/les_siswa"] = ServerValue.increment(-1)

                keyPembayaran = binding.textUnggah.text.toString().substringAfter("belum dikonfirmasi admin, ")
                if (keyPembayaran == "dapat diubah kembali") {
                    delete["pembayaran/${binding.textUnggah.text.toString().substringBefore(" :").substringAfter("pembayaran ")}"] = null
                    delete["jumlah_data/pembayaran"] = ServerValue.increment(-1)
                }

                val loading = LoadingDialog(this@BayarLesActivity)
                loading.startLoading()

                Database.database.reference.updateChildren(delete)
                    .addOnSuccessListener {
                        if (keyPembayaran == "dapat diubah kembali") {
                            FirebaseStorage.getInstance().reference.child("bukti_bayar/${intent.getStringExtra(EXTRA_IDLESSISWA)}").delete()
                        }
                        loading.isDismiss()
                        Toast.makeText(applicationContext, "data berhasil dihapus", Toast.LENGTH_SHORT).show()
                        goToLes()
                    }
            } else {
                Toast.makeText(applicationContext, "tidak dapat menghapus les terverifikasi", Toast.LENGTH_SHORT).show()
            }
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

    private fun loadAlamat() {
        val waliMurid = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid!!}/kontak")
        waliMurid.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotWaliMurid: DataSnapshot) {
                if (dataSnapshotWaliMurid.exists()) {
                    var idDesa = dataSnapshotWaliMurid.child("id_desa").value.toString()
                    var idKecamatan = dataSnapshotWaliMurid.child("id_desa").value.toString().substring(0,7)
                    var idKabupaten = dataSnapshotWaliMurid.child("id_desa").value.toString().substring(0,4)
                    var idProvinsi = dataSnapshotWaliMurid.child("id_desa").value.toString().substring(0,2)

                    val desa = Database.database.getReference("wilayah_desa/${idProvinsi}/${idKabupaten}/${idKecamatan}/${idDesa}/nama")
                    desa.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshotDesa: DataSnapshot) {
                            if (dataSnapshotDesa.exists()) {
                                val kecamatan = Database.database.getReference("wilayah_kecamatan/${idProvinsi}/${idKabupaten}/${idKecamatan}/nama")
                                kecamatan.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshotKecamatan: DataSnapshot) {
                                        if (dataSnapshotKecamatan.exists()) {
                                            val kabupaten = Database.database.getReference("wilayah_kabupaten/${idProvinsi}/${idKabupaten}/nama")
                                            kabupaten.addValueEventListener(object : ValueEventListener {
                                                override fun onDataChange(dataSnapshotKabupaten: DataSnapshot) {
                                                    if (dataSnapshotKabupaten.exists()) {
                                                        val provinsi = Database.database.getReference("wilayah_provinsi/${idProvinsi}/nama")
                                                        provinsi.addValueEventListener(object : ValueEventListener {
                                                            override fun onDataChange(dataSnapshotProvinsi: DataSnapshot) {
                                                                if (dataSnapshotProvinsi.exists()) {
                                                                    binding.tvPdfAlamat.text = "${dataSnapshotWaliMurid.child("alamat_rumah").value}"
                                                                    binding.tvPdfKecamatan.text = "${dataSnapshotDesa.value}, ${dataSnapshotKecamatan.value}"
                                                                    binding.tvPdfKabupaten.text = "${dataSnapshotKabupaten.value}, ${dataSnapshotProvinsi.value}"
                                                                    binding.tvPdfTelepon.text = "${dataSnapshotWaliMurid.child("telepon").value}"
                                                                }
                                                            }
                                                            override fun onCancelled(error: DatabaseError) {}
                                                        })
                                                    }
                                                }
                                                override fun onCancelled(error: DatabaseError) {}
                                            })
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {}
                                })
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadDataPribadiWaliMurid() {
        val nama = Database.database.getReference("user/${Autentikasi.auth.currentUser?.uid!!}/nama")
        nama.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshotNama: DataSnapshot) {
                if (dataSnapshotNama.exists()) {
                    binding.tvPdfWalmur.text = dataSnapshotNama.value.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    @SuppressLint("NewApi")
    private fun createPDF() {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(1200, 2010, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        canvas.drawBitmap(scaleBitmap!!, 400f, 30f, paint)
        paint.color = Color.BLACK
        paint.textSize = 30f
        paint.textAlign = Paint.Align.RIGHT
        titlePaint.textAlign = Paint.Align.CENTER
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.color = Color.BLACK
        titlePaint.textSize = 50f
        canvas.drawText("${intent.getStringExtra(EXTRA_IDLESSISWA)}", (pageWidth / 2).toFloat(), 250f, titlePaint)
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        titlePaint.textSize = 35f
        canvas.drawText("${binding.tvPdfTanggal.text}", (pageWidth / 2).toFloat(), 310f, titlePaint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Bimbel : LesinAja", (pageWidth - 97).toFloat(), 430f, paint)
        canvas.drawText("081242306969", (pageWidth - 97).toFloat(), 470f, paint)
        canvas.drawText("Perum. Graha Kuncara L 22", (pageWidth - 97).toFloat(), 512f, paint)
        canvas.drawText("Kemiri - Sidoarjo - Jawa Timur" + "", (pageWidth - 97).toFloat(), 562f, paint)

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Wali Murid : " + "${binding.tvPdfWalmur.text}", 20f, 430f, paint)
        canvas.drawText("Siswa : " + "${intent.getStringExtra(EXTRA_NAMASISWA)}", 20f, 470f, paint)
        canvas.drawText("${binding.tvPdfTelepon.text}", 20f, 512f, paint)
        canvas.drawText("${binding.tvPdfAlamat.text}", 20f, 562f, paint)
        canvas.drawText("${binding.tvPdfKecamatan.text}", 20f, 612f, paint)
        canvas.drawText("${binding.tvPdfKabupaten.text}", 20f, 662f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRect(20f, 780f, (pageWidth - 20).toFloat(), 860f, paint)
        paint.textAlign = Paint.Align.LEFT
        paint.style = Paint.Style.FILL
        canvas.drawText("No.", 40f, 830f, paint)
        canvas.drawText("Pembayaran", 200f, 830f, paint)
        canvas.drawText("Jumlah Pertemuan", 500f, 830f, paint)
        canvas.drawText("Harga", 900f, 830f, paint)
        canvas.drawLine(180f, 790f, 180f, 840f, paint)
        canvas.drawLine(480f, 790f, 480f, 840f, paint)
        canvas.drawLine(880f, 790f, 880f, 840f, paint)

        canvas.drawText("1.", 40f, 950f, paint)
        canvas.drawText("Pendaftaran Les", 200f, 950f, paint)
        canvas.drawText("", 700f, 950f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${binding.tvBiayaDaftarRupiah.text}", (pageWidth - 40).toFloat(), 950f, paint)
        paint.textAlign = Paint.Align.LEFT

        canvas.drawText("2.", 40f, 1050f, paint)
        canvas.drawText("Les ${intent.getStringExtra(EXTRA_NAMALES)}", 200f, 1050f, paint)
        canvas.drawText("${intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN)}", 700f, 1050f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${binding.tvBiayaLesRupiah.text}", (pageWidth - 40).toFloat(), 1050f, paint)
        paint.textAlign = Paint.Align.LEFT

        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.rgb(249,174,48)
        canvas.drawRect(680f, 1150f, (pageWidth - 20).toFloat(), 1250f, paint)
        paint.color = Color.BLACK
        paint.textSize = 50f
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Total", 700f, 1210f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${binding.tvTotal.text}", (pageWidth - 40).toFloat(), 1210f, paint)

        pdfDocument.finishPage(page)

        val file = File(Environment.getExternalStorageDirectory(), "/LesinAja-${intent.getStringExtra(EXTRA_IDLESSISWA)}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        pdfDocument.close()
        Toast.makeText(this@BayarLesActivity, "PDF sudah dibuat", Toast.LENGTH_LONG).show()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            FileProvider.getUriForFile(
                this@BayarLesActivity,
                this@BayarLesActivity.applicationContext.packageName + ".provider",
                file
            ), "application/pdf"
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }
}