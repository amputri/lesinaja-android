package com.lesinaja.les.ui.walimurid.les

import android.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.lesinaja.les.base.Autentikasi
import com.lesinaja.les.base.Database
import com.lesinaja.les.base.umum.Wilayah
import com.lesinaja.les.base.walimurid.LesKey
import com.lesinaja.les.databinding.ActivityLesBinding

class LesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLesBinding
    private lateinit var lesList : MutableList<LesKey>

    var idSiswa: String = ""
    var biayaDaftar: String = ""
    var namaSiswa: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lesList = mutableListOf()

        setSiswaAdapter()

        binding.btnAmbilLes.setOnClickListener {
            if (idSiswa != "0") {
                goToTambahLes()
            } else {
                Toast.makeText(this, "pilih siswa terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToTambahLes() {
        Intent(this, TambahLesActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            it.putExtra(TambahLesActivity.EXTRA_IDSISWA, idSiswa)
            it.putExtra(TambahLesActivity.EXTRA_BIAYADAFTAR, biayaDaftar)
            it.putExtra(TambahLesActivity.EXTRA_NAMASISWA, namaSiswa)
            startActivity(it)
        }
    }

    private fun setListView() {
        lesList.clear()
        binding.lvLes.adapter = null

        val ref = Database.database.getReference("les_siswa").orderByChild("id_siswa").equalTo(idSiswa)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (h in snapshot.children) {
                        var waktuMulai: Array<Long> = arrayOf()

                        val gantiTutor = Database.database.getReference("les_gantitutor/${h.key}/waktu_mulai")
                        gantiTutor.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshotGantiTutor: DataSnapshot) {
                                if (snapshotGantiTutor.exists()) {
                                    for (j in 0 until snapshotGantiTutor.childrenCount) {
                                        waktuMulai = waktuMulai.plus(snapshotGantiTutor.child("${j}").value.toString().toLong())
                                    }

                                    val les = LesKey(
                                        h.key!!,
                                        h.child("gaji_tutor").value.toString().toInt(),
                                        h.child("id_les").value.toString(),
                                        h.child("id_siswa").value.toString(),
                                        h.child("preferensi_tutor").value.toString(),
                                        waktuMulai
                                    )
                                    if (les != null) {
                                        lesList.add(les)
                                    }

                                    val adapter = LesAdapter(this@LesActivity, com.lesinaja.les.R.layout.item_les, lesList)
                                    binding.lvLes.adapter = adapter
                                } else {
                                    for (j in 0 until h.child("waktu_mulai").childrenCount) {
                                        waktuMulai = waktuMulai.plus(h.child("waktu_mulai/${j}").value.toString().toLong())
                                    }

                                    val les = LesKey(
                                        h.key!!,
                                        h.child("gaji_tutor").value.toString().toInt(),
                                        h.child("id_les").value.toString(),
                                        h.child("id_siswa").value.toString(),
                                        h.child("preferensi_tutor").value.toString(),
                                        waktuMulai
                                    )
                                    if (les != null) {
                                        lesList.add(les)
                                    }

                                    val adapter = LesAdapter(this@LesActivity, com.lesinaja.les.R.layout.item_les, lesList)
                                    binding.lvLes.adapter = adapter
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

    private fun setSiswaAdapter() {
        var siswa = ArrayList<Wilayah>()
        siswa.add(Wilayah("0", "pilih siswa"))

        val ref = Database.database.getReference("siswa").orderByChild("id_walimurid").equalTo("${Autentikasi.auth.currentUser?.uid}")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (h in dataSnapshot.children) {
                    siswa.add(Wilayah("${h.key}//${h.child("biaya_daftar").value}", h.child("nama").getValue() as String))
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        binding.spinSiswa.adapter = ArrayAdapter(
            this,
            R.layout.simple_list_item_1,
            siswa
        )

        binding.spinSiswa.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val selectedObject = binding.spinSiswa.selectedItem as Wilayah
                idSiswa = selectedObject.id.substringBefore("//")
                biayaDaftar = selectedObject.id.substringAfter("//")
                namaSiswa = selectedObject.nama

                setListView()
            }
        }
    }
}