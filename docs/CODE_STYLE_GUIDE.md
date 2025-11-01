# 代码规范指南

## 目录

1. [总体原则](#总体原则)
2. [Kotlin编码规范](#kotlin编码规范)
3. [Android特定规范](#android特定规范)
4. [注释规范](#注释规范)
5. [资源命名规范](#资源命名规范)
6. [项目结构规范](#项目结构规范)
7. [Git提交规范](#git提交规范)

## 总体原则

### 代码质量原则

1. **可读性优先**: 代码应该易于理解和维护
2. **一致性**: 整个项目保持统一的编码风格
3. **简洁性**: 避免不必要的复杂性
4. **可测试性**: 代码应该易于测试
5. **性能考虑**: 在保证可读性的前提下优化性能

### 设计原则

1. **单一职责原则**: 每个类和方法只负责一个功能
2. **开闭原则**: 对扩展开放，对修改关闭
3. **依赖倒置**: 依赖抽象而不是具体实现
4. **接口隔离**: 使用小而专一的接口

## Kotlin编码规范

### 命名规范

#### 1. 类和接口命名

```kotlin
// ✅ 正确：使用大驼峰命名法 (PascalCase)
class WordRepository
interface WordDao
data class StudyRecord
enum class DifficultyLevel

// ❌ 错误
class wordRepository
interface wordDao
data class studyRecord
```

#### 2. 函数命名

```kotlin
// ✅ 正确：使用小驼峰命名法 (camelCase)，动词开头
fun loadTodayWords()
fun updateStudyProgress()
fun isWordFavorite()
suspend fun insertWord()

// ❌ 错误
fun LoadTodayWords()
fun update_study_progress()
fun wordisfavorite()
```

#### 3. 变量命名

```kotlin
// ✅ 正确：使用小驼峰命名法，名词性
private val dailyProgress = MutableLiveData<DailyProgress>()
private var currentWordIndex = 0
private lateinit var wordAdapter: WordAdapter

// ❌ 错误
private val DailyProgress = MutableLiveData<DailyProgress>()
private var current_word_index = 0
private lateinit var wa: WordAdapter
```

#### 4. 常量命名

```kotlin
// ✅ 正确：全大写，下划线分隔
companion object {
    private const val DEFAULT_WORD_COUNT = 10
    private const val DATABASE_NAME = "vocabulary_database"
    private const val PREFS_NAME = "vocabulary_settings"
}

// ❌ 错误
companion object {
    private const val defaultWordCount = 10
    private const val databaseName = "vocabulary_database"
}
```

### 代码格式

#### 1. 缩进和空格

```kotlin
// ✅ 正确：使用4个空格缩进
class WordRepository(
    private val wordDao: WordDao,
    private val studyRecordDao: StudyRecordDao
) {
    
    fun getRandomWords(count: Int): List<Word> {
        return wordDao.getRandomWords(count)
    }
}

// ❌ 错误：使用Tab或2个空格
class WordRepository(
  private val wordDao: WordDao,
  private val studyRecordDao: StudyRecordDao
) {
  
  fun getRandomWords(count: Int): List<Word> {
    return wordDao.getRandomWords(count)
  }
}
```

#### 2. 行长度限制

```kotlin
// ✅ 正确：每行不超过120个字符，适当换行
class StudyRecord(
    val id: Long = 0,
    val wordId: Long,
    val studyDate: Date,
    val isCompleted: Boolean = false
)

// 长参数列表换行
fun generateTestQuestion(
    targetWord: Word,
    wrongOptions: List<String>,
    questionType: QuestionType
): TestQuestion {
    // 实现代码
}

// ❌ 错误：单行过长
class StudyRecord(val id: Long = 0, val wordId: Long, val studyDate: Date, val isCompleted: Boolean = false, val correctCount: Int = 0)
```

#### 3. 空行使用

```kotlin
// ✅ 正确：合理使用空行分隔逻辑块
class HomeViewModel(private val repository: WordRepository) : ViewModel() {
    
    private val _dailyProgress = MutableLiveData<DailyProgress>()
    val dailyProgress: LiveData<DailyProgress> = _dailyProgress
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun loadTodayProgress() {
        viewModelScope.launch {
            _isLoading.value = true
            
            val today = getCurrentDate()
            val dailyGoal = repository.getDailyGoalByDate(today)
            
            updateProgressData(dailyGoal)
            
            _isLoading.value = false
        }
    }
    
    private fun updateProgressData(dailyGoal: DailyGoal?) {
        // 实现代码
    }
}
```

### 函数编写规范

#### 1. 函数长度

```kotlin
// ✅ 正确：函数保持简短，单一职责
fun loadTodayWords() {
    viewModelScope.launch {
        setLoadingState(true)
        val words = fetchTodayWords()
        createStudyRecords(words)
        updateWordsList(words)
        setLoadingState(false)
    }
}

private suspend fun fetchTodayWords(): List<Word> {
    val dailyGoal = repository.getDailyGoalByDate(getCurrentDate())
    val targetCount = dailyGoal?.targetWordCount ?: DEFAULT_WORD_COUNT
    return repository.getRandomWords(targetCount)
}

// ❌ 错误：函数过长，职责不清
fun loadTodayWords() {
    viewModelScope.launch {
        _isLoading.value = true
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val dailyGoal = repository.getDailyGoalByDate(today)
        val targetCount = dailyGoal?.targetWordCount ?: 10
        val words = repository.getRandomWords(targetCount)
        words.forEach { word ->
            val studyRecord = StudyRecord(
                wordId = word.id,
                studyDate = today,
                isCompleted = false
            )
            repository.insertStudyRecord(studyRecord)
        }
        _todayWords.value = words
        _isLoading.value = false
    }
}
```

#### 2. 参数处理

```kotlin
// ✅ 正确：使用默认参数，参数验证
fun getRandomWords(
    count: Int = DEFAULT_WORD_COUNT,
    difficulty: Int? = null,
    category: String? = null
): List<Word> {
    require(count > 0) { "Word count must be positive" }
    require(difficulty == null || difficulty in 1..5) { "Difficulty must be between 1 and 5" }
    
    return when {
        difficulty != null -> wordDao.getRandomWordsByDifficulty(difficulty, count)
        category != null -> wordDao.getRandomWordsByCategory(category, count)
        else -> wordDao.getRandomWords(count)
    }
}

// ❌ 错误：没有参数验证，逻辑不清晰
fun getRandomWords(count: Int, difficulty: Int, category: String): List<Word> {
    if (difficulty > 0) {
        return wordDao.getRandomWordsByDifficulty(difficulty, count)
    }
    if (category.isNotEmpty()) {
        return wordDao.getRandomWordsByCategory(category, count)
    }
    return wordDao.getRandomWords(count)
}
```

### 类设计规范

#### 1. 数据类

```kotlin
// ✅ 正确：简洁的数据类定义
data class Word(
    val id: Long = 0,
    val english: String,
    val chinese: String,
    val pronunciation: String? = null,
    val definition: String? = null,
    val example: String? = null,
    val exampleTranslation: String? = null,
    val difficulty: Int = 1,
    val category: String = "general"
) {
    init {
        require(english.isNotBlank()) { "English word cannot be blank" }
        require(chinese.isNotBlank()) { "Chinese translation cannot be blank" }
        require(difficulty in 1..5) { "Difficulty must be between 1 and 5" }
    }
}
```

#### 2. ViewModel类

```kotlin
// ✅ 正确：清晰的ViewModel结构
class StudyViewModel(
    private val repository: WordRepository
) : ViewModel() {
    
    // 私有可变LiveData
    private val _todayWords = MutableLiveData<List<Word>>()
    private val _currentPosition = MutableLiveData<Int>()
    private val _isLoading = MutableLiveData<Boolean>()
    
    // 公开只读LiveData
    val todayWords: LiveData<List<Word>> = _todayWords
    val currentPosition: LiveData<Int> = _currentPosition
    val isLoading: LiveData<Boolean> = _isLoading
    
    // 公开方法
    fun loadTodayWords() {
        // 实现代码
    }
    
    fun updateCurrentPosition(position: Int) {
        _currentPosition.value = position
    }
    
    // 私有辅助方法
    private suspend fun createStudyRecords(words: List<Word>) {
        // 实现代码
    }
}
```

## Android特定规范

### Activity和Fragment

#### 1. 生命周期方法顺序

```kotlin
// ✅ 正确：按生命周期顺序排列方法
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViews()
        setupNavigation()
    }
    
    override fun onStart() {
        super.onStart()
        // onStart逻辑
    }
    
    override fun onResume() {
        super.onResume()
        // onResume逻辑
    }
    
    override fun onPause() {
        super.onPause()
        // onPause逻辑
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 清理资源
    }
    
    private fun initializeViews() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    
    private fun setupNavigation() {
        // 导航设置
    }
}
```

#### 2. ViewBinding使用

```kotlin
// ✅ 正确：正确使用ViewBinding
class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // 防止内存泄漏
    }
    
    private fun setupUI() {
        binding.buttonStartStudy.setOnClickListener {
            // 点击处理
        }
    }
}
```

### 资源使用规范

#### 1. 字符串资源

```kotlin
// ✅ 正确：使用字符串资源
binding.textTitle.text = getString(R.string.title_home)
binding.textProgress.text = getString(R.string.progress_format, completed, total)

// ❌ 错误：硬编码字符串
binding.textTitle.text = "首页"
binding.textProgress.text = "$completed/$total"
```

#### 2. 尺寸和颜色

```kotlin
// ✅ 正确：使用资源文件
binding.cardView.setCardBackgroundColor(
    ContextCompat.getColor(requireContext(), R.color.md_theme_light_surface)
)

val padding = resources.getDimensionPixelSize(R.dimen.default_padding)
binding.container.setPadding(padding, padding, padding, padding)

// ❌ 错误：硬编码数值
binding.cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
binding.container.setPadding(16, 16, 16, 16)
```

## 注释规范

### 类注释

```kotlin
/**
 * 单词数据仓库类
 * 
 * 作为数据层的统一访问接口，封装了所有数据库操作。
 * 遵循Repository模式，为上层提供清晰的数据访问API，
 * 隐藏了具体的数据源实现细节。
 * 
 * 主要功能：
 * - 单词的增删改查操作
 * - 学习记录管理
 * - 收藏功能支持
 * - 每日目标跟踪
 * 
 * @param wordDao 单词数据访问对象
 * @param studyRecordDao 学习记录数据访问对象
 * @param favoriteWordDao 收藏单词数据访问对象
 * @param dailyGoalDao 每日目标数据访问对象
 * 
 * @author 开发团队
 * @since 1.0.0
 */
class WordRepository(
    private val wordDao: WordDao,
    private val studyRecordDao: StudyRecordDao,
    private val favoriteWordDao: FavoriteWordDao,
    private val dailyGoalDao: DailyGoalDao
) {
    // 类实现
}
```

### 方法注释

```kotlin
/**
 * 加载今日学习单词
 * 
 * 根据今日的学习目标数量，随机获取相应数量的单词，
 * 并为每个单词创建学习记录。如果今日没有设置目标，
 * 则使用默认数量（10个单词）。
 * 
 * 执行流程：
 * 1. 获取今日学习目标
 * 2. 确定需要学习的单词数量
 * 3. 随机获取单词列表
 * 4. 为每个单词创建学习记录
 * 5. 更新UI状态
 * 
 * @throws IllegalStateException 当数据库中没有可用单词时抛出
 * 
 * @see getDailyGoalByDate
 * @see createStudyRecord
 */
fun loadTodayWords() {
    viewModelScope.launch {
        try {
            _isLoading.value = true
            
            // 获取今日学习目标
            val today = getCurrentDate()
            val dailyGoal = repository.getDailyGoalByDate(today)
            val targetCount = dailyGoal?.targetWordCount ?: DEFAULT_WORD_COUNT
            
            // 获取随机单词
            val words = repository.getRandomWords(targetCount)
            if (words.isEmpty()) {
                throw IllegalStateException("No words available in database")
            }
            
            // 创建学习记录
            createStudyRecords(words, today)
            
            // 更新UI
            _todayWords.value = words
            
        } catch (e: Exception) {
            _error.value = "加载单词失败: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
}
```

### 行内注释

```kotlin
fun generateTestQuestion(word: Word, allWords: List<Word>): TestQuestion {
    // 随机决定题目类型：0为英译中，1为中译英
    val isEnglishToChinese = (0..1).random() == 0
    
    if (isEnglishToChinese) {
        // 生成英译中题目
        val wrongOptions = allWords
            .filter { it.id != word.id }    // 排除目标单词
            .map { it.chinese }             // 获取中文释义
            .shuffled()                     // 随机排序
            .take(3)                       // 取3个干扰项
        
        // 将正确答案和干扰项混合并随机排序
        val options = (wrongOptions + word.chinese).shuffled()
        val correctIndex = options.indexOf(word.chinese)
        
        return TestQuestion(
            question = "${word.english}",
            options = options,
            correctAnswerIndex = correctIndex,
            word = word
        )
    } else {
        // 生成中译英题目的类似逻辑
        // ...
    }
}
```

## 资源命名规范

### 布局文件命名

```
# Activity布局
activity_main.xml
activity_settings.xml

# Fragment布局
fragment_home.xml
fragment_study.xml
fragment_test.xml

# 列表项布局
item_word_card.xml
item_favorite_word.xml
item_study_record.xml

# 对话框布局
dialog_word_detail.xml
dialog_confirm_delete.xml

# 自定义View布局
view_progress_indicator.xml
view_word_statistics.xml
```

### 字符串资源命名

```xml
<!-- 应用基本信息 -->
<string name="app_name">简约背诵</string>
<string name="app_version">版本 %s</string>

<!-- 页面标题 -->
<string name="title_home">首页</string>
<string name="title_study">学习</string>
<string name="title_test">测试</string>

<!-- 按钮文本 -->
<string name="button_start_study">开始学习</string>
<string name="button_continue_study">继续学习</string>
<string name="button_start_test">开始测试</string>

<!-- 提示信息 -->
<string name="message_loading">加载中...</string>
<string name="message_no_data">暂无数据</string>
<string name="message_network_error">网络连接失败</string>

<!-- 错误信息 -->
<string name="error_word_not_found">未找到单词</string>
<string name="error_database_error">数据库错误</string>

<!-- 格式化字符串 -->
<string name="progress_format">%1$d/%2$d</string>
<string name="score_format">得分：%1$d/%2$d (%3$d%%)</string>
```

### 颜色资源命名

```xml
<!-- Material Design 3 主题色 -->
<color name="md_theme_light_primary">#6750A4</color>
<color name="md_theme_light_onPrimary">#FFFFFF</color>
<color name="md_theme_light_surface">#FFFBFE</color>

<!-- 语义化颜色 -->
<color name="success_green">#4CAF50</color>
<color name="warning_orange">#FF9800</color>
<color name="error_red">#F44336</color>
<color name="info_blue">#2196F3</color>

<!-- 功能性颜色 -->
<color name="text_primary">#1C1B1F</color>
<color name="text_secondary">#49454F</color>
<color name="background_card">#FFFFFF</color>
<color name="divider_color">#E7E0EC</color>
```

### 尺寸资源命名

```xml
<!-- 间距 -->
<dimen name="spacing_xs">4dp</dimen>
<dimen name="spacing_small">8dp</dimen>
<dimen name="spacing_medium">16dp</dimen>
<dimen name="spacing_large">24dp</dimen>
<dimen name="spacing_xl">32dp</dimen>

<!-- 文字大小 -->
<dimen name="text_size_caption">12sp</dimen>
<dimen name="text_size_body">14sp</dimen>
<dimen name="text_size_title">16sp</dimen>
<dimen name="text_size_headline">20sp</dimen>

<!-- 组件尺寸 -->
<dimen name="button_height">48dp</dimen>
<dimen name="card_corner_radius">12dp</dimen>
<dimen name="card_elevation">4dp</dimen>
```

## 项目结构规范

### 包结构

```
com.justbyheart.vocabulary/
├── data/                          # 数据层
│   ├── entity/                    # 数据实体
│   ├── dao/                       # 数据访问对象
│   ├── database/                  # 数据库配置
│   ├── repository/                # 数据仓库
│   └── converter/                 # 数据转换器
├── ui/                           # UI层
│   ├── home/                     # 主页模块
│   ├── study/                    # 学习模块
│   ├── test/                     # 测试模块
│   ├── review/                   # 复习模块
│   ├── favorites/                # 收藏模块
│   ├── library/                  # 词库模块
│   ├── search/                   # 搜索模块
│   ├── settings/                 # 设置模块
│   ├── worddisplay/              # 单词详情模块
│   └── todaywords/               # 今日单词模块
├── utils/                        # 工具类
│   ├── DateUtils.kt              # 日期工具
│   ├── PreferenceUtils.kt        # 偏好设置工具
│   └── WordDataLoader.kt         # 数据加载工具
├── di/                           # 依赖注入（如使用Dagger/Hilt）
└── MainActivity.kt               # 主Activity
```

### 文件组织

```kotlin
// 每个模块的标准文件结构
ui/study/
├── StudyFragment.kt              # Fragment
├── StudyViewModel.kt             # ViewModel
├── StudyViewModelFactory.kt      # ViewModel工厂
├── adapter/                      # 适配器
│   └── WordPagerAdapter.kt
└── model/                        # 模块特定数据模型
    └── StudyState.kt
```

## Git提交规范

### 提交信息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 类型说明

```bash
feat:     新功能
fix:      修复bug
docs:     文档更新
style:    代码格式调整（不影响功能）
refactor: 代码重构
test:     测试相关
chore:    构建过程或辅助工具的变动
perf:     性能优化
```

### 提交示例

```bash
# 新功能
feat(study): 添加单词学习进度跟踪功能

增加了学习进度的实时更新和显示：
- 在学习页面显示当前进度
- 支持进度条可视化显示
- 自动保存学习状态

Closes #123

# 修复bug
fix(test): 修复测试页面选项显示错误

修复了在某些设备上选项文字被截断的问题：
- 调整了选项按钮的高度
- 优化了文字大小适配
- 添加了文字换行支持

# 文档更新
docs(readme): 更新安装说明和使用指南

- 添加了详细的环境配置步骤
- 更新了功能特性说明
- 修正了示例代码中的错误

# 代码重构
refactor(repository): 重构数据访问层架构

将原有的直接DAO调用重构为Repository模式：
- 创建统一的WordRepository接口
- 封装所有数据访问逻辑
- 提高代码的可测试性和可维护性
```

### 分支命名规范

```bash
# 功能分支
feature/add-word-pronunciation    # 添加单词发音功能
feature/improve-test-ui          # 改进测试界面

# 修复分支
fix/crash-on-empty-database      # 修复空数据库崩溃问题
fix/memory-leak-in-adapter       # 修复适配器内存泄漏

# 发布分支
release/v1.1.0                   # 版本1.1.0发布

# 热修复分支
hotfix/critical-data-loss        # 紧急修复数据丢失问题
```

---

**遵循这些规范将帮助我们维护高质量、可维护的代码库。** 📝