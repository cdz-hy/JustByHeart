package com.justbyheart.vocabulary.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.databinding.FragmentFavoritesBinding
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest

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
        // 加载当前词库的收藏单词
        viewModel.loadFavoriteWords(requireContext())
    }
    
    private fun setupRecyclerView() {
        favoriteAdapter = FavoriteWordAdapter({
            viewModel.removeFromFavorites(it.id)
        }, {
            val action = FavoritesFragmentDirections.actionFavoritesFragmentToWordDisplayFragment(it.id)
            findNavController().navigate(action)
        })
        
        binding.recyclerViewFavorites.apply {
            adapter = favoriteAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoriteWords.collectLatest { words ->
                favoriteAdapter.submitList(words)
                
                binding.textNoFavorites.visibility = if (words.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerViewFavorites.visibility = if (words.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}