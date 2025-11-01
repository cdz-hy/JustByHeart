# 项目概览

## 项目信息

| 项目名称 | 简约背诵 (JustByHeart) |
|---------|-----------------------|
| 版本 | 1.0.0 |
| 开发语言 | Kotlin |
| 最低Android版本 | Android 7.0 (API 24) |
| 目标Android版本 | Android 14 (API 34) |
| 架构模式 | MVVM (Model-View-ViewModel) |
| 数据库 | Room Database |
| UI框架 | Material Design 3 |

## 项目目标

创建一个简洁高效的单词背诵应用，帮助用户：
- 系统化学习各类英语词汇
- 通过科学的记忆方法提高学习效率
- 跟踪学习进度，建立良好的学习习惯
- 提供个性化的学习体验

## 技术架构

### 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                             │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────┐ │
│  │  Fragment   │ │  Activity   │ │       Adapter           │ │
│  └─────────────┘ └─────────────┘ └─────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                   Business Logic Layer                     │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────┐ │
│  │  ViewModel  │ │ ViewModelF. │ │      LiveData           │ │
│  └─────────────┘ └─────────────┘ └─────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                             │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────┐ │
│  │ Repository  │ │     DAO     │ │      Entity             │ │
│  └─────────────┘ └─────────────┘ └─────────────────────────┘ │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────┐ │
│  │  Database   │ │ Converter   │ │      Utils              │ │
│  └─────────────┘ └─────────────┘ └─────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 技术栈

#### 核心框架
- **Kotlin**: 主要开发语言
- **Android Jetpack**: 现代Android开发组件
- **Material Design 3**: UI设计系统

#### 数据层
- **Room Database**: 本地数据持久化
- **LiveData**: 响应式数据观察
- **Kotlin Coroutines**: 异步编程

#### UI层
- **View Binding**: 视图绑定
- **Navigation Component**: 页面导航
- **RecyclerView**: 列表展示
- **ViewPager2**: 页面滑动

#### 工具库
- **Gson**: JSON数据解析

## 核心组件详解

本项目遵循Google推荐的MVVM架构模式，将代码逻辑清晰地分离到不同的组件中，以提高可维护性、可测试性和可扩展性。以下是项目中核心组件的详细说明：

### Fragment (视图控制器)
- **作用**: `Fragment` 是UI层的主要组成部分，负责管理和显示应用界面的特定部分。它承载着用户能看到的所有视觉元素，如按钮、文本、列表等。
- **职责**:
    - **UI渲染**: 将ViewModel提供的数据渲染到屏幕上。
    - **用户交互**: 监听用户的操作（如点击、滑动），并将这些事件通知给`ViewModel`进行处理。
    - **生命周期管理**: `Fragment`拥有自己的生命周期，能响应Android系统的生命周期事件，从而在正确的时机加载数据或释放资源。
- **特点**:
    - 它们是模块化的，可被复用在不同的`Activity`中。
    - 不直接处理业务逻辑或数据获取，保持视图层的纯粹性。
- **文件位置**: `app/src/main/java/com/justbyheart/vocabulary/ui/` 下的各个功能模块包中，例如 `HomeFragment.kt`。

### Entity (数据模型)
- **作用**: `Entity` (或称Model) 是数据层的基石，用于定义应用需要持久化存储的数据结构。在本项目中，它们是Room数据库中的表结构映射。
- **职责**:
    - **数据结构定义**: 声明一个数据对象所包含的字段（如单词的英文、中文、音标等）。
    - **数据库表映射**: 通过Room的注解（如`@Entity`, `@PrimaryKey`, `@ColumnInfo`），将一个Kotlin数据类（`data class`）映射成数据库中的一张表。
- **特点**:
    - 它们是纯粹的数据容器，不包含任何业务逻辑。
- **文件位置**: `app/src/main/java/com/justbyheart/vocabulary/data/entity/`，例如 `Word.kt`。

### ViewModelFactory (视图模型工厂)
- **作用**: `ViewModelFactory` 是一个用于创建`ViewModel`实例的工厂类。当`ViewModel`需要接收参数（如`Repository`）时，必须使用`ViewModelFactory`来实例化它。
- **职责**:
    - **依赖注入**: 将`ViewModel`所需的依赖（例如`WordRepository`）传递给`ViewModel`的构造函数。
    - **实例化ViewModel**: 确保`ViewModel`在`Fragment`或`Activity`的生命周期内被正确创建和管理，避免在屏幕旋转等配置更改时丢失数据。
- **特点**:
    - 它是连接`ViewModel`和其依赖项的桥梁。
- **文件位置**: `app/src/main/java/com/justbyheart/vocabulary/ui/` 下的各个功能模块包中，例如 `HomeViewModelFactory.kt`。

### Adapter (数据适配器)
- **作用**: `Adapter` 是连接数据和UI列表（如`RecyclerView`）的桥梁。它负责将数据集合中的每一项数据转换成一个可以在列表中显示的视图项。
- **职责**:
    - **视图创建**: 为列表中的每个项目创建一个`ViewHolder`。
    - **数据绑定**: 将特定位置的数据绑定到`ViewHolder`所持有的视图上。
    - **高效更新**: 通过`DiffUtil`等机制，高效地更新列表内容，只刷新发生变化的部分，从而提高性能。
- **特点**:
    - 它是`RecyclerView`或`ViewPager2`等组件不可或缺的一部分。
- **文件位置**: `app/src/main/java/com/justbyheart/vocabulary/ui/` 下的各个功能模块包中，例如 `FavoriteWordAdapter.kt`。

### DAO (数据访问对象)
- **作用**: `DAO` (Data Access Object) 是数据访问层的核心接口，它定义了所有与数据库交互的操作。
- **职责**:
    - **数据库操作抽象**: 提供清晰的、类型安全的方法（如`insert`, `query`, `update`, `delete`）来操作数据库，而无需编写复杂的SQL语句。
    - **SQL查询定义**: 使用Room的注解（如`@Query`, `@Insert`）将Kotlin/Java方法映射到具体的SQL查询。
    - **返回响应式数据**: 可以返回`LiveData`或`Flow`，使UI能够自动响应数据库的变化。
- **特点**:
    - 它是`Repository`与`Database`之间的中间层。
- **文件位置**: `app/src/main/java/com/justbyheart/vocabulary/data/dao/`，例如 `WordDao.kt`。

### Converter (类型转换器)
- **作用**: `Converter` 用于在Room数据库存储和读取数据时，对不支持的自定义数据类型进行转换。Room本身只支持基本数据类型（如`String`, `Int`, `Long`等）。
- **职责**:
    - **对象到数据库类型的转换**: 将一个自定义对象（如`Date`）转换成Room可以存储的类型（如`Long`）。
    - **数据库类型到对象的转换**: 将从数据库中读取的数据（如`Long`）转换回原始的自定义对象（如`Date`）。
- **特点**:
    - 通过`@TypeConverter`注解实现。
- **文件位置**: `app/src/main/java/com/justbyheart/vocabulary/data/converter/`，例如 `DateConverter.kt`。

## 功能模块

### 1. 主页模块 (Home)
**文件位置**: `ui/home/`

**主要功能**:
- 显示每日学习进度
- 快速访问学习、搜索、复习和词库功能
- 学习目标设置入口

**核心组件**:
- `HomeFragment`: 主页界面
- `HomeViewModel`: 业务逻辑处理
- `DailyProgress`: 进度数据模型

### 2. 学习模块 (Study)
**文件位置**: `ui/study/`

**主要功能**:
- 单词卡片式学习
- 收藏重要单词
- 学习进度跟踪
- 单词翻转隐藏中文释义

**核心组件**:
- `StudyFragment`: 学习界面
- `StudyViewModel`: 学习逻辑
- `WordPagerAdapter`: 单词卡片适配器

### 3. 测试模块 (Test)
**文件位置**: `ui/test/`

**主要功能**:
- 中英互译选择题
- 即时答案反馈
- 测试结果统计
- 震动反馈

**核心组件**:
- `TestFragment`: 测试界面
- `TestViewModel`: 测试逻辑
- `TestQuestion`: 题目数据模型

### 4. 复习模块 (Review)
**文件位置**: `ui/review/`

**主要功能**:
- 按日期查看历史学习记录
- 重新学习以往单词
- 日期选择器

**核心组件**:
- `ReviewFragment`: 复习界面
- `ReviewViewModel`: 复习逻辑
- `ReviewWordAdapter`: 单词列表适配器

### 5. 收藏模块 (Favorites)
**文件位置**: `ui/favorites/`

**主要功能**:
- 管理收藏的单词
- 快速取消收藏

**核心组件**:
- `FavoritesFragment`: 收藏界面
- `FavoritesViewModel`: 收藏逻辑
- `FavoriteWordAdapter`: 收藏列表适配器

### 6. 词库模块 (Library)
**文件位置**: `ui/library/`

**主要功能**:
- 查看所有未掌握和已掌握单词
- 标签页切换未掌握/已掌握单词列表

**核心组件**:
- `LibraryFragment`: 词库界面
- `LibraryViewModel`: 词库逻辑
- `LibraryWordAdapter`: 单词列表适配器

### 7. 搜索模块 (Search)
**文件位置**: `ui/search/`

**主要功能**:
- 模糊搜索单词
- 实时显示搜索结果

**核心组件**:
- `SearchFragment`: 搜索界面
- `SearchViewModel`: 搜索逻辑
- `WordSearchAdapter`: 搜索单词列表适配器

### 8. 设置模块 (Settings)
**文件位置**: `ui/settings/`

**主要功能**:
- 调整每日学习目标
- 切换词库
- 数据导入/导出
- 主题颜色切换

**核心组件**:
- `SettingsFragment`: 设置界面
- `SettingsViewModel`: 设置逻辑

## 数据模型

### 核心实体

#### Word (单词)
```kotlin
data class Word(
    val id: Long,                    // 唯一标识
    val english: String,             // 英文单词
    val chinese: String,             // 中文释义
    val pronunciation: String?,      // 音标
    val definition: String?,         // 英文定义
    val example: String?,            // 例句
    val exampleTranslation: String?, // 例句翻译
    val category: String,            // 分类
    val synos: String?,              // 同义词
    val phrases: String?,            // 短语
    val relWord: String?,            // 同根词
    val wordBank: String             // 词库来源类别
)
```

#### StudyRecord (学习记录)
```kotlin
data class StudyRecord(
    val wordId: Long,                // 关联单词ID
    val studyDate: Date,             // 学习日期
    val isCompleted: Boolean,        // 是否完成
    val correctCount: Int,           // 正确次数
    val wrongCount: Int,             // 错误次数
    val lastReviewDate: Date?        // 最后复习日期
)
```

#### FavoriteWord (收藏单词)
```kotlin
data class FavoriteWord(
    val id: Long,                    // 收藏ID
    val wordId: Long,                // 单词ID
    val addedDate: Date              // 收藏日期
)
```

#### DailyGoal (每日目标)
```kotlin
data class DailyGoal(
    val date: Date,                  // 目标日期
    val targetWordCount: Int,        // 目标单词数
    val completedWordCount: Int,     // 已完成数
    val isCompleted: Boolean,        // 是否完成
    val dailyWordIds: String,        // 当日学习的单词ID列表
    val flippedWordIds: String       // 当日已翻转的单词ID列表
)
```

### 数据关系

```
Word (1) ←→ (N) StudyRecord
Word (1) ←→ (N) FavoriteWord
Date (1) ←→ (1) DailyGoal
```

## UI设计规范

### 设计原则
- **简洁性**: 界面简洁，突出核心功能
- **一致性**: 保持统一的视觉风格
- **易用性**: 操作直观，学习成本低
- **美观性**: 现代化的Material Design 3设计

### 颜色方案
- **主色调**: #6750A4 (紫色)
- **成功色**: #4CAF50 (绿色)
- **警告色**: #FF9800 (橙色)
- **错误色**: #F44336 (红色)

### 字体规范
- **标题**: 20sp, Medium
- **副标题**: 16sp, Regular
- **正文**: 14sp, Regular
- **说明**: 12sp, Regular

## 性能指标

### 应用性能
- **启动时间**: < 2秒
- **页面切换**: < 500ms
- **数据加载**: < 1秒
- **内存占用**: < 100MB

### 数据库性能
- **单词查询**: < 100ms
- **批量插入**: < 500ms
- **复杂查询**: < 200ms

## 测试策略

### 测试层级
1. **单元测试**: ViewModel、Repository、Utils
2. **集成测试**: 数据库操作、API调用
3. **UI测试**: 用户交互流程

### 测试覆盖率目标
- **代码覆盖率**: > 80%
- **业务逻辑覆盖率**: > 90%
- **关键路径覆盖率**: 100%

## 部署流程

### 构建配置
```kotlin
buildTypes {
    debug {
        isDebuggable = true
        applicationIdSuffix = ".debug"
    }
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(...)
    }
}
```

### 发布检查清单
- [ ] 代码审查通过
- [ ] 所有测试通过
- [ ] 性能测试达标
- [ ] UI测试验证
- [ ] 安全扫描通过
- [ ] 版本号更新
- [ ] 发布说明准备

## 项目指标

### 开发指标
- **代码行数**: ~5000行
- **文件数量**: ~80个
- **模块数量**: 8个主要模块
- **依赖库**: 20个主要依赖

### 功能指标
- **支持词库**: 7种（六级、四级、托福、雅思等）
- **测试题型**: 2种（中译英、英译中）
- **学习模式**: 卡片式学习
- **数据存储**: 完全本地化

## 未来规划

### 短期目标 (v1.1)
- [ ] 添加单词发音功能
- [ ] 支持学习提醒通知
- [ ] 增加学习统计图表
- [ ] 优化测试算法

### 中期目标 (v1.2)
- [ ] 支持自定义单词导入
- [ ] 添加学习计划制定
- [ ] 实现云端数据同步
- [ ] 支持多种测试模式

### 长期目标 (v2.0)
- [ ] AI智能推荐学习内容
- [ ] 语音识别练习功能
- [ ] 社交学习功能
- [ ] 多语言支持

## 相关文档

- [用户使用指南](USER_GUIDE.md)
- [开发指南](DEVELOPMENT_GUIDE.md)
- [API文档](API_DOCUMENTATION.md)
- [代码规范](CODE_STYLE_GUIDE.md)

## 团队信息

### 开发团队
- **项目负责人**: [姓名]
- **Android开发**: [姓名]
- **UI设计**: [姓名]
- **测试工程师**: [姓名]

### 联系方式
- **项目邮箱**: [email@example.com]
- **技术支持**: [support@example.com]
- **GitHub**: [项目地址]

---

**感谢您对简约背诵应用项目的关注！** 🎉