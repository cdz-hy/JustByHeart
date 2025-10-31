package com.justbyheart.vocabulary.ui.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.justbyheart.vocabulary.R
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.entity.StudyRecord
import com.justbyheart.vocabulary.data.model.ExportData
import com.justbyheart.vocabulary.data.model.ExportWordBank
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.databinding.FragmentSettingsBinding
import com.justbyheart.vocabulary.utils.WordDataLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

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
    
    // 文件选择器
    private val selectFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { importData(it) }
    }
    
    companion object {
        // SharedPreferences相关常量
        private const val PREFS_NAME = "vocabulary_settings"          // 配置文件名
        private const val KEY_DAILY_WORD_COUNT = "daily_word_count"   // 每日单词数键名
        private const val KEY_CURRENT_WORD_BANK = "current_word_bank" // 当前词库键名
        private const val DEFAULT_DAILY_WORD_COUNT = 10               // 默认每日单词数
        private const val DEFAULT_WORD_BANK = "六级核心"                // 默认词库
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
        checkInitializationStatus() // 检查数据初始化状态
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
                binding.textDailyWordCount.text = getString(R.string.daily_word_count_format, wordCount)
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
        
        // 设置词库选择按钮的点击事件
        binding.buttonSelectWordBank.setOnClickListener {
            showWordBankSelectionDialog()
        }
        
        // 设置导出数据按钮的点击事件
        binding.buttonExportData.setOnClickListener {
            exportData()
        }
        
        // 设置导入数据按钮的点击事件
        binding.buttonImportData.setOnClickListener {
            selectFileLauncher.launch("*/*")
        }

        // 设置主题切换按钮的点击事件
        binding.themeDefault.setOnClickListener { saveAndApplyTheme("Theme.JustByHeartVocabulary") }
        binding.themeMorandiBlue.setOnClickListener { saveAndApplyTheme("Theme.JustByHeart.MorandiBlue") }
        binding.themeMorandiGreen.setOnClickListener { saveAndApplyTheme("Theme.JustByHeart.MorandiGreen") }
        binding.themeMorandiPink.setOnClickListener { saveAndApplyTheme("Theme.JustByHeart.MorandiPink") }
        binding.themeMorandiGrey.setOnClickListener { saveAndApplyTheme("Theme.JustByHeart.MorandiGrey") }
        binding.themeMorandiBrown.setOnClickListener { saveAndApplyTheme("Theme.JustByHeart.MorandiBrown") }
    }

    private fun saveAndApplyTheme(themeName: String) {
        sharedPreferences.edit().putString("theme", themeName).apply()
        requireActivity().recreate()
    }
    
    /**
     * 从SharedPreferences加载设置
     */
    private fun loadSettings() {
        val dailyWordCount = sharedPreferences.getInt(KEY_DAILY_WORD_COUNT, DEFAULT_DAILY_WORD_COUNT)
        val currentWordBank = sharedPreferences.getString(KEY_CURRENT_WORD_BANK, DEFAULT_WORD_BANK)
        
        binding.seekBarDailyWords.progress = dailyWordCount - 1
        binding.textDailyWordCount.text = getString(R.string.daily_word_count_format, dailyWordCount)
        binding.textCurrentWordBank.text = currentWordBank
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
     * 保存当前词库到SharedPreferences
     * @param wordBank 要保存的词库名称
     */
    private fun saveCurrentWordBank(wordBank: String) {
        sharedPreferences.edit()
            .putString(KEY_CURRENT_WORD_BANK, wordBank)
            .apply()
    }
    
    /**
     * 显示词库选择对话框
     */
    private fun showWordBankSelectionDialog() {
        val wordBanks = WordDataLoader.getAvailableWordBanks().toTypedArray()
        val currentWordBank = sharedPreferences.getString(KEY_CURRENT_WORD_BANK, DEFAULT_WORD_BANK) ?: DEFAULT_WORD_BANK
        val selectedIndex = wordBanks.indexOf(currentWordBank)
        
        AlertDialog.Builder(requireContext())
            .setTitle("选择词库")
            .setSingleChoiceItems(wordBanks, selectedIndex) { dialog, which ->
                val selectedWordBank = wordBanks[which]
                if (selectedWordBank != currentWordBank) {
                    switchWordBank(selectedWordBank)
                }
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    /**
     * 加载新的词库
     * @param wordBank 词库名称
     */
    private suspend fun loadNewWordBank(wordBank: String) {
        try {
            // 获取当前词库
            val currentWordBank = sharedPreferences.getString(KEY_CURRENT_WORD_BANK, DEFAULT_WORD_BANK) ?: DEFAULT_WORD_BANK
            
            // 获取词库对应的文件名
            val fileNames = WordDataLoader.getWordBankFileNames(wordBank)
            
            // 加载所有相关文件的单词
            val allWords = mutableListOf<com.justbyheart.vocabulary.data.entity.Word>()
            for (fileName in fileNames) {
                val words = WordDataLoader.loadWordsFromAssets(requireContext(), fileName)
                allWords.addAll(words)
                binding.textInitializeStatus.text = "正在加载词库: $fileName..."
            }
            
            // 保存到数据库
            viewModel.initializeWords(allWords)
            
            // 保存当前词库设置
            saveCurrentWordBank(wordBank)
            binding.textCurrentWordBank.text = wordBank
            binding.textInitializeStatus.text = "已加载新词库: $wordBank (${allWords.size}个单词)"
            
            Toast.makeText(context, "已加载新词库: $wordBank (${allWords.size}个单词)", Toast.LENGTH_LONG).show()
            
            // 延迟一小段时间再发送广播，确保数据库操作完成
            delay(200)
            
            // 询问是否迁移数据
            showDataMigrationDialog(currentWordBank, wordBank)
            
            // 发送广播通知首页刷新
            val intent = android.content.Intent("com.justbyheart.vocabulary.WORD_BANK_CHANGED")
            requireActivity().sendBroadcast(intent)
        } catch (e: Exception) {
            binding.textInitializeStatus.text = "加载词库失败: ${e.message}"
            Toast.makeText(context, "加载词库失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * 切换词库
     * @param newWordBank 新的词库名称
     */
    private fun switchWordBank(newWordBank: String) {
        val currentWordBank = sharedPreferences.getString(KEY_CURRENT_WORD_BANK, DEFAULT_WORD_BANK) ?: DEFAULT_WORD_BANK
        
        if (newWordBank == currentWordBank) return
        
        binding.buttonSelectWordBank.isEnabled = false
        binding.textInitializeStatus.text = "正在切换词库..."
        binding.textInitializeStatus.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                // 检查新词库是否已经存在于数据库中
                val wordCountInNewBank = viewModel.getWordCountByWordBank(newWordBank)
                
                if (wordCountInNewBank == 0) {
                    // 新词库不存在，需要加载
                    loadNewWordBank(newWordBank)
                } else {
                    // 新词库已存在，直接切换
                    saveCurrentWordBank(newWordBank)
                    binding.textCurrentWordBank.text = newWordBank
                    binding.textInitializeStatus.text = "已切换到词库: $newWordBank"
                    
                    // 延迟一小段时间再发送广播，确保操作完成
                    delay(200)
                    
                    // 询问是否迁移数据
                    showDataMigrationDialog(currentWordBank, newWordBank)
                    
                    // 发送广播通知首页刷新
                    val intent = android.content.Intent("com.justbyheart.vocabulary.WORD_BANK_CHANGED")
                    requireActivity().sendBroadcast(intent)
                }
            } catch (e: Exception) {
                binding.textInitializeStatus.text = "切换词库失败: ${e.message}"
                Toast.makeText(context, "切换词库失败: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.buttonSelectWordBank.isEnabled = true
            }
        }
    }
    
    /**
     * 显示数据迁移对话框
     * @param fromWordBank 原词库
     * @param toWordBank 目标词库
     */
    private fun showDataMigrationDialog(fromWordBank: String, toWordBank: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("数据迁移")
            .setMessage("是否将当前词库($fromWordBank)的背诵和收藏记录迁移到新词库($toWordBank)？")
            .setPositiveButton("迁移背诵记录") { _, _ ->
                migrateStudyRecords(fromWordBank, toWordBank)
            }
            .setNegativeButton("迁移收藏记录") { _, _ ->
                migrateFavoriteWords(fromWordBank, toWordBank)
            }
            .setNeutralButton("全部迁移") { _, _ ->
                migrateStudyRecords(fromWordBank, toWordBank)
                migrateFavoriteWords(fromWordBank, toWordBank)
            }
            .show()
    }
    
    /**
     * 迁移学习记录
     * @param fromWordBank 原词库
     * @param toWordBank 目标词库
     */
    private fun migrateStudyRecords(fromWordBank: String, toWordBank: String) {
        lifecycleScope.launch {
            try {
                viewModel.migrateStudyRecords(fromWordBank, toWordBank)
                Toast.makeText(context, "背诵记录迁移完成", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "背诵记录迁移失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * 迁移收藏记录
     * @param fromWordBank 原词库
     * @param toWordBank 目标词库
     */
    private fun migrateFavoriteWords(fromWordBank: String, toWordBank: String) {
        lifecycleScope.launch {
            try {
                viewModel.migrateFavoriteWords(fromWordBank, toWordBank)
                Toast.makeText(context, "收藏记录迁移完成", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "收藏记录迁移失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * 初始化单词数据
     * 从assets目录加载单词数据并保存到数据库
     */
    private fun initializeWordData() {
        binding.buttonInitializeData.isEnabled = false
        binding.textInitializeStatus.text = getString(R.string.initializing_word_data)
        
        lifecycleScope.launch {
            try {
                // 先清空现有单词数据
                viewModel.clearAllWords()
                binding.textInitializeStatus.text = "正在清空数据库..."
                
                // 获取当前选择的词库
                val currentWordBank = sharedPreferences.getString(KEY_CURRENT_WORD_BANK, DEFAULT_WORD_BANK) ?: DEFAULT_WORD_BANK
                
                // 获取词库对应的文件名
                val fileNames = WordDataLoader.getWordBankFileNames(currentWordBank)
                
                // 加载所有相关文件的单词
                val allWords = mutableListOf<com.justbyheart.vocabulary.data.entity.Word>()
                for (fileName in fileNames) {
                    val words = WordDataLoader.loadWordsFromAssets(requireContext(), fileName)
                    allWords.addAll(words)
                    binding.textInitializeStatus.text = "正在加载词库: $fileName..."
                }
                
                viewModel.initializeWords(allWords)
                binding.textInitializeStatus.text = getString(R.string.word_data_initialized, allWords.size)
                
                // 标记数据已初始化
                sharedPreferences.edit()
                    .putBoolean("data_initialized", true)
                    .apply()
                    
                // 更新显示的单词总数
                updateWordCountDisplay()
                
                // 发送广播通知首页刷新数据
                val intent = android.content.Intent("com.justbyheart.vocabulary.WORD_BANK_CHANGED")
                requireActivity().sendBroadcast(intent)
            } catch (e: Exception) {
                binding.textInitializeStatus.text = getString(R.string.initialization_failed, e.message)
            } finally {
                binding.buttonInitializeData.isEnabled = true
            }
        }
    }
    
    /**
     * 更新单词总数显示
     */
    private fun updateWordCountDisplay() {
        lifecycleScope.launch {
            try {
                // 获取当前词库
                val currentWordBank = sharedPreferences.getString(KEY_CURRENT_WORD_BANK, DEFAULT_WORD_BANK) ?: DEFAULT_WORD_BANK
                val wordCount = viewModel.getWordCountByWordBank(currentWordBank)
                binding.textInitializeStatus.text = getString(R.string.word_data_initialized, wordCount)
                binding.textInitializeStatus.visibility = View.VISIBLE
            } catch (e: Exception) {
                // 如果获取单词数量失败，保持原状态
            }
        }
    }
    
    /**
     * 检查数据初始化状态
     */
    private fun checkInitializationStatus() {
        val isDataInitialized = sharedPreferences.getBoolean("data_initialized", false)
        if (isDataInitialized) {
            // 如果数据已经初始化，禁用按钮并显示提示
            binding.buttonInitializeData.isEnabled = false
            binding.buttonInitializeData.text = getString(R.string.data_already_initialized)
            
            // 显示初始化状态
            updateWordCountDisplay()
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
    
    /**
     * 导出数据到JSON文件
     */
    private fun exportData() {
        binding.buttonExportData.isEnabled = false
        binding.textInitializeStatus.text = "正在准备导出数据..."
        binding.textInitializeStatus.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                // 获取所有词库
                val wordBanks = WordDataLoader.getAvailableWordBanks()
                val exportData = mutableListOf<ExportWordBank>()
                
                // 日期格式化器
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                
                for (wordBank in wordBanks) {
                    // 获取已背诵的单词及背诵时间
                    val completedWordsWithRecords = viewModel.getCompletedWordsWithRecordsByWordBank(wordBank)
                    val memorizedWords = completedWordsWithRecords.map { (word, record) ->
                        listOf(
                            word.english,
                            word.chinese,
                            dateFormat.format(record.studyDate) // 使用学习记录中的实际时间
                        )
                    }
                    
                    // 获取收藏的单词
                    val favoriteWords = viewModel.getFavoriteWordsByWordBank(wordBank)
                    val favoriteWordNames = favoriteWords.map { it.english }
                    
                    // 创建导出数据对象
                    val exportWordBank = ExportWordBank(
                        wordBankName = wordBank,
                        memorizedWords = memorizedWords,
                        favoriteWords = favoriteWordNames
                    )
                    
                    exportData.add(exportWordBank)
                }
                
                // 将数据转换为JSON格式
                val gson = GsonBuilder().setPrettyPrinting().create()
                val json = gson.toJson(exportData)
                
                // 创建文件
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val fileName = "vocabulary_data_${System.currentTimeMillis()}.json"
                val file = File(downloadsDir, fileName)
                
                // 写入文件
                FileWriter(file).use { writer ->
                    writer.write(json)
                }
                
                binding.textInitializeStatus.text = "数据导出成功: ${file.absolutePath}"
                Toast.makeText(context, "数据导出成功: ${file.name}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                binding.textInitializeStatus.text = "导出失败: ${e.message}"
                Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.buttonExportData.isEnabled = true
            }
        }
    }
    
    /**
     * 从JSON文件导入数据
     */
    private fun importData(uri: Uri) {
        binding.buttonImportData.isEnabled = false
        binding.textInitializeStatus.text = "正在导入数据..."
        binding.textInitializeStatus.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                // 读取文件内容
                val jsonString = requireContext().contentResolver.openInputStream(uri)?.bufferedReader().use { reader ->
                    reader?.readText()
                }
                
                if (jsonString.isNullOrEmpty()) {
                    throw Exception("文件内容为空")
                }
                
                // 解析JSON
                val gson = Gson()
                val listType = object : TypeToken<ExportData>() {}.type
                val importData: ExportData = gson.fromJson(jsonString, listType)
                
                // 处理导入的数据
                var importedRecords = 0
                for (wordBankData in importData) {
                    val wordBankName = wordBankData.wordBankName
                    
                    // 处理已背诵的单词
                    for (memorizedWord in wordBankData.memorizedWords) {
                        if (memorizedWord.size >= 3) {
                            val englishWord = memorizedWord[0]
                            val memorizationDateStr = memorizedWord[2]
                            
                            // 查找对应单词的ID
                            val word = viewModel.getWordByEnglishAndWordBank(englishWord, wordBankName)
                            if (word != null) {
                                // 解析日期
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                val memorizationDate = dateFormat.parse(memorizationDateStr)
                                
                                if (memorizationDate != null) {
                                    // 检查是否已存在相同日期的学习记录
                                    val existingRecord = viewModel.getStudyRecordByWordIdAndDate(word.id, memorizationDate)
                                    
                                    if (existingRecord != null) {
                                        // 更新现有记录
                                        val updatedRecord = existingRecord.copy(isCompleted = true)
                                        viewModel.updateStudyRecord(updatedRecord)
                                    } else {
                                        // 创建新记录
                                        val newRecord = StudyRecord(
                                            wordId = word.id,
                                            studyDate = memorizationDate,
                                            isCompleted = true
                                        )
                                        viewModel.insertStudyRecord(newRecord)
                                    }
                                    
                                    importedRecords++
                                }
                            }
                        }
                    }
                    
                    // 处理收藏的单词
                    for (favoriteWordName in wordBankData.favoriteWords) {
                        val word = viewModel.getWordByEnglishAndWordBank(favoriteWordName, wordBankName)
                        if (word != null) {
                            val isFavorite = viewModel.isWordFavorite(word.id)
                            if (!isFavorite) {
                                viewModel.insertFavoriteWord(word.id)
                                importedRecords++
                            }
                        }
                    }
                }
                
                binding.textInitializeStatus.text = "数据导入完成，共导入 $importedRecords 条记录"
                Toast.makeText(context, "数据导入完成，共导入 $importedRecords 条记录", Toast.LENGTH_LONG).show()
                
                // 发送广播通知其他界面刷新
                val intent = android.content.Intent("com.justbyheart.vocabulary.DATA_IMPORTED")
                requireActivity().sendBroadcast(intent)
            } catch (e: Exception) {
                binding.textInitializeStatus.text = "导入失败: ${e.message}"
                Toast.makeText(context, "导入失败: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.buttonImportData.isEnabled = true
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}