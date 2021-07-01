package com.example.memopedia.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.memopedia.R
import com.example.memopedia.databinding.MemeItemBinding
import com.example.memopedia.network.Meme

class BaseAdapter(
    private val context: Context
) : RecyclerView.Adapter<BaseAdapter.BaseViewHolder>() {

    inner class BaseViewHolder(val binding: MemeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val authorName = binding.authorName
        val memeImage = binding.memeImage
        val memeTitle = binding.memeTitle

        fun bind(meme: Meme) {

            authorName.text = meme.author
            memeTitle.text = meme.title

            Glide.with(context)
                .load(meme.url)
                .placeholder(R.drawable.ic_image)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(memeImage)
        }
    }

    private val RECYCLER_COMPARATOR = object : DiffUtil.ItemCallback<Meme>() {
        override fun areItemsTheSame(oldItem: Meme, newItem: Meme) =
            oldItem.postLink == newItem.postLink

        override fun areContentsTheSame(oldItem: Meme, newItem: Meme) =
            oldItem == newItem
    }

    val differ = AsyncListDiffer(this, RECYCLER_COMPARATOR)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

        val binding = MemeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {

        val item = differ.currentList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}