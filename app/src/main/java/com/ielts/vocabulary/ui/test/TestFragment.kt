package com.ielts.vocabulary.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ielts.vocabulary.R
import com.ielts.vocabulary.data.database.VocabularyDatabase
import com.ielts.vocabulary.data.repository.WordRepository
import com.ielts.vocabulary.databinding.FragmentTestBinding

class TestFragment : Fragment() {
    
    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: TestViewModel
    
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
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        viewModel.startTest()
    }
    
    private fun setupUI() {
        binding.buttonOption1.setOnClickListener { viewModel.selectAnswer(0) }
        binding.buttonOption2.setOnClickListener { viewModel.selectAnswer(1) }
        binding.buttonOption3.setOnClickListener { viewModel.selectAnswer(2) }
        binding.buttonOption4.setOnClickListener { viewModel.selectAnswer(3) }
        
        binding.buttonNext.setOnClickListener {
            viewModel.nextQuestion()
        }
        
        binding.buttonFinish.setOnClickListener {
            findNavController().popBackStack()
        }
    }
    
    private fun observeViewModel() {
        viewModel.currentQuestion.observe(viewLifecycleOwner) { question ->
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
            binding.layoutQuestion.visibility = if (isLoading) View.GONE else View.VISIBLE
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
        
        binding.buttonFinish.visibility = View.VISIBLE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}