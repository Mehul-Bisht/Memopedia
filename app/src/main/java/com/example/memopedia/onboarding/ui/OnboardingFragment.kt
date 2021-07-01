package com.example.memopedia.onboarding.ui

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memopedia.R
import com.example.memopedia.databinding.FragmentOnboardingBinding
import com.example.memopedia.onboarding.adapters.OnBoardingAdapter
import com.example.memopedia.onboarding.adapters.OnBoardingFooterAdapter
import com.example.memopedia.onboarding.adapters.OnBoardingHeaderAdapter
import com.example.memopedia.onboarding.adapters.OnBoardingItemCountAdapter
import com.example.memopedia.onboarding.data.FooterMetaData
import com.example.memopedia.onboarding.data.HeaderMetaData
import com.example.memopedia.onboarding.data.ItemCountMetaData
import com.example.memopedia.onboarding.datastore.DataStorePreferenceStorage
import com.example.memopedia.onboarding.ui.OnBoardingViewModel.OnBoardingState.*
import com.example.memopedia.ui.HomeActivity
import com.facebook.shimmer.ShimmerFrameLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    @Inject
    lateinit var datastore: DataStorePreferenceStorage

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private var recyclerViewState: Parcelable? = null

    private val viewModel by activityViewModels<OnBoardingViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentOnboardingBinding.bind(view)

        binding.shimmerFrameLayout.startShimmerAnimation()

        val headerAdapter = OnBoardingHeaderAdapter(requireContext())
        val itemCountAdapter = OnBoardingItemCountAdapter() { subreddits ->

            lifecycleScope.launchWhenStarted {

                datastore.save(true)
                val intent = Intent(requireContext(), HomeActivity::class.java)
                intent.putStringArrayListExtra("subreddits",subreddits)
                startActivity(intent)
            }
        }
        val onBoardingAdapter = OnBoardingAdapter(requireContext())
        onBoardingAdapter.setOnItemClick { categoryItem ->
            viewModel.toggleSelectedState(categoryItem)
        }
        val footerAdapter = OnBoardingFooterAdapter()

        onBoardingAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        val concatAdapter = ConcatAdapter(headerAdapter, itemCountAdapter, onBoardingAdapter, footerAdapter)
        binding.onboardRecyclerview.adapter = concatAdapter
        binding.onboardRecyclerview.itemAnimator = null

        viewModel.fetchCategories()

        lifecycleScope.launchWhenStarted {

            async {

                headerAdapter.setQueryTextListener { textChangedFlow ->

                    lifecycleScope.launchWhenStarted {

                        textChangedFlow
                            .debounce(300)
                            .filter { query ->

                                return@filter query.isNotEmpty()
                            }
                            .distinctUntilChanged()
                            .flatMapLatest { query ->
                                flow {
                                    emit(query)
                                }
                            }
                            .collect { result ->
                                viewModel.search(result)
                            }
                    }
                }
            }

            async {

                headerAdapter.setSearchViewListener { searchActiveFlow ->

                    lifecycleScope.launchWhenStarted {

                        searchActiveFlow
                            .debounce(300)
                            .collect { isActive ->

                                viewModel.toggleSearchViewState(isActive)
                            }
                    }
                }
            }

            async {

                viewModel.godFlow.collect { state ->

                    when (state) {

                        is Success -> {

                            binding.apply {

                                shimmerFrameLayout.visibility = View.GONE
                                loadingPlaceholder.visibility = View.GONE
                                onboardRecyclerview.visibility = View.VISIBLE
                            }

                            recyclerViewState = binding.onboardRecyclerview.layoutManager?.onSaveInstanceState()

                            headerAdapter.headerDiffer.submitList(listOf(HeaderMetaData(true, 0)))
                            footerAdapter.footerDiffer.submitList(listOf(FooterMetaData(true, 0)))
                            onBoardingAdapter.asyncListDiffer.submitList(state.data)
                            binding.onboardRecyclerview.layoutManager?.onRestoreInstanceState(recyclerViewState)

                            if (state.shouldScroll) {
                                binding.onboardRecyclerview.scrollToPosition(0)
                            }
                        }

                        is Loading -> {

                            binding.apply {

                                shimmerFrameLayout.visibility = View.VISIBLE
                                loadingPlaceholder.visibility = View.VISIBLE
                                onboardRecyclerview.visibility = View.GONE
                            }

                            headerAdapter.headerDiffer.submitList(listOf(HeaderMetaData(false, 0)))
                            footerAdapter.footerDiffer.submitList(listOf(FooterMetaData(false, 0)))
                        }

                        is Error -> {

                            binding.apply {

                                shimmerFrameLayout.visibility = View.GONE
                                loadingPlaceholder.visibility = View.GONE
                            }

                            headerAdapter.headerDiffer.submitList(listOf(HeaderMetaData(false, 0)))
                            footerAdapter.footerDiffer.submitList(listOf(FooterMetaData(false, 0)))
                        }

                        is None -> {

                            binding.apply {

                                shimmerFrameLayout.visibility = View.VISIBLE
                                loadingPlaceholder.visibility = View.VISIBLE
                                onboardRecyclerview.visibility = View.GONE
                            }

                            headerAdapter.headerDiffer.submitList(listOf(HeaderMetaData(false, 0)))
                            footerAdapter.footerDiffer.submitList(listOf(FooterMetaData(false, 0)))
                        }
                    }
                }
            }

            async {

                viewModel.selectedCount.collect { pair ->

                    itemCountAdapter.counterDiffer.submitList(
                        listOf(
                            ItemCountMetaData(
                                id = pair.first,
                                count = pair.first,
                                subreddits = pair.second
                            )
                        )
                    )
                }
            }

            async {

                viewModel.onBoardingtags.collect {

                    Log.d("tag size ", "${it.size}")
                }
            }

            async {

                viewModel.tagsMetaData.collect {

                    Log.d("tag metadata size ", "${it.size}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding.shimmerFrameLayout.stopShimmerAnimation()
        _binding = null
    }
}