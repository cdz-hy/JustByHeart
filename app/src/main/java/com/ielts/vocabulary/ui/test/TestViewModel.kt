package com.ielts.vocabulary.ui.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ielts.vocabulary.data.entity.Word
import com.ielts.vocabulary.data.repository.WordRepository
import kotlinx.coroutines.launch

/**
 * 测试题目数据类
 * 
 * @property question 题目文本
 * @property options 选项列表（4个选项）
 * @property correctAnswerIndex 正确答案的索引
 * @property word 对应的单词对象
 */
data class TestQuestion(
    val question: String,           // 题目问题
    val options: List<String>,      // 选项列表
    val correctAnswerIndex: Int,    // 正确答案索引
    val word: Word                  // 对应单词
)

/**
 * 测试结果数据类
 * 
 * @property correctAnswers 正确答案数量
 * @property totalQuestions 总题目数量
 */
data class TestResult(
    val correctAnswers: Int,        // 正确答案数
    val totalQuestions: Int         // 总题目数
)

/**
 * 测试页面ViewModel
 * 
 * 负责管理单词测试的业务逻辑，包括：
 * - 生成测试题目（中英互译）
 * - 管理答题流程
 * - 计算测试结果
 * - 处理用户答题交互
 * 
 * @param repository 数据仓库，用于获取单词数据
 */
class TestViewModel(private val repository: WordRepository) : ViewModel() {
    
    // 当前题目的私有可变LiveData
    private val _currentQuestion = MutableLiveData<TestQuestion?>()
    // 对外暴露的只读LiveData
    val currentQuestion: LiveData<TestQuestion?> = _currentQuestion
    
    // 当前题目索引的私有可变LiveData
    private val _currentQuestionIndex = MutableLiveData<Int>()
    // 对外暴露的只读LiveData
    val currentQuestionIndex: LiveData<Int> = _currentQuestionIndex
    
    // 用户选择答案的私有可变LiveData
    private val _selectedAnswer = MutableLiveData<Int>()
    // 对外暴露的只读LiveData
    val selectedAnswer: LiveData<Int> = _selectedAnswer
    
    // 显示正确答案的私有可变LiveData
    private val _showCorrectAnswer = MutableLiveData<Int>()
    // 对外暴露的只读LiveData
    val showCorrectAnswer: LiveData<Int> = _showCorrectAnswer
    
    // 测试结果的私有可变LiveData
    private val _testResult = MutableLiveData<TestResult?>()
    // 对外暴露的只读LiveData
    val testResult: LiveData<TestResult?> = _testResult
    
    // 加载状态的私有可变LiveData
    private val _isLoading = MutableLiveData<Boolean>()
    // 对外暴露的只读LiveData
    val isLoading: LiveData<Boolean> = _isLoading
    
    // 测试题目列表
    private var questions = listOf<TestQuestion>()
    // 当前题目索引
    private var currentIndex = 0
    // 正确答案计数
    private var correctAnswersCount = 0
    
    /**
     * 开始测试
     * 
     * 随机获取单词并生成测试题目，
     * 默认生成5道题目进行测试。
     */
    fun startTest() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // 获取5个随机单词用于生成测试题
            val words = repository.getRandomWords(5)
            questions = generateQuestions(words)
            
            if (questions.isNotEmpty()) {
                currentIndex = 0
                correctAnswersCount = 0
                showCurrentQuestion()
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 生成测试题目
     * 
     * 为每个单词随机生成中译英或英译中的选择题，
     * 每道题包含1个正确答案和3个干扰项。
     * 
     * @param words 用于生成题目的单词列表
     * @return 生成的题目列表
     */
    private suspend fun generateQuestions(words: List<Word>): List<TestQuestion> {
        // 获取更多单词作为干扰项
        val allWords = repository.getRandomWords(20)
        
        return words.map { targetWord ->
            // 随机决定是英译中还是中译英
            val isEnglishToChinese = (0..1).random() == 0
            
            if (isEnglishToChinese) {
                // 英译中题目
                val wrongOptions = allWords
                    .filter { it.id != targetWord.id }  // 排除目标单词
                    .map { it.chinese }                 // 获取中文释义
                    .shuffled()                         // 随机排序
                    .take(3)                           // 取3个作为干扰项
                
                // 将正确答案和干扰项混合并随机排序
                val options = (wrongOptions + targetWord.chinese).shuffled()
                val correctIndex = options.indexOf(targetWord.chinese)
                
                TestQuestion(
                    question = "${targetWord.english}",
                    options = options,
                    correctAnswerIndex = correctIndex,
                    word = targetWord
                )
            } else {
                // 中译英题目
                val wrongOptions = allWords
                    .filter { it.id != targetWord.id }  // 排除目标单词
                    .map { it.english }                 // 获取英文单词
                    .shuffled()                         // 随机排序
                    .take(3)                           // 取3个作为干扰项
                
                // 将正确答案和干扰项混合并随机排序
                val options = (wrongOptions + targetWord.english).shuffled()
                val correctIndex = options.indexOf(targetWord.english)
                
                TestQuestion(
                    question = "${targetWord.chinese}",
                    options = options,
                    correctAnswerIndex = correctIndex,
                    word = targetWord
                )
            }
        }
    }
    
    /**
     * 显示当前题目
     * 
     * 如果还有题目未完成，显示当前题目；
     * 否则显示测试结果。
     */
    private fun showCurrentQuestion() {
        if (currentIndex < questions.size) {
            _currentQuestion.value = questions[currentIndex]
            _currentQuestionIndex.value = currentIndex
            _selectedAnswer.value = -1          // 重置选择状态
            _showCorrectAnswer.value = -1       // 重置正确答案显示状态
        } else {
            // 测试结束，显示结果
            _testResult.value = TestResult(correctAnswersCount, questions.size)
        }
    }
    
    /**
     * 选择答案
     * 
     * 用户选择答案后，显示正确答案并统计得分。
     * 
     * @param answerIndex 用户选择的答案索引
     */
    fun selectAnswer(answerIndex: Int) {
        _selectedAnswer.value = answerIndex
        
        val currentQ = _currentQuestion.value
        if (currentQ != null) {
            // 显示正确答案
            _showCorrectAnswer.value = currentQ.correctAnswerIndex
            
            // 如果答案正确，增加正确答案计数
            if (answerIndex == currentQ.correctAnswerIndex) {
                correctAnswersCount++
            }
        }
    }
    
    /**
     * 进入下一题
     * 
     * 增加题目索引并显示下一题，
     * 如果是最后一题则显示测试结果。
     */
    fun nextQuestion() {
        currentIndex++
        showCurrentQuestion()
    }
    
    /**
     * 获取总题目数量
     * 
     * @return 测试题目总数
     */
    fun getTotalQuestions(): Int = questions.size
}