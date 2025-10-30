package com.justbyheart.vocabulary

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.lifecycleScope
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.utils.WordDataLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VocabularyApplication : Application() {
    
    val database by lazy { VocabularyDatabase.getDatabase(this) }
    val repository by lazy {
        WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )
    }
    
    private lateinit var sharedPreferences: SharedPreferences
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        sharedPreferences = getSharedPreferences("vocabulary_settings", Context.MODE_PRIVATE)
        
        // 初始化单词数据（仅在首次启动时）
        initializeWordsIfNeeded()
    }
    
    private fun initializeWordsIfNeeded() {
        // 检查是否已经初始化过数据
        val isDataInitialized = sharedPreferences.getBoolean("data_initialized", false)
        
        if (!isDataInitialized) {
            // 在后台线程中初始化数据
            applicationScope.launch(Dispatchers.IO) {
                try {
                    // 从assets目录加载单词数据
                    val words = WordDataLoader.loadWordsFromAssets(this@VocabularyApplication)
                    
                    // 插入到数据库
                    repository.insertWords(words)
                    
                    // 标记数据已初始化
                    sharedPreferences.edit()
                        .putBoolean("data_initialized", true)
                        .apply()
                } catch (e: Exception) {
                    // 初始化失败，下次启动时重试
                    e.printStackTrace()
                }
            }
        }
    }
}