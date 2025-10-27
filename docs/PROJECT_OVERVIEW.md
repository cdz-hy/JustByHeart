# 项目概览

## 📋 项目信息

| 项目名称 | 雅思核心单词背诵应用 (IELTS Vocabulary App) |
|---------|-------------------------------------------|
| 版本 | 1.0.0 |
| 开发语言 | Kotlin |
| 最低Android版本 | Android 7.0 (API 24) |
| 目标Android版本 | Android 14 (API 34) |
| 架构模式 | MVVM (Model-View-ViewModel) |
| 数据库 | Room Database |
| UI框架 | Material Design 3 |

## 🎯 项目目标

创建一个专为雅思考生设计的单词背诵应用，帮助用户：
- 系统化学习雅思核心词汇
- 通过科学的记忆方法提高学习效率
- 跟踪学习进度，建立良好的学习习惯
- 提供个性化的学习体验

## 🏗️ 技术架构

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
- **Lottie**: 动画效果（预留）

## 📱 功能模块

### 1. 主页模块 (Home)
**文件位置**: `ui/home/`

**主要功能**:
- 显示每日学习进度
- 快速访问学习和复习功能
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

**核心组件**:
- `TestFragment`: 测试界面
- `TestViewModel`: 测试逻辑
- `TestQuestion`: 题目数据模型

### 4. 复习模块 (Review)
**文件位置**: `ui/review/`

**主要功能**:
- 按日期查看历史学习记录
- 重新学习以往单词

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

### 6. 设置模块 (Settings)
**文件位置**: `ui/settings/`

**主要功能**:
- 调整每日学习目标
- 初始化单词数据
- 应用配置管理

**核心组件**:
- `SettingsFragment`: 设置界面
- `SettingsViewModel`: 设置逻辑

## 🗄️ 数据模型

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
    val difficulty: Int,             // 难度等级(1-5)
    val category: String             // 分类
)
```

#### StudyRecord (学习记录)
```kotlin
data class StudyRecord(
    val id: Long,                    // 记录ID
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
    val id: Long,                    // 目标ID
    val date: Date,                  // 目标日期
    val targetWordCount: Int,        // 目标单词数
    val completedWordCount: Int,     // 已完成数
    val isCompleted: Boolean         // 是否完成
)
```

### 数据关系

```
Word (1) ←→ (N) StudyRecord
Word (1) ←→ (N) FavoriteWord
Date (1) ←→ (1) DailyGoal
```

## 🎨 UI设计规范

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

## 📊 性能指标

### 应用性能
- **启动时间**: < 2秒
- **页面切换**: < 500ms
- **数据加载**: < 1秒
- **内存占用**: < 100MB

### 数据库性能
- **单词查询**: < 100ms
- **批量插入**: < 500ms
- **复杂查询**: < 200ms

## 🧪 测试策略

### 测试层级
1. **单元测试**: ViewModel、Repository、Utils
2. **集成测试**: 数据库操作、API调用
3. **UI测试**: 用户交互流程

### 测试覆盖率目标
- **代码覆盖率**: > 80%
- **业务逻辑覆盖率**: > 90%
- **关键路径覆盖率**: 100%

## 🚀 部署流程

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

## 📈 项目指标

### 开发指标
- **代码行数**: ~3000行
- **文件数量**: ~50个
- **模块数量**: 6个主要模块
- **依赖库**: 15个主要依赖

### 功能指标
- **支持单词数**: 1000+
- **测试题型**: 2种（中译英、英译中）
- **学习模式**: 卡片式学习
- **数据存储**: 完全本地化

## 🔮 未来规划

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

## 📚 相关文档

- [用户使用指南](USER_GUIDE.md)
- [开发指南](DEVELOPMENT_GUIDE.md)
- [API文档](API_DOCUMENTATION.md)
- [代码规范](CODE_STYLE_GUIDE.md)

## 👥 团队信息

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

**感谢您对雅思单词背诵应用项目的关注！** 🎉