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
import com.justbyheart.vocabulary.ui.search.WordSearchAdapter // 添加了WordSearchAdapter导入

/**
 * 搜索功能Fragment
 * 提供单词搜索界面和功能
 */
class SearchFragment : Fragment(), SearchView.OnQueryTextListener {

    // 视图绑定
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    // ViewModel和适配器实例
    private lateinit var viewModel: SearchViewModel
    private lateinit var searchAdapter: WordSearchAdapter // 更改为WordSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 初始化视图绑定
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        // 获取数据库实例和仓库
        val database = VocabularyDatabase.getDatabase(requireContext())
        val repository = WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )

        // 初始化ViewModel
        viewModel = ViewModelProvider(
            this,
            SearchViewModelFactory(repository)
        )[SearchViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置RecyclerView、SearchView和观察ViewModel
        setupRecyclerView()
        setupSearchView()
        observeViewModel()

        // 处理从其他页面传递过来的搜索关键词
        val query = arguments?.getString("query")
        if (!query.isNullOrEmpty()) {
            binding.searchViewFull.setQuery(query, true)
        }
    }

    /**
     * 设置RecyclerView
     */
    private fun setupRecyclerView() {
        // 创建适配器并设置点击事件处理
        searchAdapter = WordSearchAdapter { word -> // 更改为WordSearchAdapter
            // 处理搜索结果点击：导航到完整单词卡片视图
            val bundle = Bundle()
            bundle.putLong("wordId", word.id)
            findNavController().navigate(com.justbyheart.vocabulary.R.id.action_navigation_search_to_wordCardDisplayFragment, bundle)
        }
        binding.recyclerViewSearchResults.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
        }
    }

    /**
     * 设置搜索视图
     */
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

    /**
     * 当用户提交搜索查询时调用
     */
    override fun onQueryTextSubmit(query: String?): Boolean {
        viewModel.searchWords(query ?: "")
        return true
    }

    /**
     * 当搜索框中的文本发生变化时调用
     */
    override fun onQueryTextChange(newText: String?): Boolean {
        viewModel.searchWords(newText ?: "")
        return true
    }

    /**
     * 观察ViewModel中的数据变化
     */
    private fun observeViewModel() {
        // 观察搜索结果的变化
        viewModel.searchResults.observe(viewLifecycleOwner) { words ->
            searchAdapter.submitList(words)
            binding.textEmptySearchResults.visibility = if (words.isEmpty()) View.VISIBLE else View.GONE
        }

        // 观察加载状态的变化
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarSearch.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}