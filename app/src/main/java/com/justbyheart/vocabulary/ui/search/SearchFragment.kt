package com.justbyheart.vocabulary.ui.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.databinding.FragmentSearchBinding
import com.justbyheart.vocabulary.ui.search.WordSearchAdapter // Added

class SearchFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SearchViewModel
    private lateinit var searchAdapter: WordSearchAdapter // Changed to WordSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        val database = VocabularyDatabase.getDatabase(requireContext())
        val repository = WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )

        viewModel = ViewModelProvider(
            this,
            SearchViewModelFactory(repository)
        )[SearchViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        observeViewModel()

        val query = arguments?.getString("query")
        if (!query.isNullOrEmpty()) {
            binding.searchViewFull.setQuery(query, true)
        }
    }

    private fun setupRecyclerView() {
        searchAdapter = WordSearchAdapter { word -> // Changed to WordSearchAdapter
            // Handle click on search result: navigate to full word card view
            val bundle = Bundle()
            bundle.putLong("wordId", word.id)
            findNavController().navigate(com.justbyheart.vocabulary.R.id.action_navigation_search_to_wordCardDisplayFragment, bundle)
        }
        binding.recyclerViewSearchResults.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
        }
    }

    private fun setupSearchView() {
        // 设置SearchView的查询文本监听器
        binding.searchViewFull.setOnQueryTextListener(this)

        // 使整个SearchView可点击
        binding.searchViewFull.setOnClickListener {
            binding.searchViewFull.isIconified = false
        }

        // 以编程方式请求焦点并显示软键盘
        binding.searchViewFull.postDelayed({
            binding.searchViewFull.performClick()
            binding.searchViewFull.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.showSoftInput(binding.searchViewFull, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        viewModel.searchWords(query ?: "")
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        viewModel.searchWords(newText ?: "")
        return true
    }

    private fun observeViewModel() {
        viewModel.searchResults.observe(viewLifecycleOwner) { words ->
            searchAdapter.submitList(words)
            binding.textEmptySearchResults.visibility = if (words.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarSearch.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}