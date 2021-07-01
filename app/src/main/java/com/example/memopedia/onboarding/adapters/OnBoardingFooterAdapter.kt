package com.example.memopedia.onboarding.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.memopedia.databinding.OnboardingFooterItemBinding
import com.example.memopedia.onboarding.data.FooterMetaData

class OnBoardingFooterAdapter : RecyclerView.Adapter<OnBoardingFooterAdapter.FooterViewHolder>() {

    inner class FooterViewHolder(private val binding: OnboardingFooterItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val root = binding.footerRoot

        fun bind(footerMetaData: FooterMetaData) {

            root.visibility =

                if (footerMetaData.shouldShow)
                    View.VISIBLE
                else
                    View.GONE
        }
    }

    private val RECYCLER_COMPARATOR = object : DiffUtil.ItemCallback<FooterMetaData>() {
        override fun areItemsTheSame(oldItem: FooterMetaData, newItem: FooterMetaData) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: FooterMetaData, newItem: FooterMetaData) =
            oldItem == newItem
    }

    val footerDiffer = AsyncListDiffer(this, RECYCLER_COMPARATOR)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FooterViewHolder {

        val binding = OnboardingFooterItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return FooterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FooterViewHolder, position: Int) {

        if (position < 1) {

            val footer = footerDiffer.currentList[position]
            holder.bind(footer)
        }
    }

    override fun getItemCount(): Int = footerDiffer.currentList.size
}