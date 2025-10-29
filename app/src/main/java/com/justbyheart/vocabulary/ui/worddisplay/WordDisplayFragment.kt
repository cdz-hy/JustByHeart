package com.justbyheart.vocabulary.ui.worddisplay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.justbyheart.vocabulary.R
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.databinding.FragmentWordDisplayBinding

class WordDisplayFragment : Fragment() {

    private var _binding: FragmentWordDisplayBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WordDisplayViewModel
    private val args: WordDisplayFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordDisplayBinding.inflate(inflater, container, false)

        val database = VocabularyDatabase.getDatabase(requireContext())
        val repository = WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )

        viewModel = ViewModelProvider(
            this,
            WordDisplayViewModelFactory(repository)
        )[WordDisplayViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val wordId = args.wordId
        viewModel.loadWord(wordId)

        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonFavorite.setOnClickListener {
            viewModel.toggleFavorite()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.word.observe(viewLifecycleOwner) { word ->
            if (word != null) {
                binding.textEnglish.text = word.english
                binding.textPronunciation.text = word.pronunciation
                binding.textChinese.text = word.chinese
                binding.textDefinition.text = word.definition

                // Display examples
                val examples = word.example?.split("\n") ?: emptyList()
                val exampleTranslations = word.exampleTranslation?.split("\n") ?: emptyList()

                val formattedExamples = StringBuilder()
                for (i in 0 until minOf(examples.size, exampleTranslations.size)) {
                    formattedExamples.append(examples[i]).append("\n")
                    formattedExamples.append(exampleTranslations[i]).append("\n\n")
                }
                binding.textExample.text = formattedExamples.toString().trimEnd() // Remove trailing newlines
            } else {
                Toast.makeText(context, R.string.word_not_found, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            binding.buttonFavorite.isSelected = isFavorite
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}