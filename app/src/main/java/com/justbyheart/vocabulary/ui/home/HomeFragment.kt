package com.justbyheart.vocabulary.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.justbyheart.vocabulary.R
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HomeViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        
        val database = VocabularyDatabase.getDatabase(requireContext())
        val repository = WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )
        
        viewModel = ViewModelProvider(
            this,
            HomeViewModelFactory(repository)
        )[HomeViewModel::class.java]
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        viewModel.loadTodayProgress()
        viewModel.loadOverallProgress()
    }
    
    private fun setupUI() {
        // 设置当前日期
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        binding.textCurrentDate.text = dateFormat.format(Date())

        binding.cardSearch.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_search)
        }

        // 为开始学习按钮设置点击监听器
        binding.buttonStartStudy.setOnClickListener {
            // 导航到学习片段
            findNavController().navigate(R.id.navigation_study)
        }

        // 为每日目标卡片设置点击监听器
        binding.cardDailyGoal.setOnClickListener {
            // 导航到设置片段
            findNavController().navigate(R.id.navigation_settings)
        }

        // 为总进度卡片设置点击监听器
        binding.cardOverallProgress.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_libraryFragment)
        }
    }
    
    private fun observeViewModel() {
        viewModel.dailyProgress.observe(viewLifecycleOwner) { progress ->
            binding.textWordsCompleted.text = "${progress.completed}/${progress.target}"
            binding.progressDaily.progress = if (progress.target > 0) {
                (progress.completed * 100 / progress.target)
            } else 0
            
            binding.buttonStartStudy.text = if (progress.completed > 0) {
                getString(R.string.continue_studying)
            } else {
                getString(R.string.start_studying)
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.overallProgress.observe(viewLifecycleOwner) { progress ->
            val memorized = progress.first
            val total = progress.second
            binding.textOverallProgress.text = "$memorized/$total"
            binding.progressOverall.progress = if (total > 0) {
                (memorized * 100 / total)
            } else 0
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}