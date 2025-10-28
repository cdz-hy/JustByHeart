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
    
    private fun setupViewPager() {
        wordAdapter = WordPagerAdapter { word, isFavorite ->
            viewModel.toggleFavorite(word.id, isFavorite)
        }
        
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
        binding.buttonStartTest.setOnClickListener {
            findNavController().navigate(R.id.testFragment)
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
            wordAdapter.submitList(words)
            binding.textProgress.text = "0/${words.size}"
            
            binding.buttonStartTest.visibility = if (words.isNotEmpty()) View.VISIBLE else View.GONE
        }
        
        viewModel.currentPosition.observe(viewLifecycleOwner) { position ->
            val totalWords = wordAdapter.itemCount
            binding.textProgress.text = "${position + 1}/$totalWords"
            
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
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}