package com.justbyheart.vocabulary.ui.review

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.repository.WordRepository
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*
// 导入正确的ViewBinding类
import com.justbyheart.vocabulary.databinding.FragmentReviewBinding

class ReviewFragment : Fragment() {
    
    private var _binding: FragmentReviewBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ReviewViewModel
    private lateinit var wordAdapter: ReviewWordAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewBinding.inflate(inflater, container, false)
        
        val database = VocabularyDatabase.getDatabase(requireContext())
        val repository = WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )
        
        viewModel = ViewModelProvider(
            this,
            ReviewViewModelFactory(repository)
        )[ReviewViewModel::class.java]
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupUI()
        observeViewModel()
        // 默认显示本日已背诵的单词
        val today = viewModel.getTodayZeroed()
        viewModel.loadWordsForDate(today, requireContext())
        val dateFormat = SimpleDateFormat("MM月dd日", Locale.getDefault())
        binding.textSelectedDate.text = dateFormat.format(today)
        binding.textSelectedDate.visibility = View.VISIBLE
    }
    
    private fun setupRecyclerView() {
        wordAdapter = ReviewWordAdapter { word ->
            val action = ReviewFragmentDirections.actionReviewFragmentToWordDisplayFragment(word.id)
            findNavController().navigate(action)
        }
        binding.recyclerViewWords.apply {
            adapter = wordAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun setupUI() {
        binding.buttonSelectDate.setOnClickListener {
            showDatePicker()
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                
                viewModel.loadWordsForDate(selectedDate, requireContext())
                
                val dateFormat = SimpleDateFormat("MM月dd日", Locale.getDefault())
                binding.textSelectedDate.text = dateFormat.format(selectedDate)
                binding.textSelectedDate.visibility = View.VISIBLE
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.show()
    }
    
    private fun observeViewModel() {
        viewModel.reviewWords.observe(viewLifecycleOwner) { words ->
            wordAdapter.submitList(words)
            
            if (words.isEmpty()) {
                binding.textNoWords.visibility = View.VISIBLE
                binding.recyclerViewWords.visibility = View.GONE
            } else {
                binding.textNoWords.visibility = View.GONE
                binding.recyclerViewWords.visibility = View.VISIBLE
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