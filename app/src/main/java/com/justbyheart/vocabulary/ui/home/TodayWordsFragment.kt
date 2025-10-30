package com.justbyheart.vocabulary.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.databinding.FragmentTodayWordsBinding
import com.justbyheart.vocabulary.ui.library.LibraryWordAdapter
import kotlinx.coroutines.launch

class TodayWordsFragment : Fragment() {
    
    private var _binding: FragmentTodayWordsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var wordAdapter: LibraryWordAdapter
    private lateinit var repository: WordRepository
    private val args: TodayWordsFragmentArgs by navArgs()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayWordsBinding.inflate(inflater, container, false)
        
        val database = VocabularyDatabase.getDatabase(requireContext())
        repository = WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupUI()
        loadTodayWords()
    }
    
    private fun setupRecyclerView() {
        wordAdapter = LibraryWordAdapter { word ->
            val action = TodayWordsFragmentDirections.actionTodayWordsFragmentToWordDisplayFragment(word.id)
            findNavController().navigate(action)
        }
        
        binding.recyclerViewTodayWords.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = wordAdapter
        }
    }
    
    private fun setupUI() {
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
    
    private fun loadTodayWords() {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val wordIds = args.wordIds.toList()
                val words = if (wordIds.isNotEmpty()) {
                    repository.getWordsByIds(wordIds)
                } else {
                    emptyList()
                }
                
                binding.progressBar.visibility = View.GONE
                if (words.isEmpty()) {
                    binding.textEmpty.visibility = View.VISIBLE
                } else {
                    binding.textEmpty.visibility = View.GONE
                    wordAdapter.submitList(words)
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.textEmpty.visibility = View.VISIBLE
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}