package com.justbyheart.vocabulary.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.databinding.JustbyheartFavoriteWordBinding

class FavoriteWordAdapter(
    private val onRemoveClick: (Word) -> Unit
) : ListAdapter<Word, FavoriteWordAdapter.WordViewHolder>(WordDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = JustbyheartFavoriteWordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WordViewHolder(binding, onRemoveClick)
    }
    
    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class WordViewHolder(
        private val binding: JustbyheartFavoriteWordBinding,
        private val onRemoveClick: (Word) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(word: Word) {
            binding.textEnglish.text = word.english
            binding.textChinese.text = word.chinese
            binding.textPronunciation.text = word.pronunciation ?: ""
            
            binding.buttonRemove.setOnClickListener {
                onRemoveClick(word)
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