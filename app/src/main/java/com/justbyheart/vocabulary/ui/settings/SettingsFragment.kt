package com.justbyheart.vocabulary.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.databinding.FragmentSettingsBinding
import com.justbyheart.vocabulary.utils.WordDataLoader
import kotlinx.coroutines.launch

/**
 * 设置界面Fragment
 * 提供每日单词数量设置和数据初始化功能
 */
class SettingsFragment : Fragment() {
    
    // 视图绑定
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    // ViewModel和SharedPreferences实例
    private lateinit var viewModel: SettingsViewModel
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        // SharedPreferences相关常量
        private const val PREFS_NAME = "vocabulary_settings"          // 配置文件名
        private const val KEY_DAILY_WORD_COUNT = "daily_word_count"   // 每日单词数键名
        private const val DEFAULT_DAILY_WORD_COUNT = 10               // 默认每日单词数
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 初始化视图绑定
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        
        // 获取数据库实例和仓库
        val database = VocabularyDatabase.getDatabase(requireContext())
        val repository = WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(repository)
        )[SettingsViewModel::class.java]
        
        // 获取SharedPreferences实例
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 设置UI组件和观察ViewModel
        setupUI()
        observeViewModel()
        loadSettings() // 加载已保存的设置
    }
    
    /**
     * 设置UI组件的交互逻辑
     */
    private fun setupUI() {
        // 设置SeekBar的监听器
        binding.seekBarDailyWords.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            // 当进度改变时更新显示的单词数量
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val wordCount = progress + 1 // 最少1个单词
                binding.textDailyWordCount.text = "$wordCount 个单词/天"
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            // 当停止拖动时保存设置
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val wordCount = (seekBar?.progress ?: 0) + 1
                saveDailyWordCount(wordCount)
            }
        })
        
        // 设置初始化数据按钮的点击事件
        binding.buttonInitializeData.setOnClickListener {
            initializeWordData()
        }
    }
    
    /**
     * 从SharedPreferences加载设置
     */
    private fun loadSettings() {
        val dailyWordCount = sharedPreferences.getInt(KEY_DAILY_WORD_COUNT, DEFAULT_DAILY_WORD_COUNT)
        binding.seekBarDailyWords.progress = dailyWordCount - 1
        binding.textDailyWordCount.text = "$dailyWordCount 个单词/天"
    }
    
    /**
     * 保存每日单词数量到SharedPreferences
     * @param count 要保存的单词数量
     */
    private fun saveDailyWordCount(count: Int) {
        sharedPreferences.edit()
            .putInt(KEY_DAILY_WORD_COUNT, count)
            .apply()
        
        viewModel.updateDailyWordCount(count)
    }
    
    /**
     * 初始化单词数据
     * 从assets目录加载单词数据并保存到数据库
     */
    private fun initializeWordData() {
        binding.buttonInitializeData.isEnabled = false
        binding.textInitializeStatus.text = "正在初始化单词数据..."
        
        lifecycleScope.launch {
            try {
                // 从assets目录加载单词数据
                val words = WordDataLoader.loadWordsFromAssets(requireContext())
                viewModel.initializeWords(words)
                binding.textInitializeStatus.text = "单词数据初始化完成！共加载 ${words.size} 个单词"
            } catch (e: Exception) {
                binding.textInitializeStatus.text = "初始化失败：${e.message}"
            } finally {
                binding.buttonInitializeData.isEnabled = true
            }
        }
    }
    
    /**
     * 观察ViewModel中的数据变化
     */
    private fun observeViewModel() {
        // 观察加载状态变化
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}