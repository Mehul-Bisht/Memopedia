package com.example.memopedia.onboarding.adapters

import android.content.Context
import android.os.Parcelable
import android.util.Log
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memopedia.databinding.OnboardingListItemBinding
import com.example.memopedia.onboarding.data.Category
import com.example.memopedia.onboarding.data.CategoryItem

class OnBoardingAdapter(
    private val context: Context
): RecyclerView.Adapter<OnBoardingAdapter.OnBoardingViewHolder>() {

    private var onItemClick: ((CategoryItem) -> Unit)? = null
    private val positionList: SparseIntArray = SparseIntArray()

    fun setOnItemClick(onItemClick: (CategoryItem) -> Unit) {

        this.onItemClick = onItemClick
    }

    inner class OnBoardingViewHolder(private val binding: OnboardingListItemBinding):
        RecyclerView.ViewHolder(binding.root) {

        private val categoriesRecyclerView = binding.categoriesRecyclerview
        private val categoryTitle = binding.categoryTitle
        val linearLayoutManager = LinearLayoutManager(context)
        private var adapter: CategoriesAdapter? = null

        init {

            adapter = CategoriesAdapter(context)
            categoriesRecyclerView.itemAnimator = null
            categoriesRecyclerView.adapter = adapter
            linearLayoutManager.orientation = RecyclerView.HORIZONTAL
            categoriesRecyclerView.layoutManager = linearLayoutManager
        }

        fun bind(category: Category, position: Int) {

            categoryTitle.text = category.name

            adapter?.setOnClick { categoryItem ->
                onItemClick?.let { it(categoryItem) }
            }

            adapter?.setOnSaveScroll { scrollPosition ->

                positionList.put(position,scrollPosition)
            }

            adapter?.categoriesDiffer?.submitList(category.categories)
        }
    }

    private val RECYCLER_COMPARATOR = object : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Category, newItem: Category) =
            oldItem == newItem
    }

    val asyncListDiffer = AsyncListDiffer(this, RECYCLER_COMPARATOR)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnBoardingViewHolder {

        val binding = OnboardingListItemBinding.inflate(
            LayoutInflater.from(parent.context),parent,false
        )

        return OnBoardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnBoardingViewHolder, position: Int) {

        val item = asyncListDiffer.currentList[position]
        holder.bind(item, position)

        val lastSeenFirstPosition = positionList.get(position,0)

        if (lastSeenFirstPosition >= 0) {
            holder.linearLayoutManager.scrollToPositionWithOffset(lastSeenFirstPosition,0)
        }
    }

    override fun onViewRecycled(holder: OnBoardingViewHolder) {
        super.onViewRecycled(holder)

        val position = holder.bindingAdapterPosition
        val firstVisiblePosition = holder.linearLayoutManager.findFirstVisibleItemPosition()
        positionList.put(position, firstVisiblePosition)
    }

    override fun getItemCount(): Int = asyncListDiffer.currentList.size
}