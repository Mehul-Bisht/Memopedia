package com.example.memopedia.ui.memes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.memopedia.R
import com.example.memopedia.adapters.BaseAdapter
import com.example.memopedia.databinding.FragmentMemesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MemesFragment : Fragment(R.layout.fragment_memes) {

    private var _binding: FragmentMemesBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<MemesViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMemesBinding.bind(view)
        val adapter = BaseAdapter(requireContext())
        binding.recyclerview.adapter = adapter

        lifecycleScope.launchWhenStarted {

            viewModel.monitorEvents().collectLatest { response ->

                withContext(Dispatchers.Main) {

                    adapter.differ.submitList(response.memes)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}