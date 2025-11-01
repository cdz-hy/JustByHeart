# 简约背诵 (JustByHeart) - 架构说明文档

## 目录
- [项目概述](#项目概述)
- [技术架构](#技术架构)
- [项目结构详解](#项目结构详解)
- [数据层架构](#数据层架构)
- [UI层架构](#ui层架构)
- [工具类架构](#工具类架构)
- [设计模式分析](#设计模式分析)
- [依赖关系图](#依赖关系图)
- [优化建议](#优化建议)

---

## 项目概述

### 应用类型
Android 英文单词背诵辅助应用，采用现代化 MVVM 架构模式

### 核心功能模块
- **学习模块**: 每日单词学习，支持卡片式浏览
- **测试模块**: 中英互译选择题测试
- **收藏模块**: 重点单词收藏和管理
- **复习模块**: 历史学习记录查看
- **词库模块**: 查看所有掌握和未掌握单词
- **搜索模块**: 单词搜索功能
- **设置模块**: 学习目标设置和数据管理

### 技术栈
- **开发语言**: Kotlin
- **架构模式**: MVVM + Repository Pattern
- **数据库**: Room Database
- **UI框架**: Material Design 3 + ViewBinding
- **导航**: Navigation Component
- **异步处理**: Kotlin Coroutines
- **依赖注入**: Manual DI

---

## 技术架构

### 架构层次
```
┌─────────────────────────────────────────┐
│                UI Layer                 │
│  (Fragments + ViewModels + Adapters)    │
├─────────────────────────────────────────┤
│              Domain Layer               │
│         (Repository Pattern)            │
├─────────────────────────────────────────┤
│               Data Layer                │
│    (Room Database + DAOs + Entities)    │
└─────────────────────────────────────────┘
```

### 核心设计原则
1. **单一职责原则**: 每个类只负责一个功能
2. **依赖倒置原则**: 高层模块不依赖低层模块
3. **开闭原则**: 对扩展开放，对修改关闭
4. **关注点分离**: UI、业务逻辑、数据访问分离

---

## 项目结构详解

### 根目录结构
```
JustByHeart/
├── app/                        # 主应用模块
├── build/                      # 构建输出目录
├── docs/                       # 项目文档
├── gradle/                     # Gradle Wrapper
├── build.gradle.kts           # 项目级构建配置
├── gradle.properties          # Gradle 属性配置
├── gradlew.bat               # Windows Gradle Wrapper
├── README.md                 # 项目说明文档
└── settings.gradle.kts       # 项目设置配置
```

### 应用模块结构 (app/)
```
app/
├── build/                     # 模块构建输出
├── src/main/
│   ├── assets/               # 静态资源文件
│   ├── java/com/justbyheart/vocabulary/
│   │   ├── data/             # 数据层
│   │   ├── ui/               # UI层
│   │   ├── utils/            # 工具类
│   │   ├── MainActivity.kt   # 主活动
│   │   └── VocabularyApplication.kt  # 应用程序类
│   ├── res/                  # Android 资源文件
│   └── AndroidManifest.xml   # 应用清单文件
└── build.gradle.kts          # 模块构建配置
```

---

## 数据层架构

### 数据层组织结构
```
data/
├── entity/                    # 数据实体类
│   ├── Word.kt               # 单词实体
│   ├── StudyRecord.kt        # 学习记录实体
│   ├── FavoriteWord.kt       # 收藏单词实体
│   └── DailyGoal.kt          # 每日目标实体
├── dao/                      # 数据访问对象
│   ├── WordDao.kt            # 单词数据访问
│   ├── StudyRecordDao.kt     # 学习记录数据访问
│   ├── FavoriteWordDao.kt    # 收藏数据访问
│   └── DailyGoalDao.kt       # 目标数据访问
├── database/                 # 数据库配置
│   └── VocabularyDatabase.kt # Room数据库配置
├── repository/               # 数据仓库
│   └── WordRepository.kt     # 统一数据访问接口
└── converter/                # 数据转换器
    └── DateConverter.kt      # 日期转换器
```

### 实体类详解

#### 1. Word.kt (单词实体)
**位置**: `data/entity/Word.kt`
```kotlin
@Entity(tableName = "words")
@Parcelize
data class Word(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val english: String,                    // 英文单词
    val chinese: String,                    // 中文释义
    val pronunciation: String?,             // 音标发音
    val definition: String?,                // 英文释义
    val example: String?,                   // 例句
    val exampleTranslation: String?,        // 例句翻译
    val category: String = "general",       // 单词分类
    val synos: String?,                     // 同义词
    val phrases: String?,                   // 短语
    val relWord: String?,                   // 同根词
    val wordBank: String = "vocabulary"     // 词库来源类别
) : Parcelable
```
**功能说明**:
- 核心词汇数据模型，包含完整的单词信息
- 实现 Parcelable 接口，支持组件间数据传递
- 使用 Room 注解定义数据库表结构
- 支持可选字段，提供灵活的数据存储

#### 2. StudyRecord.kt (学习记录实体)
**位置**: `data/entity/StudyRecord.kt`
```kotlin
@Entity(
    tableName = "study_records",
    primaryKeys = ["wordId", "studyDate"],
    foreignKeys = [
        ForeignKey(
            entity = Word::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StudyRecord(
    val wordId: Long,                    // 关联的单词ID
    val studyDate: Date,                 // 学习日期
    val isCompleted: Boolean = false,    // 是否完成学习
    val correctCount: Int = 0,           // 正确答题次数
    val wrongCount: Int = 0,             // 错误答题次数
    val lastReviewDate: Date? = null     // 最后复习日期
)
```
**功能说明**:
- 跟踪用户对每个单词的学习进度
- 外键关联到 Word 表，支持级联删除
- 记录学习统计数据，支持智能复习算法
- 按日期组织学习记录，便于进度查询

#### 3. FavoriteWord.kt (收藏单词实体)
**位置**: `data/entity/FavoriteWord.kt`
```kotlin
@Entity(
    tableName = "favorite_words",
    foreignKeys = [
        ForeignKey(
            entity = Word::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FavoriteWord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wordId: Long,                    // 被收藏的单词ID
    val addedDate: Date = Date()         // 收藏日期，默认为当前时间
)
```
**功能说明**:
- 管理用户收藏的重点单词
- 简单的多对多关系实现
- 记录收藏时间，支持按时间排序

#### 4. DailyGoal.kt (每日目标实体)
**位置**: `data/entity/DailyGoal.kt`
```kotlin
@Entity(tableName = "daily_goals")
data class DailyGoal(
    @PrimaryKey
    val date: Date,                      // 目标日期
    val targetWordCount: Int = 10,       // 目标单词数量
    val completedWordCount: Int = 0,     // 已完成单词数量
    val isCompleted: Boolean = false,    // 是否完成目标
    val dailyWordIds: String = "",       // 当日学习的单词ID列表
    val flippedWordIds: String = ""      // 当日已翻转的单词ID列表
)
```
**功能说明**:
- 管理用户每日学习目标
- 跟踪学习进度和完成情况
- 支持个性化学习计划

### DAO 层详解

#### 1. WordDao.kt (单词数据访问)
**位置**: `data/dao/WordDao.kt`
**核心功能**:
```kotlin
@Dao
interface WordDao {
    @Query("SELECT * FROM words")
    fun getAllWords(): LiveData<List<Word>>
    
    @Query("SELECT * FROM words WHERE wordBank = :wordBank ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomWordsByWordBank(wordBank: String, count: Int): List<Word>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<Word>)
}
```
**设计特点**:
- 使用 LiveData 提供响应式数据更新
- 支持按词库随机单词选择
- 按难度筛选，支持个性化学习
- 批量插入优化，提高数据加载效率

#### 2. StudyRecordDao.kt (学习记录数据访问)
**位置**: `data/dao/StudyRecordDao.kt`
**核心功能**:
```kotlin
@Dao
interface StudyRecordDao {
    @Query("SELECT * FROM study_records WHERE studyDate = :date")
    fun getStudyRecordsByDate(date: Date): LiveData<List<StudyRecord>>

    @Query("SELECT COUNT(*) FROM study_records WHERE studyDate = :date AND isCompleted = 1")
    suspend fun getCompletedWordsCountByDate(date: Date): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyRecord(studyRecord: StudyRecord)
}
```
**设计特点**:
- 按日期查询学习记录，支持进度统计
- 按单词查询历史记录，支持复习决策
- 支持记录的增删改查操作

#### 3. FavoriteWordDao.kt (收藏数据访问)
**位置**: `data/dao/FavoriteWordDao.kt`
**核心功能**:
```kotlin
@Dao
interface FavoriteWordDao {
    @Query("""
        SELECT w.* FROM words w 
        INNER JOIN favorite_words f ON w.id = f.wordId 
        ORDER BY f.addedDate DESC
    """)
    fun getFavoriteWords(): LiveData<List<Word>>
    
    @Query("SELECT COUNT(*) FROM favorite_words WHERE wordId = :wordId")
    suspend fun isFavorite(wordId: Long): Int
    
    @Insert
    suspend fun insertFavorite(favoriteWord: FavoriteWord)
    
    @Query("DELETE FROM favorite_words WHERE wordId = :wordId")
    suspend fun deleteFavoriteByWordId(wordId: Long)
}
```
**设计特点**:
- 使用 JOIN 查询获取完整的收藏单词信息
- 提供收藏状态检查功能
- 支持收藏的添加和移除操作

#### 4. DailyGoalDao.kt (目标数据访问)
**位置**: `data/dao/DailyGoalDao.kt`
**核心功能**:
```kotlin
@Dao
interface DailyGoalDao {
    @Query("SELECT * FROM daily_goals WHERE date = :date")
    suspend fun getDailyGoalByDate(date: Date): DailyGoal?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyGoal(dailyGoal: DailyGoal)
    
    @Update
    suspend fun updateDailyGoal(dailyGoal: DailyGoal)
}
```
**设计特点**:
- 按日期精确查询每日目标
- 获取最近目标，支持趋势分析
- 支持目标的创建和更新

### 数据库配置

#### VocabularyDatabase.kt (Room数据库)
**位置**: `data/database/VocabularyDatabase.kt`
```kotlin
@Database(
    entities = [Word::class, StudyRecord::class, FavoriteWord::class, DailyGoal::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class VocabularyDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun studyRecordDao(): StudyRecordDao
    abstract fun favoriteWordDao(): FavoriteWordDao
    abstract fun dailyGoalDao(): DailyGoalDao
    
    companion object {
        @Volatile
        private var INSTANCE: VocabularyDatabase? = null
        
        fun getDatabase(context: Context): VocabularyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VocabularyDatabase::class.java,
                    "vocabulary_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```
**设计特点**:
- 单例模式确保数据库唯一实例
- 线程安全的懒加载初始化
- 统一的 DAO 访问接口
- 类型转换器支持复杂数据类型

### 仓库层

#### WordRepository.kt (数据仓库)
**位置**: `data/repository/WordRepository.kt`
```kotlin
class WordRepository(
    private val wordDao: WordDao,
    private val studyRecordDao: StudyRecordDao,
    private val favoriteWordDao: FavoriteWordDao,
    private val dailyGoalDao: DailyGoalDao
) {
    suspend fun getRandomWordsByWordBank(wordBank: String, count: Int) = 
        wordDao.getRandomWordsByWordBank(wordBank, count)
    
    suspend fun insertWords(words: List<Word>) = wordDao.insertWords(words)
    
    suspend fun insertStudyRecord(record: StudyRecord) = studyRecordDao.insertStudyRecord(record)
    
    fun getFavoriteWords() = favoriteWordDao.getFavoriteWords()
    
    suspend fun toggleFavorite(wordId: Long) {
        if (favoriteWordDao.isFavorite(wordId) > 0) {
            favoriteWordDao.deleteFavoriteByWordId(wordId)
        } else {
            favoriteWordDao.insertFavorite(FavoriteWord(wordId = wordId))
        }
    }
    
    suspend fun getDailyGoalByDate(date: Date): DailyGoal? {
        return dailyGoalDao.getDailyGoalByDate(date)
    }
    
    suspend fun createOrUpdateDailyGoal(targetCount: Int) {
        val today = getTodayZeroed()
        val existingGoal = dailyGoalDao.getDailyGoalByDate(today)
        
        if (existingGoal == null) {
            dailyGoalDao.insertDailyGoal(
                DailyGoal(
                    date = today,
                    targetWordCount = targetCount
                )
            )
        } else {
            dailyGoalDao.updateDailyGoal(
                existingGoal.copy(targetWordCount = targetCount)
            )
        }
    }

    suspend fun updateDailyGoal(goal: DailyGoal) {
        dailyGoalDao.updateDailyGoal(goal)
    }
}
```
**设计特点**:
- 统一的数据访问接口，隐藏具体实现
- 组合多个 DAO 的功能，提供业务级操作
- 处理复杂的业务逻辑，如收藏切换
- 为上层提供简洁的 API

---

## UI层架构

### UI层组织结构
```
ui/
├── home/                     # 主页模块
│   ├── HomeFragment.kt       # 主页片段
│   ├── HomeViewModel.kt      # 主页视图模型
│   └── HomeViewModelFactory.kt # 视图模型工厂
├── study/                    # 学习模块
│   ├── StudyFragment.kt      # 学习片段
│   ├── StudyViewModel.kt     # 学习视图模型
│   └── WordPagerAdapter.kt   # 单词分页适配器
├── test/                     # 测试模块
│   ├── TestFragment.kt       # 测试片段
│   └── TestViewModel.kt      # 测试视图模型
├── review/                   # 复习模块
│   ├── ReviewFragment.kt     # 复习片段
│   ├── ReviewViewModel.kt    # 复习视图模型
│   └── ReviewWordAdapter.kt  # 复习单词适配器
├── favorites/                # 收藏模块
│   ├── FavoritesFragment.kt  # 收藏片段
│   ├── FavoritesViewModel.kt # 收藏视图模型
│   └── FavoriteWordAdapter.kt # 收藏适配器
├── library/                  # 词库模块
│   ├── LibraryFragment.kt    # 词库片段
│   ├── LibraryViewModel.kt   # 词库视图模型
│   └── LibraryWordAdapter.kt # 词库单词适配器
├── search/                   # 搜索模块
│   ├── SearchFragment.kt     # 搜索片段
│   ├── SearchViewModel.kt    # 搜索视图模型
│   └── WordSearchAdapter.kt  # 搜索单词适配器
├── settings/                 # 设置模块
│   ├── SettingsFragment.kt   # 设置片段
│   └── SettingsViewModel.kt  # 设置视图模型
├── worddisplay/              # 单词详情模块
│   ├── WordDisplayFragment.kt # 单词详情片段
│   └── WordDisplayViewModel.kt # 单词详情视图模型
└── study/                    # 学习模块
    ├── StudyFragment.kt      # 学习片段
    ├── StudyViewModel.kt     # 学习视图模型
    └── WordPagerAdapter.kt   # 单词分页适配器
```

### 主页模块详解

#### 1. HomeFragment.kt (主页片段)
**位置**: `ui/home/HomeFragment.kt`
**核心功能**:
```kotlin
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HomeViewModel
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        viewModel.loadTodayProgress(requireContext())
        viewModel.loadOverallProgress(requireContext())
    }
    
    private fun setupUI() {
        // 设置UI交互
    }
    
    private fun observeViewModel() {
        viewModel.dailyProgress.observe(viewLifecycleOwner) { progress ->
            updateProgressUI(progress)
        }
    }
}
```
**设计特点**:
- ViewBinding 消除 findViewById 调用
- ViewModel 分离业务逻辑
- LiveData 观察者模式实现响应式UI
- 生命周期感知的数据观察

#### 2. HomeViewModel.kt (主页视图模型)
**位置**: `ui/home/HomeViewModel.kt`
```kotlin
class HomeViewModel(private val repository: WordRepository) : ViewModel() {
    
    private val _dailyProgress = MutableLiveData<DailyProgress>()
    val dailyProgress: LiveData<DailyProgress> = _dailyProgress
    
    data class DailyProgress(
        val target: Int,
        val completed: Int
    )
    
    fun loadTodayProgress(context: Context) {
        viewModelScope.launch {
            val sharedPreferences = context.getSharedPreferences("vocabulary_settings", Context.MODE_PRIVATE)
            val currentWordBank = sharedPreferences.getString("current_word_bank", "六级核心") ?: "六级核心"
            
            val today = getTodayZeroed()
            val goal = repository.getDailyGoalByDate(today)
            
            if (goal == null) {
                // 创建默认目标
                repository.createOrUpdateDailyGoal(10)
                _dailyProgress.value = DailyProgress(10, 0)
            } else {
                _dailyProgress.value = DailyProgress(
                    target = goal.targetWordCount,
                    completed = goal.completedWordCount
                )
            }
        }
    }
}
```
**设计特点**:
- 使用 Coroutines 处理异步操作
- 数据类封装UI状态，提高可读性
- 自动创建默认目标，提升用户体验
- 计算进度百分比，支持进度可视化

### 学习模块详解

#### 1. StudyFragment.kt (学习片段)
**位置**: `ui/study/StudyFragment.kt`
**核心功能**:
```kotlin
class StudyFragment : Fragment() {
    private var _binding: FragmentStudyBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: StudyViewModel
    private lateinit var wordAdapter: WordPagerAdapter
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewPager()
        setupUI()
        observeViewModel()
        viewModel.loadTodayWords(requireContext())
    }
    
    private fun setupViewPager() {
        wordAdapter = WordPagerAdapter(
            { word, isFavorite -> viewModel.toggleFavorite(word.id, isFavorite) },
            { wordId, isFlipped -> 
                if (isFlipped) {
                    viewModel.addFlippedWord(wordId)
                } else {
                    viewModel.removeFlippedWord(wordId)
                }
            },
            null
        )
        
        binding.viewPagerWords.adapter = wordAdapter
        binding.viewPagerWords.orientation = ViewPager2.ORIENTATION_HORIZONTAL
    }
    
    private fun setupUI() {
        binding.buttonStartTest.setOnClickListener {
            // 开始测试逻辑
        }
    }
    
    private fun observeViewModel() {
        viewModel.todayWords.observe(viewLifecycleOwner) { words ->
            wordAdapter.submitList(words)
        }
        
        viewModel.currentPosition.observe(viewLifecycleOwner) { position ->
            val totalWords = wordAdapter.itemCount
            if (totalWords > 0) {
                binding.textProgress.text = getString(R.string.study_progress_format, position + 1, totalWords)
            }
        }
    }
}
```
**设计特点**:
- ViewPager2 实现流畅的卡片切换
- 适配器模式分离数据展示逻辑
- 回调函数处理用户交互
- 实时更新学习进度

#### 2. StudyViewModel.kt (学习视图模型)
**位置**: `ui/study/StudyViewModel.kt`
```kotlin
class StudyViewModel(private val repository: WordRepository) : ViewModel() {
    
    private val _todayWords = MutableLiveData<List<Word>>()
    val todayWords: LiveData<List<Word>> = _todayWords
    
    private val _currentPosition = MutableLiveData<Int>()
    val currentPosition: LiveData<Int> = _currentPosition
    
    fun loadTodayWords(context: Context) {
        viewModelScope.launch {
            val sharedPreferences = context.getSharedPreferences("vocabulary_settings", Context.MODE_PRIVATE)
            val dailyWordCount = sharedPreferences.getInt("daily_word_count", 10)
            val currentWordBank = sharedPreferences.getString("current_word_bank", "六级核心") ?: "六级核心"
            
            val words = repository.getRandomWordsByWordBank(currentWordBank, dailyWordCount)
            _todayWords.value = words
        }
    }
    
    fun toggleFavorite(wordId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(wordId)
        }
    }
}
```
**设计特点**:
- 根据每日目标和词库动态加载单词
- 批量创建学习记录，提高效率
- 自动更新学习进度
- 异步操作不阻塞UI线程

#### 3. WordPagerAdapter.kt (单词分页适配器)
**位置**: `ui/study/WordPagerAdapter.kt`
```kotlin
class WordPagerAdapter(
    private val onFavoriteClick: (Word, Boolean) -> Unit,
    private val onFlipped: (Long, Boolean) -> Unit,
    private val repository: WordRepository?
) : ListAdapter<Word, WordPagerAdapter.WordViewHolder>(WordDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = JustbyheartWordCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WordViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class WordViewHolder(
        private val binding: JustbyheartWordCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(word: Word) {
            binding.apply {
                textEnglish.text = word.english
                textChinese.text = word.chinese
                textPronunciation.text = word.pronunciation
                textDefinition.text = word.definition
                textExample.text = word.example
                textExampleTranslation.text = word.exampleTranslation
                
                // 设置收藏按钮点击事件
                buttonFavorite.setOnClickListener {
                    val isFavorite = buttonFavorite.tag as? Boolean ?: false
                    onFavoriteClick(word, !isFavorite)
                }
            }
        }
    }
    
    class WordDiffCallback : DiffUtil.ItemCallback<Word>() {
        override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem == newItem
        }
    }
}
```
**设计特点**:
- ListAdapter 自动处理列表更新动画
- DiffUtil 优化列表性能
- ViewBinding 简化视图访问
- 回调函数处理点击事件

### 测试模块详解

#### 1. TestFragment.kt (测试片段)
**位置**: `ui/test/TestFragment.kt`
**核心功能**:
```kotlin
class TestFragment : Fragment() {
    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: TestViewModel
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()

        if (viewModel.testResult.value != null) {
            showTestResult(viewModel.testResult.value!!)
        } else {
            binding.layoutQuestion.visibility = View.VISIBLE
            binding.layoutResult.visibility = View.GONE
            val args = TestFragmentArgs.fromBundle(requireArguments())
            if (viewModel.currentQuestion.value == null) {
                viewModel.startTest(args.wordIds)
            }
        }
    }
    
    private fun setupUI() {
        binding.buttonOption1.setOnClickListener { viewModel.selectAnswer(0) }
        binding.buttonOption2.setOnClickListener { viewModel.selectAnswer(1) }
        binding.buttonOption3.setOnClickListener { viewModel.selectAnswer(2) }
        binding.buttonOption4.setOnClickListener { viewModel.selectAnswer(3) }
        
        binding.buttonNext.setOnClickListener {
            viewModel.nextQuestion()
        }
    }
    
    private fun observeViewModel() {
        viewModel.currentQuestion.observe(viewLifecycleOwner) { question ->
            binding.layoutResult.visibility = View.GONE
            binding.layoutQuestion.visibility = View.VISIBLE
            question?.let {
                binding.textQuestion.text = it.question
                binding.buttonOption1.text = it.options[0]
                binding.buttonOption2.text = it.options[1]
                binding.buttonOption3.text = it.options[2]
                binding.buttonOption4.text = it.options[3]
                
                resetOptionButtons()
                binding.buttonNext.visibility = View.GONE
            }
        }
    }
}
```
**设计特点**:
- 实时反馈答案正确性
- 颜色编码提升用户体验
- 观察者模式响应状态变化
- 清晰的用户交互逻辑

#### 2. TestViewModel.kt (测试视图模型)
**位置**: `ui/test/TestViewModel.kt`
```kotlin
class TestViewModel(private val repository: WordRepository) : ViewModel() {
    
    data class TestQuestion(
        val word: Word,
        val question: String,
        val options: List<String>,
        val correctAnswer: Int,
        val type: QuestionType
    )
    
    enum class QuestionType {
        ENGLISH_TO_CHINESE,
        CHINESE_TO_ENGLISH
    }
    
    data class TestResult(
        val totalQuestions: Int,
        val correctAnswers: Int,
        val incorrectWords: List<Word>
    )
    
    private val _currentQuestion = MutableLiveData<TestQuestion?>()
    val currentQuestion: LiveData<TestQuestion?> = _currentQuestion
    
    private val _testResult = MutableLiveData<TestResult>()
    val testResult: LiveData<TestResult> = _testResult
    
    private val _selectedAnswer = MutableLiveData<Int>()
    val selectedAnswer: LiveData<Int> = _selectedAnswer
    
    private var questions = mutableListOf<TestQuestion>()
    private var currentQuestionIndex = 0
    private var correctCount = 0
    private var incorrectWords = mutableListOf<Word>()
    
    fun startTest(wordIds: LongArray) {
        viewModelScope.launch {
            questions.clear()
            currentQuestionIndex = 0
            correctCount = 0
            incorrectWords.clear()
            
            val words = repository.getWordsByIds(wordIds.toList())
            
            words.forEach { word ->
                val questionType = if (Random.nextBoolean()) {
                    QuestionType.ENGLISH_TO_CHINESE
                } else {
                    QuestionType.CHINESE_TO_ENGLISH
                }
                
                val question = createQuestion(word, words, questionType)
                questions.add(question)
            }
            
            if (questions.isNotEmpty()) {
                _currentQuestion.value = questions[0]
            }
        }
    }
}
```
**设计特点**:
- 动态生成多种题型
- 智能选择干扰项
- 实时统计测试结果
- 支持双向翻译测试

### 复习模块详解

#### ReviewFragment.kt (复习片段)
**位置**: `ui/review/ReviewFragment.kt`
```kotlin
class ReviewFragment : Fragment() {
    private var _binding: FragmentReviewBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ReviewViewModel
    private lateinit var wordAdapter: ReviewWordAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewBinding.inflate(inflater, container, false)
        
        val database = VocabularyDatabase.getDatabase(requireContext())
        val repository = WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )
        
        viewModel = ViewModelProvider(
            this,
            ReviewViewModelFactory(repository)
        )[ReviewViewModel::class.java]
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupUI()
        observeViewModel()
        val today = viewModel.getTodayZeroed()
        viewModel.loadWordsForDate(today, requireContext())
    }
    
    private fun setupRecyclerView() {
        wordAdapter = ReviewWordAdapter { word ->
            val action = ReviewFragmentDirections.actionReviewFragmentToWordDisplayFragment(word.id)
            findNavController().navigate(action)
        }
        binding.recyclerViewWords.apply {
            adapter = wordAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
}
```
**设计特点**:
- RecyclerView 高效显示列表
- 日期选择器支持按日期查看
- 简洁的交互设计
- 响应式数据更新

### 收藏模块详解

#### FavoritesFragment.kt (收藏片段)
**位置**: `ui/favorites/FavoritesFragment.kt`
```kotlin
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
}
```
**设计特点**:
- RecyclerView 高效显示列表
- 空状态处理提升用户体验
- 简洁的交互设计
- 响应式数据更新

### 词库模块详解

#### LibraryFragment.kt (词库片段)
**位置**: `ui/library/LibraryFragment.kt`
```kotlin
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

        viewModel.loadUncompletedWords(requireContext())
    }
}
```
**设计特点**:
- TabLayout 支持未掌握/已掌握单词切换
- RecyclerView 高效显示列表
- 简洁的交互设计
- 响应式数据更新

### 搜索模块详解

#### SearchFragment.kt (搜索片段)
**位置**: `ui/search/SearchFragment.kt`
```kotlin
class SearchFragment : Fragment(), SearchView.OnQueryTextListener {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SearchViewModel
    private lateinit var searchAdapter: WordSearchAdapter

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
    }
}
```
**设计特点**:
- SearchView 实现搜索功能
- RecyclerView 显示搜索结果
- 实时搜索响应
- 响应式数据更新

### 设置模块详解

#### SettingsFragment.kt (设置片段)
**位置**: `ui/settings/SettingsFragment.kt`
```kotlin
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: SettingsViewModel
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "vocabulary_settings"
        private const val KEY_DAILY_WORD_COUNT = "daily_word_count"
        private const val KEY_CURRENT_WORD_BANK = "current_word_bank"
        private const val DEFAULT_DAILY_WORD_COUNT = 10
        private const val DEFAULT_WORD_BANK = "六级核心"
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        
        val database = VocabularyDatabase.getDatabase(requireContext())
        val repository = WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )
        
        viewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(repository)
        )[SettingsViewModel::class.java]
        
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        loadSettings()
    }
    
    private fun setupUI() {
        binding.seekBarDailyWords.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val wordCount = progress + 1
                binding.textDailyWordCount.text = getString(R.string.daily_word_count_format, wordCount)
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val wordCount = (seekBar?.progress ?: 0) + 1
                saveDailyWordCount(wordCount)
            }
        })
        
        binding.buttonSelectWordBank.setOnClickListener {
            showWordBankSelectionDialog()
        }
    }
}
```
**设计特点**:
- SharedPreferences 持久化设置
- SeekBar 提供直观的数值调节
- 词库选择对话框
- 数据导入/导出功能

---

## 工具类架构

### WordDataLoader.kt (单词数据加载器)
**位置**: `utils/WordDataLoader.kt`
```kotlin
object WordDataLoader {
    fun getAvailableWordBanks(): List<String> {
        return listOf(
            "六级核心",
            "托福核心",
            "雅思核心",
            "四级核心",
            "小升初核心",
            "中考核心",
            "高考核心"
        )
    }
    
    fun getWordBankFileNames(wordBank: String): List<String> {
        return when (wordBank) {
            "六级核心" -> listOf("cet6_1.json", "cet6_2.json", "cet6_3.json")
            "托福核心" -> listOf("toefl_1.json", "toefl_2.json")
            "雅思核心" -> listOf("ielts_1.json", "ielts_2.json")
            "四级核心" -> listOf("cet4_1.json", "cet4_2.json")
            "小升初核心" -> listOf("xiaoshengchu_1.json")
            "中考核心" -> listOf("zhongkao_1.json")
            "高考核心" -> listOf("gaokao_1.json")
            else -> listOf("cet6_1.json")
        }
    }
    
    suspend fun loadWordsFromAssets(context: Context, fileName: String): List<Word> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.assets.open(fileName)
            val json = inputStream.bufferedReader().use { it.readText() }
            
            val gson = Gson()
            val wordArray = gson.fromJson(json, Array<Word>::class.java)
            
            wordArray.toList().map { word ->
                word.copy(wordBank = getWordBankFromFileName(fileName))
            }
        } catch (e: Exception) {
            throw Exception("加载单词数据失败: ${e.message}")
        }
    }
}
```
**设计特点**:
- Object 单例模式，全局唯一实例
- 支持多种词库
- 协程 + IO 调度器处理文件读取
- Gson 解析 JSON 数据
- 异常处理提供错误信息

### 主活动

#### MainActivity.kt (主活动)
**位置**: `MainActivity.kt`
```kotlin
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupNavigation()
    }
    
    private fun setupNavigation() {
        val navController = findNavController(R.id.nav_host_fragment)
        binding.bottomNavigation.setupWithNavController(navController)
    }
}
```
**设计特点**:
- ViewBinding 替代 findViewById
- Navigation Component 统一导航管理
- Bottom Navigation 提供标签式导航
- 简洁的活动结构

### 应用程序类

#### VocabularyApplication.kt (应用程序类)
**位置**: `VocabularyApplication.kt`
```kotlin
class VocabularyApplication : Application() {
    
    val database by lazy { VocabularyDatabase.getDatabase(this) }
    val repository by lazy { 
        WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )
    }
    
    override fun onCreate() {
        super.onCreate()
    }
}
```
**设计特点**:
- 懒加载初始化，提高启动性能
- 全局单例访问数据库和仓库
- 为未来扩展预留空间
- 依赖注入的基础设施

---

## 设计模式分析

### 1. MVVM 架构模式
**应用位置**: 整个UI层
```
View (Fragment) ←→ ViewModel ←→ Model (Repository)
```
**优势**:
- 分离关注点，提高可测试性
- 双向数据绑定，响应式UI
- 生命周期感知，避免内存泄漏

### 2. Repository 模式
**应用位置**: `WordRepository.kt`
```
ViewModel → Repository → DAO → Database
```
**优势**:
- 统一数据访问接口
- 隐藏数据源实现细节
- 支持多数据源切换

### 3. 单例模式 (Singleton)
**应用位置**: 
- `VocabularyDatabase.kt` - 数据库实例
- `WordDataLoader.kt` - 工具类
```kotlin
companion object {
    @Volatile
    private var INSTANCE: VocabularyDatabase? = null
    
    fun getDatabase(context: Context): VocabularyDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(...)
            INSTANCE = instance
            instance
        }
    }
}
```
**优势**:
- 确保全局唯一实例
- 线程安全的懒加载
- 节省系统资源

### 4. 观察者模式 (Observer)
**应用位置**: LiveData + Observer
```kotlin
viewModel.dailyProgress.observe(viewLifecycleOwner) { progress ->
    updateProgressUI(progress)
}
```
**优势**:
- 响应式数据更新
- 自动生命周期管理
- 解耦数据源和UI

### 5. 适配器模式 (Adapter)
**应用位置**: 
- `WordPagerAdapter.kt` - ViewPager2适配器
- `FavoriteWordAdapter.kt` - RecyclerView适配器
```kotlin
class WordPagerAdapter : ListAdapter<Word, WordViewHolder>(WordDiffCallback())
```
**优势**:
- 数据与视图分离
- 高效的列表更新
- 复用视图组件

### 6. 工厂模式 (Factory)
**应用位置**: `HomeViewModelFactory.kt`
```kotlin
class HomeViewModelFactory(private val repository: WordRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```
**优势**:
- 依赖注入支持
- 参数化对象创建
- 类型安全

### 7. DAO 模式 (Data Access Object)
**应用位置**: 所有 DAO 接口
```kotlin
@Dao
interface WordDao {
    @Query("SELECT * FROM words")
    fun getAllWords(): LiveData<List<Word>>
    
    @Insert
    suspend fun insertWords(words: List<Word>)
}
```
**优势**:
- 数据访问抽象
- SQL 查询封装
- 类型安全的数据操作

### 8. 建造者模式 (Builder)
**应用位置**: Room 数据库构建
```kotlin
Room.databaseBuilder(
    context.applicationContext,
    VocabularyDatabase::class.java,
    "vocabulary_database"
).build()
```
**优势**:
- 复杂对象的分步构建
- 参数配置灵活
- 链式调用语法

---

## 依赖关系图

### 层级依赖关系
```
┌─────────────────────────────────────────┐
│                UI Layer                 │
│  ┌─────────────┐    ┌─────────────┐    │
│  │  Fragment   │    │  ViewModel  │    │
│  │             │◄──►│             │    │
│  └─────────────┘    └─────────────┘    │
│                           │             │
└───────────────────────────┼─────────────┘
                            │
┌───────────────────────────▼─────────────┐
│              Domain Layer               │
│         ┌─────────────────┐             │
│         │   Repository    │             │
│         │                 │             │
│         └─────────────────┘             │
│                   │                     │
└───────────────────┼─────────────────────┘
                    │
┌───────────────────▼─────────────────────┐
│               Data Layer                │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐ │
│  │   DAO   │  │ Entity  │  │Database │ │
│  │         │  │         │  │         │ │
│  └─────────┘  └─────────┘  └─────────┘ │
└─────────────────────────────────────────┘
```

### 模块间依赖关系
```
VocabularyApplication
├── VocabularyDatabase (Singleton)
│   ├── WordDao
│   ├── StudyRecordDao
│   ├── FavoriteWordDao
│   └── DailyGoalDao
├── WordRepository
│   └── 依赖所有 DAO
└── ViewModels
    ├── HomeViewModel
    ├── StudyViewModel
    ├── TestViewModel
    └── 其他 ViewModels
```

### 数据流向
```
User Input → Fragment → ViewModel → Repository → DAO → Database
                ↑                                              ↓
User Interface ← LiveData ← ViewModel ← Repository ← DAO ← Database
```

---

## 优化建议

### 1. 架构优化

#### 1.1 引入依赖注入框架
**当前状态**: 手动依赖注入
**建议**: 引入 Hilt 或 Koin
```kotlin
// 使用 Hilt 的示例
@HiltAndroidApp
class VocabularyApplication : Application()

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VocabularyDatabase {
        return VocabularyDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideWordRepository(database: VocabularyDatabase): WordRepository {
        return WordRepository(
            database.wordDao(),
            database.studyRecordDao(),
            database.favoriteWordDao(),
            database.dailyGoalDao()
        )
    }
}
```
**优势**:
- 自动依赖管理
- 编译时验证
- 减少样板代码

#### 1.2 添加 Use Case 层
**当前状态**: ViewModel 直接调用 Repository
**建议**: 引入 Use Case 层处理业务逻辑
```kotlin
class GetTodayWordsUseCase(private val repository: WordRepository) {
    suspend operator fun invoke(context: Context): List<Word> {
        val sharedPreferences = context.getSharedPreferences("vocabulary_settings", Context.MODE_PRIVATE)
        val dailyWordCount = sharedPreferences.getInt("daily_word_count", 10)
        val currentWordBank = sharedPreferences.getString("current_word_bank", "六级核心") ?: "六级核心"
        return repository.getRandomWordsByWordBank(currentWordBank, dailyWordCount)
    }
}

class ToggleFavoriteUseCase(private val repository: WordRepository) {
    suspend operator fun invoke(wordId: Long) {
        repository.toggleFavorite(wordId)
    }
}
```
**优势**:
- 业务逻辑复用
- 单一职责原则
- 更好的测试性

### 2. 性能优化

#### 2.1 数据库优化
**建议**:
```kotlin
// 添加数据库索引
@Entity(
    tableName = "study_records",
    indices = [
        Index(value = ["wordId"]),
        Index(value = ["studyDate"]),
        Index(value = ["wordId", "studyDate"], unique = true)
    ]
)
data class StudyRecord(...)

// 使用分页加载
@Query("SELECT * FROM words ORDER BY id LIMIT :limit OFFSET :offset")
suspend fun getWordsPaged(limit: Int, offset: Int): List<Word>
```

#### 2.2 内存优化
**建议**:
```kotlin
// 使用 Paging 3 处理大列表
class WordPagingSource(private val wordDao: WordDao) : PagingSource<Int, Word>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Word> {
        // 实现分页逻辑
    }
}

// 图片缓存和压缩
class ImageLoader {
    fun loadImage(url: String, imageView: ImageView) {
        // 使用 Glide 或 Coil 加载图片
    }
}
```

### 3. 功能扩展

#### 3.1 智能复习算法
**建议**: 实现艾宾浩斯遗忘曲线算法
```kotlin
class SpacedRepetitionAlgorithm {
    fun calculateNextReviewDate(
        studyHistory: List<StudyRecord>
    ): Date {
        // A simplified interval calculation based on study frequency
        val interval = when (studyHistory.size) {
            0 -> 1 // 第一次学习，1天后复习
            1 -> 3 // 第二次学习，3天后复习
            2 -> 7 // 第三次学习，7天后复习
            else -> {
                // A simple progressive interval
                (studyHistory.size * 3).coerceAtMost(30)
            }
        }
        
        return Date(System.currentTimeMillis() + interval * 24 * 60 * 60 * 1000)
    }
}
```

#### 3.2 学习统计分析
**建议**: 添加详细的学习分析功能
```kotlin
data class LearningStatistics(
    val totalWordsLearned: Int,
    val averageAccuracy: Double,
    val learningStreak: Int,
    val weeklyProgress: List<DailyProgress>,
    val categoryProgress: Map<String, CategoryProgress>
)

class StatisticsRepository {
    suspend fun getLearningStatistics(userId: String): LearningStatistics {
        // 计算各种统计数据
    }
    
    suspend fun getWeeklyTrend(): List<DailyProgress> {
        // 获取一周的学习趋势
    }
}
```

### 4. 用户体验优化

#### 4.1 个性化学习
**建议**:
```kotlin
class PersonalizationEngine {
    fun recommendWords(user: User, studyHistory: List<StudyRecord>): List<Word> {
        // 基于学习历史推荐单词
        val weakWords = findWeakWords(studyHistory)
        val newWords = getNewWordsByDifficulty(user.level)
        val reviewWords = getWordsForReview(studyHistory)
        
        return combineRecommendations(weakWords, newWords, reviewWords)
    }
    
    fun adjustDifficulty(user: User, recentPerformance: List<TestResult>) {
        // 根据最近表现调整难度
    }
}
```

#### 4.2 语音功能
**建议**: 集成语音识别和合成
```kotlin
class VoiceManager {
    fun speakWord(word: String, language: String) {
        // 使用 TextToSpeech 播放发音
    }
    
    fun startVoiceRecognition(callback: (String) -> Unit) {
        // 使用 SpeechRecognizer 识别发音
    }
    
    fun evaluatePronunciation(word: String, userAudio: ByteArray): PronunciationScore {
        // 评估发音准确性
    }
}
```

### 5. 代码质量优化

#### 5.1 错误处理
**建议**: 统一错误处理机制
```kotlin
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Exception) : Result<T>()
    data class Loading<T>(val message: String = "") : Result<T>()
}

class WordRepository {
    suspend fun getWords(): Result<List<Word>> {
        return try {
            Result.Loading("加载单词中...")
            val words = wordDao.getAllWords()
            Result.Success(words)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

#### 5.2 测试覆盖
**建议**: 添加完整的测试套件
```kotlin
// 单元测试
@Test
fun `test word repository returns correct words`() = runTest {
    // Given
    val mockWords = listOf(Word(english = "test", chinese = "测试"))
    whenever(wordDao.getRandomWords(5)).thenReturn(mockWords)
    
    // When
    val result = repository.getRandomWords(5)
    
    // Then
    assertEquals(mockWords, result)
}

// UI测试
@Test
fun testStudyFragmentDisplaysWords() {
    // 使用 Espresso 测试UI
}
```

---

## 总结

### 项目优势
1. **清晰的架构**: MVVM + Repository 模式提供良好的代码组织
2. **现代化技术栈**: 使用最新的 Android 开发最佳实践
3. **响应式设计**: LiveData 提供流畅的用户体验
4. **数据完整性**: Room 数据库确保数据一致性
5. **模块化设计**: 各功能模块相对独立，便于维护

### 改进空间
1. **依赖注入**: 引入 Hilt 简化依赖管理
2. **状态管理**: 统一 UI 状态管理
3. **性能优化**: 数据库索引、分页加载
4. **功能扩展**: 智能复习、社交功能
5. **测试覆盖**: 完善单元测试和UI测试

### 扩展建议
1. **个性化学习**: 基于用户行为的智能推荐
2. **多媒体支持**: 语音、图片、视频学习资源
3. **云端同步**: 跨设备学习进度同步
4. **社交功能**: 学习小组、挑战赛
5. **AI辅助**: 智能纠错、学习路径规划

这个架构为单词学习应用提供了坚实的基础，通过持续优化和功能扩展，可以发展成为一个功能完善、用户体验优秀的学习平台。