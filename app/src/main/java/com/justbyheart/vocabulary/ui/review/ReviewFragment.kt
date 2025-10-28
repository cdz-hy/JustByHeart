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
import com.justbyheart.vocabulary.databinding.FragmentReviewBinding
import java.text.SimpleDateFormat
import java.util.*

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
        viewModel.loadStudyDates()
    }
    
    private fun setupRecyclerView() {
        wordAdapter = ReviewWordAdapter()
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
                
                viewModel.loadWordsForDate(selectedDate)
                
                val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
                binding.textSelectedDate.text = dateFormat.format(selectedDate)
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
            
            binding.textNoWords.visibility = if (words.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewWords.visibility = if (words.isEmpty()) View.GONE else View.VISIBLE
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