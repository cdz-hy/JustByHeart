package com.justbyheart.vocabulary.ui.test

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.justbyheart.vocabulary.R
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.databinding.FragmentTestBinding

class TestFragment : Fragment() {
    
    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: TestViewModel
    private var vibrator: Vibrator? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        
        val database = VocabularyDatabase.getDatabase(requireContext())
        val repository = WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )
        
        viewModel = ViewModelProvider(
            this,
            TestViewModelFactory(repository)
        )[TestViewModel::class.java]
        
        // 初始化震动器
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = requireContext().getSystemService(VibratorManager::class.java)
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            requireContext().getSystemService(Vibrator::class.java)
        }
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()

        // 根据ViewModel的状态决定初始显示
        if (viewModel.testResult.value != null) {
            showTestResult(viewModel.testResult.value!!)
        } else {
            binding.layoutQuestion.visibility = View.VISIBLE
            binding.layoutResult.visibility = View.GONE
            // 从导航参数中获取已翻转的单词ID，并开始测试
            val args = TestFragmentArgs.fromBundle(requireArguments())
            if (viewModel.currentQuestion.value == null) { // 避免重复开始测试
                viewModel.startTest(args.wordIds)
            }
        }
    }
    
    private fun setupUI() {
        // 添加返回按钮
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
        
        binding.buttonOption1.setOnClickListener { viewModel.selectAnswer(0) }
        binding.buttonOption2.setOnClickListener { viewModel.selectAnswer(1) }
        binding.buttonOption3.setOnClickListener { viewModel.selectAnswer(2) }
        binding.buttonOption4.setOnClickListener { viewModel.selectAnswer(3) }
        
        binding.buttonNext.setOnClickListener {
            viewModel.nextQuestion()
        }
        
        binding.buttonFinish.setOnClickListener {
            // 确保正确返回到学习界面
            findNavController().popBackStack(R.id.navigation_study, false)
        }
        
        // 为错误单词列表中的项目设置点击事件
        binding.recyclerViewIncorrectWords.adapter = IncorrectWordsAdapter { wordId ->
            val action: NavDirections = TestFragmentDirections.actionTestFragmentToWordDisplayFragment(wordId)
            // 使用特定的导航选项确保正确的返回行为
            findNavController().navigate(action)
        }
    }
    
    private fun observeViewModel() {
        viewModel.currentQuestion.observe(viewLifecycleOwner) { question ->
            binding.layoutResult.visibility = View.GONE
            binding.layoutQuestion.visibility = View.VISIBLE
            question?.let {
                binding.textQuestion.text = it.question
                binding.buttonOption1.text = it.options[0]
                binding.buttonOption2.text = it.options[1]
                binding.buttonOption3.text = it.options[2]
                binding.buttonOption4.text = it.options[3]
                
                resetOptionButtons()
                binding.buttonNext.visibility = View.GONE
            }
        }
        
        viewModel.selectedAnswer.observe(viewLifecycleOwner) { selectedIndex ->
            if (selectedIndex != -1) {
                highlightSelectedOption(selectedIndex)
                binding.buttonNext.visibility = View.VISIBLE
            }
        }
        
        viewModel.showCorrectAnswer.observe(viewLifecycleOwner) { correctIndex ->
            if (correctIndex != -1) {
                highlightCorrectAnswer(correctIndex)
                
                // 震动反馈
                val selectedIndex = viewModel.selectedAnswer.value ?: -1
                if (selectedIndex == correctIndex) {
                    // 正确答案，轻微震动一下
                    vibrateDevice(longArrayOf(0, 50), -1)
                } else {
                    // 错误答案，较重震动两下
                    vibrateDevice(longArrayOf(0, 100, 100, 100), -1)
                }
            }
        }
        
        viewModel.currentQuestionIndex.observe(viewLifecycleOwner) { index ->
            val total = viewModel.getTotalQuestions()
            binding.textProgress.text = "${index + 1}/$total"
        }
        
        viewModel.testResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                showTestResult(it)
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
    
    private fun resetOptionButtons() {
        listOf(
            binding.buttonOption1,
            binding.buttonOption2,
            binding.buttonOption3,
            binding.buttonOption4
        ).forEach { button ->
            button.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
            button.isEnabled = true
        }
    }
    
    private fun highlightSelectedOption(selectedIndex: Int) {
        val buttons = listOf(
            binding.buttonOption1,
            binding.buttonOption2,
            binding.buttonOption3,
            binding.buttonOption4
        )
        
        buttons[selectedIndex].setBackgroundColor(
            resources.getColor(R.color.md_theme_light_primaryContainer, null)
        )
    }
    
    private fun highlightCorrectAnswer(correctIndex: Int) {
        val buttons = listOf(
            binding.buttonOption1,
            binding.buttonOption2,
            binding.buttonOption3,
            binding.buttonOption4
        )
        
        buttons.forEach { it.isEnabled = false }
        
        buttons[correctIndex].setBackgroundColor(
            resources.getColor(R.color.success_green, null)
        )
        
        val selectedIndex = viewModel.selectedAnswer.value ?: -1
        if (selectedIndex != correctIndex && selectedIndex != -1) {
            buttons[selectedIndex].setBackgroundColor(
                resources.getColor(R.color.error_red, null)
            )
        }
    }
    
    private fun showTestResult(result: TestResult) {
        binding.layoutQuestion.visibility = View.GONE
        binding.layoutResult.visibility = View.VISIBLE
        
        binding.textScore.text = "${result.correctAnswers}/${result.totalQuestions}"
        binding.textScorePercentage.text = "${(result.correctAnswers * 100 / result.totalQuestions)}%"
        
        // 显示测试单词数和错误单词列表
        binding.textTestedWordsCount.text = getString(R.string.tested_words_count, result.totalQuestions / 2)
        
        // 设置错误单词列表
        if (result.incorrectWords.isNotEmpty()) {
            binding.textIncorrectWordsTitle.visibility = View.VISIBLE
            binding.recyclerViewIncorrectWords.visibility = View.VISIBLE
            
            val adapter = binding.recyclerViewIncorrectWords.adapter as IncorrectWordsAdapter
            adapter.submitList(result.incorrectWords)
        } else {
            binding.textIncorrectWordsTitle.visibility = View.GONE
            binding.recyclerViewIncorrectWords.visibility = View.GONE
        }
        
        binding.buttonFinish.visibility = View.VISIBLE
    }
    
    private fun vibrateDevice(pattern: LongArray, repeat: Int) {
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createWaveform(pattern, repeat)
                v.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(pattern, repeat)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}