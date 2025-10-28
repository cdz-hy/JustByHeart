package com.justbyheart.vocabulary.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.databinding.FragmentFavoritesBinding

class FavoritesFragment : Fragment() {
    
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: FavoritesViewModel
    private lateinit var favoriteAdapter: FavoriteWordAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        
        val database = VocabularyDatabase.getDatabase(requireContext())
        val repository = WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )
        
        viewModel = ViewModelProvider(
            this,
            FavoritesViewModelFactory(repository)
        )[FavoritesViewModel::class.java]
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        favoriteAdapter = FavoriteWordAdapter { word ->
            viewModel.removeFromFavorites(word.id)
        }
        
        binding.recyclerViewFavorites.apply {
            adapter = favoriteAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun observeViewModel() {
        viewModel.favoriteWords.observe(viewLifecycleOwner) { words ->
            favoriteAdapter.submitList(words)
            
            binding.textNoFavorites.visibility = if (words.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewFavorites.visibility = if (words.isEmpty()) View.GONE else View.VISIBLE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}