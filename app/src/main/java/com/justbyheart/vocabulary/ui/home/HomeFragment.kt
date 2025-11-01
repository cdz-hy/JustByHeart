package com.justbyheart.vocabulary.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.justbyheart.vocabulary.R
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.databinding.FragmentHomeBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HomeViewModel
    
    // 添加广播接收器用于监听词库切换
    private val wordBankChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // 词库切换后刷新首页数据，使用异步方式避免阻塞UI线程
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // 延迟一小段时间，确保词库切换完成
                    delay(100)
                    viewModel.loadTodayProgress(requireContext())
                    viewModel.loadOverallProgress(requireContext())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
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
        
        // 注册广播接收器 (适配Android 14)
        val filter = IntentFilter("com.justbyheart.vocabulary.WORD_BANK_CHANGED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 14及以上版本需要指定receiver flags
            requireActivity().registerReceiver(wordBankChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            // Android 13及以下版本
            requireActivity().registerReceiver(wordBankChangeReceiver, filter)
        }
        
        setupUI()
        observeViewModel()
        viewModel.loadTodayProgress(requireContext())
        viewModel.loadOverallProgress(requireContext())
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
            // 导航到今日单词片段
            viewModel.getTodayWordIds { wordIds ->
                if (wordIds != null) {
                    val action = HomeFragmentDirections.actionHomeFragmentToTodayWordsFragment(wordIds)
                    findNavController().navigate(action)
                }
            }
        }

        // 为总进度卡片设置点击监听器
        binding.cardOverallProgress.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_libraryFragment)
        }
        
        // 为复习卡片设置点击监听器
        binding.cardReview.setOnClickListener {
            findNavController().navigate(R.id.navigation_review)
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
        // 注销广播接收器
        try {
            requireActivity().unregisterReceiver(wordBankChangeReceiver)
        } catch (e: IllegalArgumentException) {
            // 接收器未注册，忽略异常
        }
        _binding = null
    }
}