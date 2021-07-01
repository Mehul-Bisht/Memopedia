package com.example.memopedia.ui.shared

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.example.memopedia.R
import com.example.memopedia.databinding.FragmentSharedBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SharedFragment : Fragment(R.layout.fragment_shared) {

    private var _binding: FragmentSharedBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSharedBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}