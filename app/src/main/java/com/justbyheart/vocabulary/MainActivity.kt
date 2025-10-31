package com.justbyheart.vocabulary

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.databinding.ActivityMainBinding
import com.justbyheart.vocabulary.utils.WordDataLoader
import kotlinx.coroutines.launch

/**
 * 应用主活动类
 * 
 * 作为应用的入口点，负责设置主界面布局和底部导航栏。
 * 使用Navigation Component管理Fragment之间的导航。
 * 
 * 主要功能：
 * - 初始化应用主界面
 * - 设置底部导航栏
 * - 管理Fragment容器
 */
class MainActivity : AppCompatActivity() {
    
    // 使用ViewBinding进行视图绑定，避免findViewById的使用
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController // 声明 NavController
    private lateinit var rootDestinations: Set<Int>

    // 双击退出回调
    private val onBackPressedCallback = object : OnBackPressedCallback(false) { // 初始禁用
        private var backPressedTime: Long = 0
        private val EXIT_TIME_INTERVAL = 2000L // 2秒

        override fun handleOnBackPressed() {
            if (System.currentTimeMillis() - backPressedTime < EXIT_TIME_INTERVAL) {
                finish() // 退出应用
            } else {
                backPressedTime = System.currentTimeMillis()
                Toast.makeText(this@MainActivity, R.string.press_again_to_exit, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Activity创建时的回调方法
     * 
     * 初始化界面布局和导航组件
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 在 onCreate 中初始化 rootDestinations
        rootDestinations = setOf(
            R.id.navigation_home,
            R.id.navigation_study,
            R.id.navigation_review,
            R.id.navigation_favorites,
            R.id.navigation_settings,
            R.id.testFragment
        )

        setupNavigation()

        // 注册返回键回调
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // 监听导航目的地变化，动态启用/禁用回调
        navController.addOnDestinationChangedListener { _, destination, _ ->
            onBackPressedCallback.isEnabled = destination.id in rootDestinations
        }
        
        // 检查是否已初始化数据
        val sharedPreferences = getSharedPreferences("vocabulary_settings", MODE_PRIVATE)
        val isDataInitialized = sharedPreferences.getBoolean("data_initialized", false)
        
        if (!isDataInitialized) {
            showInitialWordBankSelection()
        }
    }
    
    /**
     * 显示初始词库选择对话框
     */
    private fun showInitialWordBankSelection() {
        val wordBanks = WordDataLoader.getAvailableWordBanks().toTypedArray()
        var selectedIndex = 0

        AlertDialog.Builder(this)
            .setTitle("选择词库")
            .setSingleChoiceItems(wordBanks, selectedIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton("确定") { _, _ ->
                val selectedWordBank = wordBanks[selectedIndex]
                initializeWordBankData(selectedWordBank)
            }
            .setCancelable(false)
            .show()
    }

    /**
     * 初始化词库数据
     */
    private fun initializeWordBankData(wordBank: String) {
        lifecycleScope.launch {
            try {
                val repository = getRepository()
                
                // 获取词库对应的文件名
                val fileNames = WordDataLoader.getWordBankFileNames(wordBank)

                // 加载所有相关文件的单词
                val allWords = mutableListOf<com.justbyheart.vocabulary.data.entity.Word>()
                for (fileName in fileNames) {
                    val words = WordDataLoader.loadWordsFromAssets(this@MainActivity, fileName)
                    allWords.addAll(words)
                }

                // 保存到数据库
                repository.insertWords(allWords)

                // 保存当前词库设置
                val sharedPreferences = getSharedPreferences("vocabulary_settings", MODE_PRIVATE)
                sharedPreferences.edit()
                    .putString("current_word_bank", wordBank)
                    .putBoolean("data_initialized", true)
                    .apply()

                Toast.makeText(this@MainActivity, "词库初始化完成", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // 出错时显示错误信息并重新选择
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("初始化失败")
                    .setMessage("词库初始化失败: ${e.message}")
                    .setPositiveButton("重试") { _, _ ->
                        showInitialWordBankSelection()
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }
    
    /**
     * 设置导航组件
     * 
     * 配置NavController和底部导航栏的关联，
     * 实现点击底部导航项时自动切换对应的Fragment。
     */
    private fun setupNavigation() {
        try {
            // 获取NavHostFragment实例
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            
            // 获取NavController
            navController = navHostFragment.navController
            
            // 将底部导航栏与NavController关联
            // 这样点击底部导航项时会自动导航到对应的Fragment
            val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNavigation.setupWithNavController(navController)
            
            // 解决部分情况下无法跳转到首页的问题
            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        // 使用navigate方法确保能正确跳转到首页
                        navController.navigate(R.id.navigation_home)
                        true
                    }
                    R.id.navigation_study -> {
                        navController.navigate(R.id.navigation_study)
                        true
                    }
                    R.id.navigation_review -> {
                        navController.navigate(R.id.navigation_review)
                        true
                    }
                    R.id.navigation_favorites -> {
                        navController.navigate(R.id.navigation_favorites)
                        true
                    }
                    R.id.navigation_settings -> {
                        navController.navigate(R.id.navigation_settings)
                        true
                    }
                    else -> false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 获取WordRepository实例
     * 
     * @return WordRepository实例
     */
    fun getRepository(): WordRepository {
        return (application as VocabularyApplication).repository
    }
}