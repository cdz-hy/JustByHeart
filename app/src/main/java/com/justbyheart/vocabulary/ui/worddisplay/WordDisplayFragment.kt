package com.justbyheart.vocabulary.ui.worddisplay

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.justbyheart.vocabulary.R
import com.justbyheart.vocabulary.data.database.VocabularyDatabase
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.data.repository.WordRepository
import com.justbyheart.vocabulary.databinding.FragmentWordDisplayBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.min

class WordDisplayFragment : Fragment() {

    private var _binding: FragmentWordDisplayBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WordDisplayViewModel
    private val args: WordDisplayFragmentArgs by navArgs()
    private var currentWord: Word? = null

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

        // 添加播放按钮点击事件
        binding.buttonSpeak.setOnClickListener {
            currentWord?.let { word ->
                playPronunciation(word.english)
            }
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.word.observe(viewLifecycleOwner) { word ->
            if (word != null) {
                currentWord = word
                binding.textEnglish.text = word.english
                binding.textPronunciation.text = word.pronunciation
                binding.textChinese.text = word.chinese
                binding.textDefinition.text = word.definition

                // Display examples
                val examples = word.example?.split("\n") ?: emptyList()
                val exampleTranslations = word.exampleTranslation?.split("\n") ?: emptyList()

                val formattedExamples = StringBuilder()
                for (i in 0 until min(examples.size, exampleTranslations.size)) {
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

    /**
     * 播放单词发音
     */
    private fun playPronunciation(word: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val audioUrl = fetchAudioUrl(word)
                if (audioUrl != null) {
                    playAudio(audioUrl)
                } else {
                    Toast.makeText(context, R.string.no_pronunciation, Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(context, R.string.no_network, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, R.string.service_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 获取单词发音的音频链接
     */
    private suspend fun fetchAudioUrl(word: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.dictionaryapi.dev/api/v2/entries/en/$word")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                // 读取响应内容
                val inputStream = connection.inputStream
                val response = inputStream.bufferedReader().use { it.readText() }
                inputStream.close()

                // 解析JSON获取audio字段
                val audioUrl = parseAudioFromJson(response)
                return@withContext audioUrl
            }
        } catch (e: Exception) {
            // 异常会被上层捕获并处理
        }
        null
    }

    /**
     * 从JSON响应中解析audio字段
     */
    private fun parseAudioFromJson(jsonResponse: String): String? {
        return try {
            val gson = Gson()
            val jsonArray = gson.fromJson(jsonResponse, Array<DictionaryResponse>::class.java)

            if (jsonArray.isNotEmpty()) {
                val phonetics = jsonArray[0].phonetics
                if (phonetics != null && phonetics.isNotEmpty()) {
                    // 查找包含audio的phonetic对象
                    for (phonetic in phonetics) {
                        if (!phonetic.audio.isNullOrEmpty()) {
                            // 检查audio字段是否是完整URL
                            val audioUrl = phonetic.audio
                            val finalUrl = if (audioUrl!!.startsWith("//")) {
                                "https:$audioUrl"
                            } else if (audioUrl.startsWith("/")) {
                                "https://api.dictionaryapi.dev$audioUrl"
                            } else {
                                audioUrl
                            }
                            return finalUrl
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 播放音频
     */
    private fun playAudio(audioUrl: String) {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener { it.release() }
        mediaPlayer.setOnErrorListener { mp, _, _ ->
            mp.release()
            Toast.makeText(context, R.string.playback_failed, Toast.LENGTH_SHORT).show()
            false
        }

        try {
            mediaPlayer.setDataSource(audioUrl)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener { it.start() }
        } catch (e: Exception) {
            mediaPlayer.release()
            Toast.makeText(context, R.string.playback_failed, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Dictionary API响应数据类
     */
    private data class DictionaryResponse(
        val word: String,
        val phonetics: Array<Phonetic>?
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as DictionaryResponse

            if (word != other.word) return false
            if (phonetics != null) {
                if (other.phonetics == null) return false
                if (!phonetics.contentEquals(other.phonetics)) return false
            } else if (other.phonetics != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = word.hashCode()
            result = 31 * result + (phonetics?.contentHashCode() ?: 0)
            return result
        }
    }

    private data class Phonetic(
        val text: String?,
        val audio: String?
    )
}