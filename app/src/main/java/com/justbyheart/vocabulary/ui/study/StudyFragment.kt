package com.justbyheart.vocabulary.ui.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.justbyheart.vocabulary.R
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.databinding.FragmentStudyBinding

class StudyFragment : Fragment() {
    
    private var _binding: FragmentStudyBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: StudyViewModel
    private lateinit var wordAdapter: WordPagerAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyBinding.inflate(inflater, container, false)
        
        val database = VocabularyDatabase.getDatabase(requireContext())
        val repository = WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )
        
        viewModel = ViewModelProvider(
            this,
            StudyViewModelFactory(repository)
        )[StudyViewModel::class.java]
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewPager()
        setupUI()
        observeViewModel()
        viewModel.loadTodayWords()
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.loadTodayWords()
    }
    
    private fun setupViewPager() {
                // 初始化适配器，并传入两个回调函数：
                // 1. 点击收藏按钮时的回调
                // 2. 翻转单词卡片时的回调
                wordAdapter = WordPagerAdapter({
                    word, isFavorite ->
                    viewModel.toggleFavorite(word.id, isFavorite)
                }, {
                    wordId, isFlippedToBack ->
                    if (isFlippedToBack) {
                        // 从正面翻转到背面
                        viewModel.addFlippedWord(wordId)
                    } else {
                        // 从背面翻转到正面
                        viewModel.removeFlippedWord(wordId)
                    }
                })
        
        binding.viewPagerWords.adapter = wordAdapter
        binding.viewPagerWords.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        
        binding.viewPagerWords.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.updateCurrentPosition(position)
            }
        })
    }
    
    private fun setupUI() {
        // 设置开始测试按钮的点击事件
        binding.buttonStartTest.setOnClickListener {
            // 检查是否有翻转的单词
            if (viewModel.flippedWords.value.isNullOrEmpty()) {
                // 如果没有翻转的单词，显示提示信息
                android.widget.Toast.makeText(requireContext(), R.string.select_flipped_word_first, android.widget.Toast.LENGTH_SHORT).show()
            } else {
                // 创建一个导航动作，并将在ViewModel中收集到的已翻转单词ID列表作为参数传递给测试Fragment
                val action = StudyFragmentDirections.actionStudyFragmentToTestFragment(
                    viewModel.flippedWords.value!!.toLongArray()
                )
                findNavController().navigate(action)
            }
        }
        
        binding.buttonPrevious.setOnClickListener {
            val currentItem = binding.viewPagerWords.currentItem
            if (currentItem > 0) {
                binding.viewPagerWords.currentItem = currentItem - 1
            }
        }
        
        binding.buttonNext.setOnClickListener {
            val currentItem = binding.viewPagerWords.currentItem
            if (currentItem < wordAdapter.itemCount - 1) {
                binding.viewPagerWords.currentItem = currentItem + 1
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.todayWords.observe(viewLifecycleOwner) { words ->
            wordAdapter.submitList(words) {
                // 当列表更新完成后，如果列表不为空，则立即更新进度
                if (words.isNotEmpty()) {
                    binding.textProgress.text = getString(R.string.study_progress_format, 1, words.size)
                    viewModel.updateCurrentPosition(0)
                } else {
                    binding.textProgress.text = getString(R.string.study_progress_format, 0, 0)
                }
            }
            
            binding.buttonStartTest.visibility = if (words.isNotEmpty()) View.VISIBLE else View.GONE
        }
        
        viewModel.currentPosition.observe(viewLifecycleOwner) { position ->
            val totalWords = wordAdapter.itemCount
            if (totalWords > 0) {
                binding.textProgress.text = getString(R.string.study_progress_format, position + 1, totalWords)
            }
            
            binding.buttonPrevious.isEnabled = position > 0
            binding.buttonNext.isEnabled = position < totalWords - 1
            
            if (position == totalWords - 1 && totalWords > 0) {
                viewModel.markTodayComplete()
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.viewPagerWords.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        // 观察收藏单词列表的变化
        viewModel.favoriteWords.observe(viewLifecycleOwner) { favoriteWords ->
            // 更新适配器中的收藏列表
            wordAdapter.setFavoriteWords(favoriteWords)
        }
        
        // 观察翻转单词列表的变化
        viewModel.flippedWords.observe(viewLifecycleOwner) { flippedWords ->
            wordAdapter.setFlippedWords(flippedWords)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}