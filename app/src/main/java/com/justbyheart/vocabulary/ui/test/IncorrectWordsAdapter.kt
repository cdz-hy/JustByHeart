package com.justbyheart.vocabulary.ui.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.justbyheart.vocabulary.R
import com.justbyheart.vocabulary.data.entity.Word

class IncorrectWordsAdapter(
    private val onItemClick: (Long) -> Unit
) : ListAdapter<Word, IncorrectWordsAdapter.WordViewHolder>(WordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incorrect_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.card_view)
        private val textEnglish: TextView = itemView.findViewById(R.id.text_english)
        private val textChinese: TextView = itemView.findViewById(R.id.text_chinese)

        fun bind(word: Word) {
            textEnglish.text = word.english
            textChinese.text = word.chinese
            
            cardView.setOnClickListener {
                onItemClick(word.id)
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