package com.justbyheart.vocabulary.ui.study

import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.databinding.JustbyheartWordCardBinding

/**
 * 单词页面适配器
 * 用于在ViewPager中显示单词卡片列表
 * @param onFavoriteClick 收藏按钮点击回调函数
 */
class WordPagerAdapter(
    private val onFavoriteClick: (Word, Boolean) -> Unit,
    private val onWordFlipped: (Long, Boolean) -> Unit // 单词卡片被翻转时的回调，参数：单词ID，是否翻转到背面
) : ListAdapter<Word, WordPagerAdapter.WordViewHolder>(WordDiffCallback()) {

    private var favoriteWords: Set<Long> = emptySet()
    private var flippedWords: Set<Long> = emptySet()

    /**
     * 设置收藏单词列表
     * @param words 收藏的单词列表
     */
    fun setFavoriteWords(words: List<Word>) {
        val newFavoriteIds = words.map { it.id }.toSet()
        val oldFavoriteIds = favoriteWords
        favoriteWords = newFavoriteIds

        val changedIds = newFavoriteIds.union(oldFavoriteIds) - newFavoriteIds.intersect(oldFavoriteIds)
        changedIds.forEach { id ->
            val index = currentList.indexOfFirst { it.id == id }
            if (index != -1) {
                notifyItemChanged(index, "favorite")
            }
        }
    }

    fun setFlippedWords(wordIds: Set<Long>) {
        flippedWords = wordIds
    }

    /**
     * 创建ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = JustbyheartWordCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WordViewHolder(binding, onFavoriteClick, onWordFlipped)
    }

    /**
     * 绑定ViewHolder数据
     */
    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = getItem(position)
        holder.bind(word, favoriteWords.contains(word.id), flippedWords.contains(word.id))
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains("favorite")) {
            val word = getItem(position)
            holder.updateFavoriteStatus(favoriteWords.contains(word.id))
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    /**
     * 单词视图持有者
     * 负责绑定单词数据到卡片视图，并处理交互逻辑
     */
    class WordViewHolder(
        private val binding: JustbyheartWordCardBinding,
        private val onFavoriteClick: (Word, Boolean) -> Unit,
        private val onWordFlipped: (Long, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var isFavorite: Boolean = false

        /**
         * 绑定单词数据到视图
         * @param word 要显示的单词对象
         * @param isFavorite 单词是否被收藏
         * @param isFlipped 单词卡片是否已翻转
         */
        fun bind(word: Word, isFavorite: Boolean, isFlipped: Boolean) {
            this.isFavorite = isFavorite
            // 绑定单词各项信息到对应视图组件
            binding.textEnglish.text = word.english
            binding.textChinese.text = word.chinese
            binding.textPronunciation.text = word.pronunciation ?: ""
            binding.textDefinition.text = word.definition ?: ""
            // 只显示第一组例句
            val examples = word.example?.split("\n") ?: emptyList()
            val exampleTranslations = word.exampleTranslation?.split("\n") ?: emptyList()

            if (examples.isNotEmpty() && exampleTranslations.isNotEmpty()) {
                binding.textExample.text = examples[0]
                binding.textExampleTranslation.text = exampleTranslations[0]
            } else {
                binding.textExample.text = ""
                binding.textExampleTranslation.text = ""
            }
            binding.backOfCard.text = word.english

            // 根据翻转状态设置卡片显示正面或背面
            if (isFlipped) {
                binding.frontOfCardGroup.visibility = View.GONE
                binding.backOfCard.visibility = View.VISIBLE
            } else {
                binding.frontOfCardGroup.visibility = View.VISIBLE
                binding.backOfCard.visibility = View.GONE
            }

            // 设置收藏按钮点击事件
            binding.buttonFavorite.setOnClickListener {
                this.isFavorite = !this.isFavorite
                updateFavoriteButton()
                onFavoriteClick(word, this.isFavorite)
            }

            // 设置卡片点击事件（用于翻转卡片）
            binding.cardContentLayout.setOnClickListener {
                flipCard(word)
            }

            updateFavoriteButton()
        }

        fun updateFavoriteStatus(isFavorite: Boolean) {
            this.isFavorite = isFavorite
            updateFavoriteButton()
        }

        /**
         * 更新收藏按钮状态
         */
        private fun updateFavoriteButton() {
            binding.buttonFavorite.isSelected = isFavorite
        }

        /**
         * 翻转卡片动画
         * 实现卡片正面和背面之间的3D翻转效果
         */
        private fun flipCard(word: Word) {
            val frontView = binding.frontOfCardGroup
            val backView = binding.backOfCard

            val isShowingFront = frontView.visibility == View.VISIBLE

            // 调用回调，传递单词ID和翻转状态
            // 如果当前显示正面，翻转到背面则传递true；如果当前显示背面，翻转到正面则传递false
            onWordFlipped(word.id, isShowingFront)

            // 设置摄像机距离以实现3D效果
            val distance = 8000
            val scale = itemView.resources.displayMetrics.density * distance
            itemView.cameraDistance = scale

            // 临时移除视图阴影以避免动画过程中的视觉问题
            val originalElevation = itemView.elevation
            itemView.elevation = 0f

            // 创建两个动画对象实现翻转效果
            val anim1 = ObjectAnimator.ofFloat(itemView, "rotationY", 0f, 90f)   // 第一步：从0度旋转到90度
            val anim2 = ObjectAnimator.ofFloat(itemView, "rotationY", -90f, 0f)  // 第二步：从-90度旋转到0度

            anim1.duration = 250
            anim2.duration = 250

            // 第一个动画结束时切换显示正面或背面
            anim1.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (isShowingFront) {
                        frontView.visibility = View.GONE
                        backView.visibility = View.VISIBLE
                    } else {
                        frontView.visibility = View.VISIBLE
                        backView.visibility = View.GONE
                    }
                    anim2.start()
                }
            })

            // 第二个动画结束时恢复视图阴影
            anim2.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    itemView.elevation = originalElevation
                }
            })

            anim1.start()
        }
    }

    /**
     * 单词差异回调
     * 用于优化RecyclerView的数据更新效率
     */
    class WordDiffCallback : DiffUtil.ItemCallback<Word>() {
        /**
         * 判断两个项目是否是同一个项目
         */
        override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem.id == newItem.id
        }

        /**
         * 判断两个项目的内容是否相同
         */
        override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem == newItem
        }
    }
}