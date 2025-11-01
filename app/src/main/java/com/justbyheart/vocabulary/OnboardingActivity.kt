package com.justbyheart.vocabulary

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.entity.DailyGoal
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.databinding.ActivityOnboardingBinding
import com.justbyheart.vocabulary.utils.WordDataLoader
import kotlinx.coroutines.launch
import java.util.*

class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var wordBanks: Array<String>
    private var selectedWordBankIndex = 0
    private var dailyGoal = 10 // 默认每日目标
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 检查是否已完成引导流程
        val sharedPreferences = getSharedPreferences("vocabulary_settings", Context.MODE_PRIVATE)
        val isOnboardingCompleted = sharedPreferences.getBoolean("onboarding_completed", false)
        
        if (isOnboardingCompleted) {
            // 如果已完成引导，则直接跳转到主界面
            startMainActivity()
            return
        }
        
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupWordBankSelection()
        setupDailyGoalSelection()
        setupGetStartedButton()
    }
    
    private fun setupWordBankSelection() {
        wordBanks = WordDataLoader.getAvailableWordBanks().toTypedArray()
        
        val listView = binding.listWordBanks
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_single_choice,
            wordBanks
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                if (view is TextView) {
                    view.setTextColor(getColor(R.color.md_theme_light_onSurface))
                    view.textSize = 16f
                    view.setPadding(16, 32, 16, 32)
                }
                return view
            }
        }
        
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE
        listView.setItemChecked(0, true) // 默认选中第一个
        
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            selectedWordBankIndex = position
        }
    }
    
    private fun setupDailyGoalSelection() {
        val toggleGroup = binding.toggleGroupDailyGoal
        val button10 = binding.button10
        val button30 = binding.button30
        val button50 = binding.button50
        
        // 默认选中10个
        button10.isChecked = true
        
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                dailyGoal = when (checkedId) {
                    R.id.button_10 -> 10
                    R.id.button_30 -> 30
                    R.id.button_50 -> 50
                    else -> 10
                }
            }
        }
    }
    
    private fun setupGetStartedButton() {
        binding.buttonGetStarted.setOnClickListener {
            val selectedWordBank = wordBanks[selectedWordBankIndex]
            initializeAppData(selectedWordBank)
        }
    }
    
    private fun initializeAppData(wordBank: String) {
        lifecycleScope.launch {
            try {
                val database = VocabularyDatabase.getDatabase(this@OnboardingActivity)
                val repository = WordRepository(
                    database.wordDao(),
                    database.studyRecordDao(),
                    database.favoriteWordDao(),
                    database.dailyGoalDao()
                )
                
                // 获取词库对应的文件名
                val fileNames = WordDataLoader.getWordBankFileNames(wordBank)

                // 加载所有相关文件的单词
                val allWords = mutableListOf<com.justbyheart.vocabulary.data.entity.Word>()
                for (fileName in fileNames) {
                    val words = WordDataLoader.loadWordsFromAssets(this@OnboardingActivity, fileName)
                    allWords.addAll(words)
                }

                // 保存到数据库
                repository.insertWords(allWords)
                
                // 设置每日目标
                val today = Date() // 使用 Date 而不是 LocalDate
                val dailyGoalEntity = DailyGoal(
                    date = today,
                    targetWordCount = dailyGoal, // 正确的参数名
                    completedWordCount = 0 // 正确的参数名
                )
                repository.insertDailyGoal(dailyGoalEntity)

                // 保存设置
                val sharedPreferences = getSharedPreferences("vocabulary_settings", Context.MODE_PRIVATE)
                sharedPreferences.edit()
                    .putString("current_word_bank", wordBank)
                    .putInt("daily_goal", dailyGoal)  // 修复：使用正确的键名保存每日目标
                    .putInt("daily_word_count", dailyGoal)  // 添加：与SettingsFragment中使用的键保持一致
                    .putBoolean("data_initialized", true)
                    .putBoolean("onboarding_completed", true)
                    .apply()

                // 跳转到主Activity
                startMainActivity()
            } catch (e: Exception) {
                Toast.makeText(this@OnboardingActivity, "初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun startMainActivity() {
        val intent = Intent(this@OnboardingActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}