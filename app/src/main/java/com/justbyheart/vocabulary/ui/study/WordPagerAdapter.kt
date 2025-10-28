package com.justbyheart.vocabulary.ui.study

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.databinding.JustbyheartWordCardBinding

class WordPagerAdapter(
    private val onFavoriteClick: (Word, Boolean) -> Unit
) : ListAdapter<Word, WordPagerAdapter.WordViewHolder>(WordDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = JustbyheartWordCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WordViewHolder(binding, onFavoriteClick)
    }
    
    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class WordViewHolder(
        private val binding: JustbyheartWordCardBinding,
        private val onFavoriteClick: (Word, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private var isFavorite = false
        
        fun bind(word: Word) {
            binding.textEnglish.text = word.english
            binding.textChinese.text = word.chinese
            binding.textPronunciation.text = word.pronunciation ?: ""
            binding.textDefinition.text = word.definition ?: ""
            binding.textExample.text = word.example ?: ""
            binding.textExampleTranslation.text = word.exampleTranslation ?: ""
            
            binding.buttonFavorite.setOnClickListener {
                isFavorite = !isFavorite
                updateFavoriteButton()
                onFavoriteClick(word, isFavorite)
            }
            
            updateFavoriteButton()
        }
        
        private fun updateFavoriteButton() {
            binding.buttonFavorite.isSelected = isFavorite
        }
    }
    
    class WordDiffCallback : DiffUtil.ItemCallback<Word>() {
        override fun areItemsTheSame(oldItem: Word, newItem: Boolean): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Word, newItem: Boolean): Boolean {
            return oldItem == newItem
        }
    }
}