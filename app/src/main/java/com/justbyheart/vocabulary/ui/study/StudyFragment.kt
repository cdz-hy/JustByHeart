package com.justbyheart.vocabulary.ui.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.justbyheart.vocabulary.R
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.databinding.FragmentStudyBinding
import kotlinx.coroutines.launch

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
        viewModel.loadTodayWords(requireContext())
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.loadTodayWords(requireContext())
    }
    
    private fun setupViewPager() {
        // 直接传递null作为repository，因为WordPagerAdapter现在不依赖于repository
        wordAdapter = WordPagerAdapter(
            { word, isFavorite -> viewModel.toggleFavorite(word.id, isFavorite) },
            { wordId, isFlipped -> 
                if (isFlipped) {
                    viewModel.addFlippedWord(wordId)
                } else {
                    viewModel.removeFlippedWord(wordId)
                }
            },
            null
        )
        
        binding.viewPagerWords.adapter = wordAdapter
        binding.viewPagerWords.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        
        binding.viewPagerWords.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 检查binding是否有效再进行操作
                if (_binding != null) {
                    viewModel.updateCurrentPosition(position)
                }
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
                lifecycleScope.launch {
                    // 获取今日已完成的单词ID列表
                    val today = viewModel.getTodayZeroed()
                    val completedWordIds = viewModel.getCompletedWordIdsForDate(today)
                    
                    // 过滤掉已完成的单词
                    val filteredFlippedWords = viewModel.flippedWords.value!!.filter { wordId ->
                        !completedWordIds.contains(wordId)
                    }
                    
                    // 检查过滤后的单词数量
                    if (filteredFlippedWords.isEmpty()) {
                        // 如果没有未完成的翻转单词，显示提示信息
                        android.widget.Toast.makeText(requireContext(), R.string.select_flipped_word_first, android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        // 创建一个导航动作，并将在ViewModel中收集到的已翻转单词ID列表作为参数传递给测试Fragment
                        val action = StudyFragmentDirections.actionStudyFragmentToTestFragment(
                            filteredFlippedWords.toLongArray()
                        )
                        // 检查NavController是否可用
                        if (isAdded) {
                            findNavController().navigate(action)
                        }
                    }
                }
            }
        }
        
        binding.buttonPrevious.setOnClickListener {
            // 检查binding是否有效再进行操作
            if (_binding != null) {
                val currentItem = binding.viewPagerWords.currentItem
                if (currentItem > 0) {
                    binding.viewPagerWords.currentItem = currentItem - 1
                }
            }
        }
        
        binding.buttonNext.setOnClickListener {
            // 检查binding是否有效再进行操作
            if (_binding != null) {
                val currentItem = binding.viewPagerWords.currentItem
                if (currentItem < wordAdapter.itemCount - 1) {
                    binding.viewPagerWords.currentItem = currentItem + 1
                }
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.todayWords.observe(viewLifecycleOwner) { words ->
            // 确保Fragment仍然活跃且binding有效
            if (!isAdded || isDetached || _binding == null) return@observe
            
            try {
                wordAdapter.submitList(words) { 
                    // 在列表提交完成后更新UI
                    if (!isAdded || isDetached || _binding == null) return@submitList
                    
                    // 当列表更新完成后，如果列表不为空，则立即更新进度
                    if (words.isNotEmpty()) {
                        binding.textProgress.text = getString(R.string.study_progress_format, 1, words.size)
                        viewModel.updateCurrentPosition(0)
                    } else {
                        binding.textProgress.text = getString(R.string.study_progress_format, 0, 0)
                    }
                    
                    // 控制 ViewPager 和提示文本的显示
                    if (words.isEmpty()) {
                        binding.viewPagerWords.visibility = View.GONE
                        binding.textNoWords.visibility = View.VISIBLE
                    } else {
                        binding.viewPagerWords.visibility = View.VISIBLE
                        binding.textNoWords.visibility = View.GONE
                    }
                    
                    binding.buttonStartTest.visibility = if (words.isNotEmpty()) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                // 确保不会因为binding访问异常导致崩溃
            }
        }
        
        viewModel.currentPosition.observe(viewLifecycleOwner) { position ->
            // 确保Fragment仍然活跃且binding有效
            if (!isAdded || isDetached || _binding == null) return@observe
            
            try {
                val totalWords = wordAdapter.itemCount
                if (totalWords > 0) {
                    binding.textProgress.text = getString(R.string.study_progress_format, position + 1, totalWords)
                }
                
                binding.buttonPrevious.isEnabled = position > 0
                binding.buttonNext.isEnabled = position < totalWords - 1
            } catch (e: Exception) {
                // 确保不会因为binding访问异常导致崩溃
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // 确保Fragment仍然活跃且binding有效
            if (!isAdded || isDetached || _binding == null) return@observe
            
            try {
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                if (isLoading) {
                    binding.viewPagerWords.visibility = View.GONE
                    binding.textNoWords.visibility = View.GONE
                }
            } catch (e: Exception) {
                // 确保不会因为binding访问异常导致崩溃
            }
        }

        // 观察收藏单词列表的变化
        viewModel.favoriteWords.observe(viewLifecycleOwner) { favoriteWords ->
            // 确保Fragment仍然活跃且binding有效
            if (!isAdded || isDetached || _binding == null) return@observe
            
            try {
                // 更新适配器中的收藏列表
                wordAdapter.setFavoriteWords(favoriteWords)
            } catch (e: Exception) {
                // 确保不会因为binding访问异常导致崩溃
            }
        }
        
        // 观察翻转单词列表的变化
        viewModel.flippedWords.observe(viewLifecycleOwner) { flippedWords ->
            // 确保Fragment仍然活跃且binding有效
            if (!isAdded || isDetached || _binding == null) return@observe
            
            try {
                wordAdapter.setFlippedWords(flippedWords)
            } catch (e: Exception) {
                // 确保不会因为binding访问异常导致崩溃
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}