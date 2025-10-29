package com.justbyheart.vocabulary.ui.review

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.databinding.JustbyheartSimpleWordBinding

class ReviewWordAdapter(private val onItemClick: (Word) -> Unit) : ListAdapter<Word, ReviewWordAdapter.WordViewHolder>(WordDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = JustbyheartSimpleWordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WordViewHolder(binding, onItemClick)
    }
    
    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class WordViewHolder(
        private val binding: JustbyheartSimpleWordBinding,
        private val onItemClick: (Word) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
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