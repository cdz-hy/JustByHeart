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

class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: SettingsViewModel
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "vocabulary_settings"
        private const val KEY_DAILY_WORD_COUNT = "daily_word_count"
        private const val DEFAULT_DAILY_WORD_COUNT = 10
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        
        val database = VocabularyDatabase.getDatabase(requireContext())
        val repository = WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )
        
        viewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(repository)
        )[SettingsViewModel::class.java]
        
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        loadSettings()
    }
    
    private fun setupUI() {
        binding.seekBarDailyWords.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val wordCount = progress + 1 // 最少1个单词
                binding.textDailyWordCount.text = "$wordCount 个单词/天"
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val wordCount = (seekBar?.progress ?: 0) + 1
                saveDailyWordCount(wordCount)
            }
        })
        
        binding.buttonInitializeData.setOnClickListener {
            initializeWordData()
        }
    }
    
    private fun loadSettings() {
        val dailyWordCount = sharedPreferences.getInt(KEY_DAILY_WORD_COUNT, DEFAULT_DAILY_WORD_COUNT)
        binding.seekBarDailyWords.progress = dailyWordCount - 1
        binding.textDailyWordCount.text = "$dailyWordCount 个单词/天"
    }
    
    private fun saveDailyWordCount(count: Int) {
        sharedPreferences.edit()
            .putInt(KEY_DAILY_WORD_COUNT, count)
            .apply()
        
        viewModel.updateDailyWordCount(count)
    }
    
    private fun initializeWordData() {
        binding.buttonInitializeData.isEnabled = false
        binding.textInitializeStatus.text = "正在初始化单词数据..."
        
        lifecycleScope.launch {
            try {
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
    
    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}