package com.justbyheart.vocabulary.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.databinding.FragmentLibraryBinding

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LibraryViewModel
    private lateinit var wordAdapter: LibraryWordAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)

        val database = VocabularyDatabase.getDatabase(requireContext())
        val repository = WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )

        viewModel = ViewModelProvider(this, LibraryViewModelFactory(repository))[LibraryViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabLayout()
        observeViewModel()

        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.loadUncompletedWords(requireContext())
    }

    private fun setupRecyclerView() {
        wordAdapter = LibraryWordAdapter { word ->
            val action = LibraryFragmentDirections.actionLibraryFragmentToWordDisplayFragment(word.id)
            findNavController().navigate(action)
        }
        binding.recyclerViewLibrary.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = wordAdapter
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.loadUncompletedWords(requireContext())
                    1 -> viewModel.loadCompletedWords(requireContext())
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeViewModel() {
        viewModel.uncompletedWords.observe(viewLifecycleOwner) {
            wordAdapter.submitList(it)
        }

        viewModel.completedWords.observe(viewLifecycleOwner) {
            wordAdapter.submitList(it)
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次返回此页面时重新加载数据
        viewModel.loadUncompletedWords(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}