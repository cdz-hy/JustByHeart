package com.ielts.vocabulary.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ielts.vocabulary.R
import com.ielts.vocabulary.data.database.VocabularyDatabase
import com.ielts.vocabulary.data.repository.WordRepository
import com.ielts.vocabulary.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HomeViewModel
    
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
        
        setupUI()
        observeViewModel()
        viewModel.loadTodayProgress()
    }
    
    private fun setupUI() {
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        binding.textCurrentDate.text = dateFormat.format(Date())
        
        binding.buttonStartStudy.setOnClickListener {
            findNavController().navigate(R.id.navigation_study)
        }
        
        binding.cardDailyGoal.setOnClickListener {
            findNavController().navigate(R.id.navigation_settings)
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
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}