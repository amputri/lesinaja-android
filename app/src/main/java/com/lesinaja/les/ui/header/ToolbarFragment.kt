package com.lesinaja.les.ui.header

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lesinaja.les.databinding.FragmentToolbarBinding

class ToolbarFragment : Fragment() {
	private var _binding: FragmentToolbarBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = FragmentToolbarBinding.inflate(inflater, container, false)
		val root: View = binding.root

		val bundle = arguments
		bundle?.getString("judul")?.let { setJudul(it) }

		return root
	}

	fun setJudul(judul: String) {
		binding.tvJudul.text = judul
	}
}