package com.example.memopedia.onboarding.adapters

import android.R
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.memopedia.databinding.OnboardingHeaderItemBinding
import com.example.memopedia.onboarding.data.HeaderMetaData
import com.example.memopedia.onboarding.getActiveStateFlow
import com.example.memopedia.onboarding.getQueryTextChangeStateFlow
import kotlinx.coroutines.flow.StateFlow


class OnBoardingHeaderAdapter(
    private val context: Context,
) : RecyclerView.Adapter<OnBoardingHeaderAdapter.HeaderViewHolder>() {

    private var queryTextListener: ((StateFlow<String>) -> Unit)? = null
    private var searchViewListener: ((StateFlow<Boolean>) -> Unit)? = null

    inner class HeaderViewHolder(binding: OnboardingHeaderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val root = binding.headerRoot
        val search = binding.search

        fun bind(headerMetaData: HeaderMetaData) {

            root.visibility =

                if (headerMetaData.shouldShow)
                    View.VISIBLE
                else
                    View.GONE

            val searchEditText: EditText =
                search.findViewById(androidx.appcompat.R.id.search_src_text)

            searchEditText.setHintTextColor(context.resources.getColor(R.color.white))
            searchEditText.setTextColor(context.resources.getColor(R.color.white))
        }
    }

    fun setQueryTextListener(listener: (StateFlow<String>) -> Unit) {

        this.queryTextListener = listener
    }

    fun setSearchViewListener(listener: (StateFlow<Boolean>) -> Unit) {

        this.searchViewListener = listener
    }

    private val RECYCLER_COMPARATOR = object : DiffUtil.ItemCallback<HeaderMetaData>() {
        override fun areItemsTheSame(oldItem: HeaderMetaData, newItem: HeaderMetaData) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: HeaderMetaData, newItem: HeaderMetaData) =
            oldItem == newItem
    }

    val headerDiffer = AsyncListDiffer(this, RECYCLER_COMPARATOR)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {

        val binding = OnboardingHeaderItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return HeaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {

        //holder.setIsRecyclable(false)

        if (position < 1) {

            val header = headerDiffer.currentList[position]
            holder.bind(header)
        }

        queryTextListener?.let {

            it(holder.search.getQueryTextChangeStateFlow() {

                val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                val view: View  = holder.search

                imm.hideSoftInputFromWindow(view.windowToken,0)
            })
        }

        searchViewListener?.let {

            it(holder.search.getActiveStateFlow())
        }
    }

    override fun getItemCount(): Int = headerDiffer.currentList.size

}