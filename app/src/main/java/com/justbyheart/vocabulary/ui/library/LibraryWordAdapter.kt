package com.justbyheart.vocabulary.ui.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.databinding.ItemSimpleWordBinding

class LibraryWordAdapter(private val onItemClick: (Word) -> Unit) : ListAdapter<Word, LibraryWordAdapter.WordViewHolder>(WordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = ItemSimpleWordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WordViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class WordViewHolder(private val binding: ItemSimpleWordBinding, private val onItemClick: (Word) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(word: Word) {
            binding.textEnglish.text = word.english
            binding.textChinese.text = word.chinese
            binding.textPronunciation.text = word.pronunciation ?: ""
            itemView.setOnClickListener { onItemClick(word) }
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