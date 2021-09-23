package com.lesinaja.les.ui.walimurid.les.presensi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.R
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.walimurid.presensi.Presensi
import com.lesinaja.les.databinding.ActivityPresensiBinding
import com.lesinaja.les.ui.header.ToolbarFragment
import com.lesinaja.les.ui.walimurid.les.LesActivity
import com.lesinaja.les.ui.walimurid.les.PerpanjangLesActivity
import java.util.*

class PresensiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPresensiBinding
    private lateinit var presensiList : MutableList<Presensi>

    companion object {
        const val EXTRA_IDLESSISWA = "id_les_siswa"
        const val EXTRA_NAMASISWA = "nama_siswa"
        const val EXTRA_NAMALES = "nama_les"
        const val EXTRA_JUMLAHPERTEMUAN = "jumlah_pertemuan"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresensiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnKembali.setOnClickListener {
            onBackPressed()
        }
        setToolbar("Presensi Les")

        presensiList = mutableListOf()

        updateUI()

        binding.btnPerpanjangLes.setEnabled(false)
        binding.btnPerpanjangLes.setOnClickListener {
            goToPerpanjangLes()
        }

        setListView()
    }

    private fun setToolbar(judul: String) {
        val toolbarFragment = ToolbarFragment()
        val bundle = Bundle()

        bundle.putString("judul", judul)
        toolbarFragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(binding.header.id, toolbarFragment).commit()
    }

    private fun updateUI() {
        binding.tvNamaSiswa.text = "Siswa: ${intent.getStringExtra(EXTRA_NAMASISWA)}"
        binding.tvNamaLes.text = "Les: ${intent.getStringExtra(EXTRA_NAMALES)}"
        binding.tvJumlahPertemuan.text = "Jumlah Pertemuan: ${intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN)}"
    }

    private fun setListView() {
        val ref = Database.database.getReference("les_siswatutor").orderByChild("id_lessiswa").equalTo(intent.getStringExtra(EXTRA_IDLESSISWA))
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    presensiList.clear()
                    var jumlahLaporan = 0

                    for (h in snapshot.children) {
                        var sudahLaporan = true

                        val presensi = Database.database.getReference("les_presensi/${h.key}").orderByChild("waktu")
                        presensi.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshotPresensi: DataSnapshot) {
                                if (snapshotPresensi.exists()) {
                                    for (i in snapshotPresensi.children) {
                                        val presensiObject = Presensi(
                                            h.key!!,
                                            h.child("id_lessiswa").value.toString(),
                                            binding.tvNamaSiswa.text.toString(),
                                            binding.tvNamaLes.text.toString(),
                                            binding.tvJumlahPertemuan.text.toString(),
                                            h.child("id_tutor").value.toString(),
                                            i.key!!,
                                            i.child("waktu").value.toString().toLong(),
                                            sudahLaporan
                                        )
                                        if (i.child("sudah_laporan").value.toString() == "false") {
                                            sudahLaporan = false
                                        } else if (i.child("sudah_laporan").value.toString() == "true") {
                                            sudahLaporan = true
                                            jumlahLaporan += 1
                                        }

                                        if (jumlahLaporan == intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN).toString().toInt()) {
                                            binding.btnPerpanjangLes.setEnabled(true)
                                        }

                                        if (presensiObject != null) {
                                            presensiList.add(presensiObject)
                                        }
                                    }

                                    binding.lvPresensi.adapter = PresensiAdapter(this@PresensiActivity, R.layout.item_presensi, presensiList)
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun goToPerpanjangLes() {
        Intent(this, PerpanjangLesActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(PerpanjangLesActivity.EXTRA_IDLESSISWA, intent.getStringExtra(EXTRA_IDLESSISWA))
            it.putExtra(PerpanjangLesActivity.EXTRA_NAMASISWA, intent.getStringExtra(EXTRA_NAMASISWA).toString().substringAfter("Siswa: "))
            it.putExtra(PerpanjangLesActivity.EXTRA_NAMALES, intent.getStringExtra(EXTRA_NAMALES).toString().substringAfter("Les: "))
            it.putExtra(PerpanjangLesActivity.EXTRA_JUMLAHPERTEMUAN, intent.getStringExtra(EXTRA_JUMLAHPERTEMUAN).toString().substringAfter("Jumlah Pertemuan: "))
            startActivity(it)
        }
    }
}