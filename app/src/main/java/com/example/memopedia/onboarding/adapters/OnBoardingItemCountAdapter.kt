package com.example.memopedia.onboarding.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.memopedia.databinding.OnboardingFooterItemBinding
import com.example.memopedia.databinding.OnboardingItemCountBinding
import com.example.memopedia.onboarding.data.FooterMetaData
import com.example.memopedia.onboarding.data.ItemCountMetaData
import com.example.memopedia.onboarding.ui.OnBoardingViewModel

class OnBoardingItemCountAdapter(
    private val onProceed: (ArrayList<String>) -> Unit
) : RecyclerView.Adapter<OnBoardingItemCountAdapter.CounterViewHolder>() {

    inner class CounterViewHolder(private val binding: OnboardingItemCountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val counter = binding.itemCount
        private val proceed = binding.proceed

        fun bind(itemCountMetaData: ItemCountMetaData) {

            proceed.setOnClickListener {

                val list: ArrayList<String> = arrayListOf()

                val map = itemCountMetaData.subreddits
                val keys = map.keys

                for (key in keys) {

                    if (map[key] == OnBoardingViewModel.State.SELECTED) {

                        list.add(key)
                    }
                }

                onProceed(list)
            }

            var message = "No items Selected, go select!"

            message = when (itemCountMetaData.count) {
                0 -> {
                    "No items Selected, go select!"
                }
                1 -> {
                    "1 item selected"
                }
                else -> {
                    "${itemCountMetaData.count} items selected"
                }
            }

            counter.text = message
        }
    }

    private val RECYCLER_COMPARATOR = object : DiffUtil.ItemCallback<ItemCountMetaData>() {
        override fun areItemsTheSame(oldItem: ItemCountMetaData, newItem: ItemCountMetaData) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ItemCountMetaData, newItem: ItemCountMetaData) =
            oldItem == newItem
    }

    val counterDiffer = AsyncListDiffer(this, RECYCLER_COMPARATOR)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CounterViewHolder {

        val binding = OnboardingItemCountBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return CounterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CounterViewHolder, position: Int) {

        if (position < 1) {

            val footer = counterDiffer.currentList[position]
            holder.bind(footer)
        }
    }

    override fun getItemCount(): Int = counterDiffer.currentList.size
}