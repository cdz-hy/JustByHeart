# 开发指南

## 目录

1. [开发环境搭建](#开发环境搭建)
2. [项目架构](#项目架构)
3. [代码规范](#代码规范)
4. [开发流程](#开发流程)
5. [测试指南](#测试指南)
6. [部署说明](#部署说明)
7. [扩展开发](#扩展开发)

## 开发环境搭建

### 必需工具

1. **Android Studio**
   - 版本：最新稳定版
   - 下载地址：https://developer.android.com/studio

2. **Java Development Kit (JDK)**
   - 版本：JDK 11 或更高
   - 推荐使用 JDK 17

3. **Android SDK**
   - 最低API级别：24 (Android 7.0)
   - 目标API级别：34 (Android 14)
   - 编译SDK版本：34

4. **Kotlin**
   - 版本：1.9.0 或更高
   - 已集成在Android Studio中

### 环境配置

1. **克隆项目**
   ```bash
   git clone [项目地址]
   cd JustByHeart
   ```

2. **导入项目**
   - 打开Android Studio
   - 选择 "Open an existing Android Studio project"
   - 选择项目根目录

3. **同步依赖**
   - Android Studio会自动提示同步Gradle
   - 点击 "Sync Now" 等待同步完成

4. **配置模拟器或设备**
   - 创建AVD (Android Virtual Device)
   - 或连接物理Android设备并开启USB调试

## 项目架构

### 整体架构

本项目采用 **MVVM (Model-View-ViewModel)** 架构模式：

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│      View       │    │   ViewModel     │    │     Model       │
│   (Activity/    │◄──►│   (Business     │◄──►│   (Repository/  │
│   Fragment)     │    │    Logic)       │    │    Database)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 目录结构详解

```
app/src/main/java/com/justbyheart/vocabulary/
├── data/                          # 数据层
│   ├── entity/                    # 数据实体类
│   │   ├── Word.kt               # 单词实体
│   │   ├── StudyRecord.kt        # 学习记录实体
│   │   ├── FavoriteWord.kt       # 收藏单词实体
│   │   └── DailyGoal.kt          # 每日目标实体
│   ├── dao/                       # 数据访问对象
│   │   ├── WordDao.kt            # 单词DAO
│   │   ├── StudyRecordDao.kt     # 学习记录DAO
│   │   ├── FavoriteWordDao.kt    # 收藏DAO
│   │   └── DailyGoalDao.kt       # 目标DAO
│   ├── database/                  # 数据库配置
│   │   └── VocabularyDatabase.kt # Room数据库
│   ├── repository/                # 数据仓库
│   │   └── WordRepository.kt     # 统一数据访问
│   └── converter/                 # 数据转换器
│       └── DateConverter.kt      # 日期转换器
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
│   └── WordDataLoader.kt         # 单词数据加载器
├── MainActivity.kt               # 主Activity
└── VocabularyApplication.kt      # 应用程序类
```

### 架构组件

1. **数据层 (Data Layer)**
   - **Entity**: 定义数据结构
   - **DAO**: 数据访问接口
   - **Database**: Room数据库配置
   - **Repository**: 统一数据访问接口

2. **业务逻辑层 (Business Layer)**
   - **ViewModel**: 处理业务逻辑，管理UI状态
   - **ViewModelFactory**: 创建ViewModel实例

3. **表现层 (Presentation Layer)**
   - **Activity**: 应用入口，管理Fragment
   - **Fragment**: UI界面，处理用户交互
   - **Adapter**: RecyclerView适配器

## 代码规范

### Kotlin编码规范

1. **命名规范**
   ```kotlin
   // 类名：大驼峰命名法
   class WordRepository
   
   // 函数名：小驼峰命名法
   fun loadTodayWords()
   
   // 变量名：小驼峰命名法
   private val dailyProgress = MutableLiveData<DailyProgress>()
   
   // 常量：全大写，下划线分隔
   companion object {
       private const val DEFAULT_WORD_COUNT = 10
   }
   ```

2. **文件组织**
   ```kotlin
   // 文件头部注释
   /**
    * 单词数据仓库类
    * 
    * 负责统一管理所有数据访问操作
    */
   
   // 包声明
   package com.justbyheart.vocabulary.data.repository
   
   // 导入语句（按字母顺序排列）
   import androidx.lifecycle.LiveData
   import com.justbyheart.vocabulary.data.dao.WordDao
   
   // 类定义
   class WordRepository(
       private val wordDao: WordDao
   ) {
       // 类内容
   }
   ```

3. **函数编写**
   ```kotlin
   /**
    * 获取随机单词
    * 
    * @param count 单词数量
    * @return 单词列表
    */
   suspend fun getRandomWords(count: Int): List<Word> {
       return wordDao.getRandomWords(count)
   }
   ```

### 注释规范

1. **类注释**
   ```kotlin
   /**
    * 单词实体类
    * 
    * 这个数据类代表了应用中的一个单词对象，包含了单词的所有相关信息。
    * 使用Room数据库进行持久化存储，实现Parcelable接口以支持在Activity/Fragment间传递。
    */
   ```

2. **方法注释**
   ```kotlin
   /**
    * 加载今日学习单词
    * 
    * 根据今日的学习目标数量，随机获取相应数量的单词，
    * 并为每个单词创建学习记录。
    */
   fun loadTodayWords() {
       // 实现代码
   }
   ```

3. **行内注释**
   ```kotlin
   // 获取今日零点时间
   val today = Calendar.getInstance().apply {
       set(Calendar.HOUR_OF_DAY, 0)  // 设置小时为0
       set(Calendar.MINUTE, 0)       // 设置分钟为0
   }.time
   ```

### 资源命名规范

1. **布局文件**
   ```
   activity_main.xml          # Activity布局
   fragment_home.xml          # Fragment布局
   item_word_card.xml         # 列表项布局
   ```

2. **字符串资源**
   ```xml
   <string name="app_name">简约背诵</string>
   <string name="title_home">首页</string>
   <string name="button_start_study">开始学习</string>
   ```

3. **颜色资源**
   ```xml
   <color name="md_theme_light_primary">#6750A4</color>
   <color name="success_green">#4CAF50</color>
   ```

## 开发流程

### 1. 功能开发流程

1. **需求分析**
   - 明确功能需求
   - 设计用户交互流程
   - 确定数据结构

2. **数据层开发**
   - 创建或修改Entity类
   - 编写DAO接口
   - 更新Repository方法

3. **业务逻辑层开发**
   - 创建或修改ViewModel
   - 实现业务逻辑
   - 处理数据状态

4. **UI层开发**
   - 设计布局文件
   - 实现Fragment/Activity
   - 绑定数据和事件

5. **测试验证**
   - 单元测试
   - 集成测试
   - UI测试

### 2. Git工作流程

1. **分支管理**
   ```bash
   # 主分支
   main                    # 生产环境代码
   
   # 开发分支
   develop                 # 开发环境代码
   
   # 功能分支
   feature/add-word-test   # 新功能开发
   
   # 修复分支
   hotfix/fix-crash-bug    # 紧急修复
   ```

2. **提交规范**
   ```bash
   # 提交格式
   <type>(<scope>): <subject>
   
   # 示例
   feat(study): 添加单词学习功能
   fix(test): 修复测试页面崩溃问题
   docs(readme): 更新README文档
   style(ui): 调整主页布局样式
   refactor(dao): 重构单词数据访问层
   ```

### 3. 代码审查

1. **审查要点**
   - 代码逻辑正确性
   - 命名规范性
   - 注释完整性
   - 性能优化
   - 安全性检查

2. **审查工具**
   - Android Studio内置检查
   - Lint静态分析
   - SonarQube代码质量检查

## 测试指南

### 单元测试

1. **测试框架**
   ```kotlin
   // 依赖配置
   testImplementation 'junit:junit:4.13.2'
   testImplementation 'org.mockito:mockito-core:4.6.1'
   ```

2. **ViewModel测试示例**
   ```kotlin
   @Test
   fun `loadTodayWords should update todayWords LiveData`() = runTest {
       // Given
       val mockWords = listOf(
           Word(1, "test", "测试"),
           Word(2, "example", "例子")
       )
       `when`(repository.getRandomWords(10)).thenReturn(mockWords)
       
       // When
       viewModel.loadTodayWords()
       
       // Then
       assertEquals(mockWords, viewModel.todayWords.value)
   }
   ```

### 集成测试

1. **数据库测试**
   ```kotlin
   @Test
   fun insertAndReadWord() = runTest {
       // Given
       val word = Word(english = "test", chinese = "测试")
       
       // When
       val wordId = wordDao.insertWord(word)
       val retrievedWord = wordDao.getWordById(wordId)
       
       // Then
       assertNotNull(retrievedWord)
       assertEquals("test", retrievedWord?.english)
   }
   ```

### UI测试

1. **Espresso测试**
   ```kotlin
   @Test
   fun clickStartStudyButton_opensStudyFragment() {
       // Given
       onView(withId(R.id.button_start_study))
           .check(matches(isDisplayed()))
       
       // When
       onView(withId(R.id.button_start_study))
           .perform(click())
       
       // Then
       onView(withId(R.id.view_pager_words))
           .check(matches(isDisplayed()))
   }
   ```

## 部署说明

### 构建配置

1. **Gradle配置**
   ```kotlin
   android {
       compileSdk 34
       
       defaultConfig {
           minSdk 24
           targetSdk 34
           versionCode 1
           versionName "1.0"
       }
       
       buildTypes {
           release {
               isMinifyEnabled true
               proguardFiles(
                   getDefaultProguardFile("proguard-android-optimize.txt"),
                   "proguard-rules.pro"
               )
           }
       }
   }
   ```

2. **签名配置**
   ```kotlin
   android {
       signingConfigs {
           release {
               storeFile file("keystore/release.keystore")
               storePassword "your_store_password"
               keyAlias "your_key_alias"
               keyPassword "your_key_password"
           }
       }
   }
   ```

### 发布流程

1. **准备发布**
   ```bash
   # 清理项目
   ./gradlew clean
   
   # 运行测试
   ./gradlew test
   
   # 构建发布版本
   ./gradlew assembleRelease
   ```

2. **APK优化**
   - 启用代码混淆
   - 移除未使用资源
   - 压缩图片资源
   - 分包处理（如需要）

## 扩展开发

### 添加新功能

1. **数据层扩展**
   ```kotlin
   // 1. 创建新的Entity
   @Entity(tableName = "word_categories")
   data class WordCategory(
       @PrimaryKey val id: Long,
       val name: String,
       val description: String
   )
   
   // 2. 创建对应的DAO
   @Dao
   interface WordCategoryDao {
       @Query("SELECT * FROM word_categories")
       fun getAllCategories(): LiveData<List<WordCategory>>
   }
   
   // 3. 更新数据库版本
   @Database(
       entities = [Word::class, WordCategory::class],
       version = 2  // 增加版本号
   )
   ```

2. **UI层扩展**
   ```kotlin
   // 1. 创建新的Fragment
   class CategoryFragment : Fragment() {
       // Fragment实现
   }
   
   // 2. 创建对应的ViewModel
   class CategoryViewModel(
       private val repository: WordRepository
   ) : ViewModel() {
       // ViewModel实现
   }
   
   // 3. 添加导航配置
   // 在nav_graph.xml中添加新的destination
   ```

### 性能优化

1. **数据库优化**
   ```kotlin
   // 添加索引
   @Entity(
       tableName = "words",
       indices = [Index(value = ["english"], unique = true)]
   )
   
   // 使用分页加载
   @Query("SELECT * FROM words LIMIT :limit OFFSET :offset")
   suspend fun getWordsPaged(limit: Int, offset: Int): List<Word>
   ```

2. **内存优化**
   ```kotlin
   // 使用WeakReference避免内存泄漏
   class WordAdapter : RecyclerView.Adapter<WordViewHolder>() {
       private var wordsRef: WeakReference<List<Word>>? = null
   }
   
   // 及时释放资源
   override fun onDestroyView() {
       super.onDestroyView()
       _binding = null
   }
   ```

### 国际化支持

1. **多语言资源**
   ```
   res/
   ├── values/strings.xml           # 默认语言（中文）
   ├── values-en/strings.xml        # 英文
   └── values-zh-rTW/strings.xml    # 繁体中文
   ```

2. **代码适配**
   ```kotlin
   // 使用资源ID而不是硬编码字符串
   binding.textTitle.text = getString(R.string.title_home)
   
   // 格式化字符串
   val message = getString(R.string.progress_format, completed, total)
   ```

---

**开发愉快！如有问题，请查阅相关文档或联系开发团队。** 🚀