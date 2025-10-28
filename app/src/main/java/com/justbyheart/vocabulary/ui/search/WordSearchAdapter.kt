package com.justbyheart.vocabulary.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.databinding.ItemSimpleWordBinding // Using item_simple_word.xml

class WordSearchAdapter(
    private val onItemClick: (Word) -> Unit // Click listener for search results
) : ListAdapter<Word, WordSearchAdapter.WordViewHolder>(WordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = ItemSimpleWordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WordViewHolder(
        private val binding: ItemSimpleWordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(word: Word) {
            binding.textEnglish.text = word.english
            binding.textChinese.text = word.chinese
            binding.textPronunciation.text = word.pronunciation ?: ""

            // Set click listener for the item
            binding.root.setOnClickListener {
                onItemClick(word)
            }
        }
    }

    class WordDiffCallback : DiffUtil.ItemCallback<Word>() {
        override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem == newItem
        }
    }
}