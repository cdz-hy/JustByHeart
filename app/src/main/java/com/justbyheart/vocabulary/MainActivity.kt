package com.justbyheart.vocabulary

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.justbyheart.vocabulary.databinding.ActivityMainBinding

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
    }
    
    /**
     * 设置导航组件
     * 
     * 配置NavController和底部导航栏的关联，
     * 实现点击底部导航项时自动切换对应的Fragment。
     */
    private fun setupNavigation() {
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
    }
}