# API 文档

## 概述

本文档描述了简约背诵应用的内部API接口，包括数据访问层(DAO)、数据仓库(Repository)和业务逻辑层(ViewModel)的主要方法。

## 数据访问层 (DAO)

### WordDao - 单词数据访问

#### 查询方法

```kotlin
// 获取所有单词
fun getAllWords(): LiveData<List<Word>>

// 根据ID获取单词
suspend fun getWordById(id: Long): Word?

// 随机获取指定数量的单词
suspend fun getRandomWords(count: Int): List<Word>

// 根据词库获取单词
suspend fun getWordsByWordBank(wordBank: String): List<Word>

// 根据词库随机获取指定数量的单词
suspend fun getRandomWordsByWordBank(wordBank: String, count: Int): List<Word>

// 获取单词总数
suspend fun getWordCount(): Int

// 根据词库获取单词总数
suspend fun getWordCountByWordBank(wordBank: String): Int
```

#### 修改方法

```kotlin
// 插入单个单词
suspend fun insertWord(word: Word): Long

// 批量插入单词
suspend fun insertWords(words: List<Word>)

// 更新单词
suspend fun updateWord(word: Word)

// 删除单词
suspend fun deleteWord(word: Word)

// 删除指定词库的所有单词
suspend fun deleteWordsByWordBank(wordBank: String): Int
```

### StudyRecordDao - 学习记录数据访问

#### 查询方法

```kotlin
// 根据日期获取学习记录
fun getStudyRecordsByDate(date: Date): LiveData<List<StudyRecord>>

// 根据单词ID获取学习记录
suspend fun getStudyRecordsByWordId(wordId: Long): List<StudyRecord>

// 获取所有学习日期
fun getAllStudyDates(): LiveData<List<Date>>

// 获取日期范围内的学习记录
suspend fun getStudyRecordsBetweenDates(startDate: Date, endDate: Date): List<StudyRecord>

// 获取指定日期的完成单词数
suspend fun getCompletedWordsCountByDate(date: Date): Int

// 获取指定日期已完成的单词
suspend fun getCompletedWordsForDate(date: Date): List<Word>

// 根据单词ID和学习日期获取学习记录
suspend fun getStudyRecordByWordIdAndDate(wordId: Long, date: Date): StudyRecord?
```

#### 修改方法

```kotlin
// 插入学习记录
suspend fun insertStudyRecord(studyRecord: StudyRecord): Long

// 更新学习记录
suspend fun updateStudyRecord(studyRecord: StudyRecord)

// 删除学习记录
suspend fun deleteStudyRecord(studyRecord: StudyRecord)
```

### FavoriteWordDao - 收藏单词数据访问

#### 查询方法

```kotlin
// 获取所有收藏单词
fun getFavoriteWords(): LiveData<List<Word>>

// 根据单词ID获取收藏记录
suspend fun getFavoriteByWordId(wordId: Long): FavoriteWord?

// 检查单词是否已收藏
suspend fun isFavorite(wordId: Long): Int
```

#### 修改方法

```kotlin
// 添加收藏
suspend fun insertFavorite(favoriteWord: FavoriteWord): Long

// 根据单词ID删除收藏
suspend fun deleteFavoriteByWordId(wordId: Long)
```

### DailyGoalDao - 每日目标数据访问

#### 查询方法

```kotlin
// 根据日期获取每日目标
suspend fun getDailyGoalByDate(date: Date): DailyGoal?

// 获取最近的每日目标
fun getRecentDailyGoals(): LiveData<List<DailyGoal>>
```

#### 修改方法

```kotlin
// 插入每日目标
suspend fun insertDailyGoal(dailyGoal: DailyGoal): Long

// 更新每日目标
suspend fun updateDailyGoal(dailyGoal: DailyGoal)

// 删除每日目标
suspend fun deleteDailyGoal(dailyGoal: DailyGoal)
```

## 数据仓库层 (Repository)

### WordRepository - 统一数据访问接口

WordRepository 封装了所有DAO的操作，提供统一的数据访问接口：

#### 单词操作

```kotlin
// 获取所有单词
fun getAllWords(): LiveData<List<Word>>

// 根据ID获取单词
suspend fun getWordById(id: Long): Word?

// 随机获取单词
suspend fun getRandomWords(count: Int): List<Word>

// 根据词库获取单词
suspend fun getWordsByWordBank(wordBank: String): List<Word>

// 根据词库随机获取单词
suspend fun getRandomWordsByWordBank(wordBank: String, count: Int): List<Word>

// 批量插入单词
suspend fun insertWords(words: List<Word>)

// 删除指定词库的所有单词
suspend fun deleteWordsByWordBank(wordBank: String): Int
```

#### 学习记录操作

```kotlin
// 根据日期获取学习记录
fun getStudyRecordsByDate(date: Date): LiveData<List<StudyRecord>>

// 插入学习记录
suspend fun insertStudyRecord(studyRecord: StudyRecord)

// 更新学习记录
suspend fun updateStudyRecord(studyRecord: StudyRecord)

// 获取所有学习日期
fun getAllStudyDates(): LiveData<List<Date>>

// 获取指定日期已完成的单词
suspend fun getCompletedWordsForDate(date: Date): List<Word>

// 根据单词ID和学习日期获取学习记录
suspend fun getStudyRecordByWordIdAndDate(wordId: Long, date: Date): StudyRecord?
```

#### 收藏操作

```kotlin
// 获取收藏单词
fun getFavoriteWords(): LiveData<List<Word>>

// 添加到收藏
suspend fun insertFavoriteWord(favoriteWord: FavoriteWord): Long

// 从收藏中移除
suspend fun deleteFavoriteWordByWordId(wordId: Long)

// 检查是否已收藏
suspend fun isWordFavorite(wordId: Long): Boolean
```

#### 每日目标操作

```kotlin
// 根据日期获取每日目标
suspend fun getDailyGoalByDate(date: Date): DailyGoal?

// 插入每日目标
suspend fun insertDailyGoal(dailyGoal: DailyGoal)

// 更新每日目标
suspend fun updateDailyGoal(dailyGoal: DailyGoal)

// 获取最近的每日目标
fun getRecentDailyGoals(): LiveData<List<DailyGoal>>

// 创建或更新每日目标
suspend fun createOrUpdateDailyGoal(targetCount: Int)
```

## 业务逻辑层 (ViewModel)

### HomeViewModel - 主页业务逻辑

#### 数据属性

```kotlin
// 每日学习进度
val dailyProgress: LiveData<DailyProgress>

// 总体学习进度
val overallProgress: LiveData<Pair<Int, Int>>

// 加载状态
val isLoading: LiveData<Boolean>
```

#### 方法

```kotlin
// 加载今日进度
fun loadTodayProgress(context: Context)

// 加载总体进度
fun loadOverallProgress(context: Context)

// 获取今日单词ID列表
fun getTodayWordIds(callback: (LongArray?) -> Unit)
```

### StudyViewModel - 学习页面业务逻辑

#### 数据属性

```kotlin
// 今日学习单词
val todayWords: LiveData<List<Word>>

// 当前学习位置
val currentPosition: LiveData<Int>

// 收藏单词列表
val favoriteWords: LiveData<List<Long>>

// 翻转单词列表
val flippedWords: LiveData<List<Long>>

// 加载状态
val isLoading: LiveData<Boolean>
```

#### 方法

```kotlin
// 加载今日单词
fun loadTodayWords(context: Context)

// 更新当前位置
fun updateCurrentPosition(position: Int)

// 切换收藏状态
fun toggleFavorite(wordId: Long, isFavorite: Boolean)

// 添加翻转单词
fun addFlippedWord(wordId: Long)

// 移除翻转单词
fun removeFlippedWord(wordId: Long)

// 获取指定日期已完成的单词ID列表
suspend fun getCompletedWordIdsForDate(date: Date): List<Long>

// 获取已背诵的单词
suspend fun getMemorizedWordsByDate(date: Date): List<Word>
```

### TestViewModel - 测试页面业务逻辑

#### 数据属性

```kotlin
// 当前题目
val currentQuestion: LiveData<TestQuestion?>

// 当前题目索引
val currentQuestionIndex: LiveData<Int>

// 用户选择的答案
val selectedAnswer: LiveData<Int>

// 正确答案显示
val showCorrectAnswer: LiveData<Int>

// 测试结果
val testResult: LiveData<TestResult?>

// 加载状态
val isLoading: LiveData<Boolean>
```

#### 方法

```kotlin
// 开始测试
fun startTest(wordIds: LongArray)

// 选择答案
fun selectAnswer(answerIndex: Int)

// 下一题
fun nextQuestion()

// 获取总题数
fun getTotalQuestions(): Int
```

## 数据模型

### Word - 单词实体

```kotlin
data class Word(
    val id: Long = 0,                    // 单词ID
    val english: String,                 // 英文单词
    val chinese: String,                 // 中文释义
    val pronunciation: String? = null,   // 音标
    val definition: String? = null,      // 英文释义
    val example: String? = null,         // 例句
    val exampleTranslation: String? = null, // 例句翻译
    val category: String = "general",    // 分类
    val synos: String? = null,           // 同义词
    val phrases: String? = null,         // 短语
    val relWord: String? = null,         // 同根词
    val wordBank: String = "vocabulary"  // 词库来源类别
)
```

### StudyRecord - 学习记录实体

```kotlin
data class StudyRecord(
    val wordId: Long,                    // 单词ID
    val studyDate: Date,                 // 学习日期
    val isCompleted: Boolean = false,    // 是否完成
    val correctCount: Int = 0,           // 正确次数
    val wrongCount: Int = 0,             // 错误次数
    val lastReviewDate: Date? = null     // 最后复习日期
)
```

### FavoriteWord - 收藏单词实体

```kotlin
data class FavoriteWord(
    val id: Long = 0,                    // 收藏ID
    val wordId: Long,                    // 单词ID
    val addedDate: Date = Date()         // 收藏日期
)
```

### DailyGoal - 每日目标实体

```kotlin
data class DailyGoal(
    val date: Date,                      // 目标日期
    val targetWordCount: Int = 10,       // 目标单词数
    val completedWordCount: Int = 0,     // 已完成单词数
    val isCompleted: Boolean = false,    // 是否完成
    val dailyWordIds: String = "",       // 当日学习的单词ID列表
    val flippedWordIds: String = ""      // 当日已翻转的单词ID列表
)
```

## 使用示例

### 1. 获取今日学习单词

```kotlin
class StudyViewModel(private val repository: WordRepository) : ViewModel() {
    
    fun loadTodayWords(context: Context) {
        viewModelScope.launch {
            // 获取设置参数
            val sharedPreferences = context.getSharedPreferences("vocabulary_settings", Context.MODE_PRIVATE)
            val dailyWordCount = sharedPreferences.getInt("daily_word_count", 10)
            val currentWordBank = sharedPreferences.getString("current_word_bank", "六级核心") ?: "六级核心"
            
            // 获取随机单词
            val words = repository.getRandomWordsByWordBank(currentWordBank, dailyWordCount)
            _todayWords.value = words
            
            // 创建学习记录
            val today = getTodayDate()
            words.forEach { word ->
                val studyRecord = StudyRecord(
                    wordId = word.id,
                    studyDate = today,
                    isCompleted = false
                )
                repository.insertStudyRecord(studyRecord)
            }
        }
    }
}
```

### 2. 管理单词收藏

```kotlin
// 添加到收藏
viewModelScope.launch {
    val favoriteWord = FavoriteWord(wordId = wordId)
    repository.insertFavoriteWord(favoriteWord)
}

// 检查收藏状态
viewModelScope.launch {
    val isFavorite = repository.isWordFavorite(wordId)
    // 更新UI状态
}

// 获取所有收藏单词
repository.getFavoriteWords().observe(this) { favoriteWords ->
    // 更新收藏列表UI
}
```

### 3. 生成测试题目

```kotlin
private suspend fun generateQuestions(wordIds: LongArray): List<TestQuestion> {
    val words = repository.getWordsByIds(wordIds.toList())
    val allWords = repository.getRandomWords(20) // 获取干扰项
    
    return words.map { targetWord ->
        val isEnglishToChinese = (0..1).random() == 0
        
        if (isEnglishToChinese) {
            // 生成英译中题目
            val wrongOptions = allWords
                .filter { it.id != targetWord.id }
                .map { it.chinese }
                .shuffled()
                .take(3)
            
            val options = (wrongOptions + targetWord.chinese).shuffled()
            val correctIndex = options.indexOf(targetWord.chinese)
            
            TestQuestion(
                question = "${targetWord.english}",
                options = options,
                correctAnswerIndex = correctIndex,
                word = targetWord
            )
        } else {
            // 生成中译英题目
            // ... 类似逻辑
        }
    }
}
```

## 错误处理

### 数据库操作异常

```kotlin
try {
    val words = repository.getRandomWords(count)
    _todayWords.value = words
} catch (e: Exception) {
    // 处理数据库异常
    _error.value = "加载单词失败：${e.message}"
}
```

### 空数据处理

```kotlin
val words = repository.getRandomWords(count)
if (words.isEmpty()) {
    // 处理空数据情况
    _error.value = "没有可用的单词数据，请先初始化词库"
    return
}
```

## 性能优化建议

1. **使用LiveData**: 利用LiveData的自动生命周期管理和数据观察机制
2. **批量操作**: 使用批量插入而不是单个插入操作
3. **索引优化**: 在经常查询的字段上添加数据库索引
4. **分页加载**: 对于大量数据使用分页加载机制
5. **缓存策略**: 合理使用内存缓存减少数据库访问

## 扩展指南

### 添加新的数据实体

1. 创建实体类并添加Room注解
2. 在数据库类中添加实体引用
3. 创建对应的DAO接口
4. 在Repository中添加相关方法
5. 更新数据库版本号并提供迁移策略

### 添加新的业务功能

1. 在对应的ViewModel中添加业务逻辑
2. 创建必要的数据模型类
3. 更新UI层以支持新功能
4. 添加相应的测试用例