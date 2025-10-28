package com.justbyheart.vocabulary.ui.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbyheart.vocabulary.data.entity.StudyRecord
import com.justbyheart.vocabulary.data.entity.Word
import com.justbyheart.vocabulary.data.repository.WordRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

data class TestQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val word: Word,
    val isEnglishToChinese: Boolean
)

data class TestResult(
    val correctAnswers: Int,
    val totalQuestions: Int
)

class TestViewModel(private val repository: WordRepository) : ViewModel() {

    private val _currentQuestion = MutableLiveData<TestQuestion?>()
    val currentQuestion: LiveData<TestQuestion?> = _currentQuestion

    private val _currentQuestionIndex = MutableLiveData<Int>()
    val currentQuestionIndex: LiveData<Int> = _currentQuestionIndex

    private val _selectedAnswer = MutableLiveData<Int>()
    val selectedAnswer: LiveData<Int> = _selectedAnswer

    private val _showCorrectAnswer = MutableLiveData<Int>()
    val showCorrectAnswer: LiveData<Int> = _showCorrectAnswer

    private val _testResult = MutableLiveData<TestResult?>()
    val testResult: LiveData<TestResult?> = _testResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var questions = listOf<TestQuestion>()
    private var currentIndex = 0
    private var correctAnswersCount = 0
    private val wordResults = mutableMapOf<Long, Pair<Boolean?, Boolean?>>() // wordId -> Pair(isE2CCorrect, isC2ECorrect)

    fun startTest(wordIds: LongArray) {
        viewModelScope.launch {
            _isLoading.value = true
            val words = wordIds.asList().mapNotNull { repository.getWordById(it) }
            questions = generateQuestions(words).shuffled()
            if (questions.isNotEmpty()) {
                currentIndex = 0
                correctAnswersCount = 0
                showCurrentQuestion()
            }
            _isLoading.value = false
        }
    }

    private suspend fun generateQuestions(words: List<Word>): List<TestQuestion> {
        val allWords = repository.getRandomWords(20)
        val generatedQuestions = mutableListOf<TestQuestion>()

        words.forEach { targetWord ->
            // English to Chinese
            val wrongOptionsE2C = allWords.filter { it.id != targetWord.id }.map { it.chinese }.shuffled().take(3)
            val optionsE2C = (wrongOptionsE2C + targetWord.chinese).shuffled()
            val correctIndexE2C = optionsE2C.indexOf(targetWord.chinese)
            generatedQuestions.add(
                TestQuestion(
                    question = targetWord.english,
                    options = optionsE2C,
                    correctAnswerIndex = correctIndexE2C,
                    word = targetWord,
                    isEnglishToChinese = true
                )
            )

            // Chinese to English
            val wrongOptionsC2E = allWords.filter { it.id != targetWord.id }.map { it.english }.shuffled().take(3)
            val optionsC2E = (wrongOptionsC2E + targetWord.english).shuffled()
            val correctIndexC2E = optionsC2E.indexOf(targetWord.english)
            generatedQuestions.add(
                TestQuestion(
                    question = targetWord.chinese,
                    options = optionsC2E,
                    correctAnswerIndex = correctIndexC2E,
                    word = targetWord,
                    isEnglishToChinese = false
                )
            )
        }
        return generatedQuestions
    }

    private fun showCurrentQuestion() {
        if (currentIndex < questions.size) {
            _currentQuestion.value = questions[currentIndex]
            _currentQuestionIndex.value = currentIndex
            _selectedAnswer.value = -1
            _showCorrectAnswer.value = -1
        } else {
            viewModelScope.launch {
                updateStudyRecords()
                _testResult.value = TestResult(correctAnswersCount, questions.size)
            }
        }
    }

    private fun getTodayZeroed(): Date {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    fun selectAnswer(answerIndex: Int) {
        _selectedAnswer.value = answerIndex
        val currentQ = _currentQuestion.value ?: return

        val isCorrect = answerIndex == currentQ.correctAnswerIndex
        if (isCorrect) {
            correctAnswersCount++
        }

        val wordId = currentQ.word.id
        val currentResult = wordResults.getOrPut(wordId) { Pair(null, null) }
        wordResults[wordId] = if (currentQ.isEnglishToChinese) {
            currentResult.copy(first = isCorrect)
        } else {
            currentResult.copy(second = isCorrect)
        }

        viewModelScope.launch {
            val today = getTodayZeroed()
            val record = repository.getStudyRecordByWordIdAndDate(wordId, today) ?: StudyRecord(wordId = wordId, studyDate = today)

            val updatedRecord = record.copy(
                correctCount = record.correctCount + if (isCorrect) 1 else 0,
                wrongCount = record.wrongCount + if (isCorrect) 0 else 1
            )
            repository.insertStudyRecord(updatedRecord)
        }

        _showCorrectAnswer.value = currentQ.correctAnswerIndex
    }

    private suspend fun updateStudyRecords() {
        val today = getTodayZeroed()
        wordResults.forEach { (wordId, result) ->
            if (result.first == true && result.second == true) {
                val record = repository.getStudyRecordByWordIdAndDate(wordId, today)
                record?.let { studyRecord ->
                    val updatedRecord = studyRecord.copy(isCompleted = true)
                    repository.updateStudyRecord(updatedRecord)
                }
            }
        }
    }

    fun nextQuestion() {
        currentIndex++
        showCurrentQuestion()
    }

    fun getTotalQuestions(): Int = questions.size
}