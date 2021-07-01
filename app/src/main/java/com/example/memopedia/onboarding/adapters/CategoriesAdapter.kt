package com.example.memopedia.onboarding.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.contains
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.memopedia.R
import com.example.memopedia.databinding.CategoriesListItemBinding
import com.example.memopedia.onboarding.data.CategoryItem
import com.google.android.material.chip.Chip
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel

class CategoriesAdapter(
    private val context: Context
) : RecyclerView.Adapter<CategoriesAdapter.OnBoardingViewHolder>() {

    private var onClick: ((CategoryItem) -> Unit)? = null
    private var onSaveScroll: ((Int) -> Unit)? = null

    fun setOnClick(onClick: (CategoryItem) -> Unit) {

        this.onClick = onClick
    }

    fun setOnSaveScroll(onSaveScroll: (Int) -> Unit) {

        this.onSaveScroll = onSaveScroll
    }

    inner class OnBoardingViewHolder(private val binding: CategoriesListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val name = binding.categoryName
        val scrollView = binding.categoryTagsHolder
        val chipGroup = binding.categoryChipgroup
        val icon = binding.categoryIcon
        val background = binding.bg

        fun bind(category: CategoryItem) {

            val radius: Float = context.resources.getDimension(R.dimen.image_corner_radius)
            val shapeAppearanceModel: ShapeAppearanceModel =
                icon.shapeAppearanceModel
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, radius)
                    .build()

            icon.shapeAppearanceModel = shapeAppearanceModel

            if (category.isSelected) {

                background.setBackgroundResource(R.drawable.category_background_selected)
                name.setBackgroundResource(R.drawable.name_selected)
                name.setTextColor(context.resources.getColor(R.color.white))
                icon.setColorFilter(Color.argb(80, 0, 0, 225))

            } else {

                background.setBackgroundResource(R.drawable.category_background)
                name.setBackgroundResource(R.drawable.name)
                name.setTextColor(context.resources.getColor(R.color.black))
                icon.colorFilter = null
            }

            name.text = category.name

            val chips = ArrayList<Chip>()

            category.tags.forEach { tag ->

                val chip = Chip(context)
                chip.text = tag
                chip.id = tag.hashCode()

                chips.add(chip)
            }

            chipGroup.removeAllViews()

            chips.forEach { chip ->

                chipGroup.addView(chip)
            }

            Glide.with(context)
                .load(category.url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_wifi)
                .into(icon)
        }
    }

    private val RECYCLER_COMPARATOR = object : DiffUtil.ItemCallback<CategoryItem>() {
        override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem) =
            oldItem == newItem
    }

    val categoriesDiffer = AsyncListDiffer(this, RECYCLER_COMPARATOR)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnBoardingViewHolder {

        val binding = CategoriesListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return OnBoardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnBoardingViewHolder, position: Int) {

        val item = categoriesDiffer.currentList[position]

        item?.let { categoryItem ->

            holder.bind(categoryItem)
            holder.itemView.setOnClickListener {

                onClick?.let { it1 -> it1(categoryItem) }
                onSaveScroll?.let { saveScroll -> saveScroll(holder.bindingAdapterPosition) }
            }

//            Log.d("spacer ","------------------")
//            Log.d("name ","${categoryItem.name}")
//            Log.d("selected ","${categoryItem.isSelected}")
        }
    }

    override fun getItemCount(): Int = categoriesDiffer.currentList.size
}