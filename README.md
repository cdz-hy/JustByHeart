# 简约背诵 (JustByHeart)

## 📱 项目简介

这是一款用于辅助英文单词背诵的Android应用程序，简称"简背"。它基于程序内置的单词库帮助用户进行高效的单词学习和复习。采用现代化的Material Design 3设计语言，提供科学高效的单词学习体验。

### ✨ 核心功能

- **📚 每日单词学习**: 可自定义每日学习单词数量（1-100个）
- **🎯 智能测试系统**: 中英互译选择题，巩固学习效果
- **⭐ 单词收藏**: 收藏重要单词，便于重点复习
- **📅 学习记录**: 按日期查看历史学习记录
- **📊 进度跟踪**: 实时显示学习进度和完成情况
- **🎨 精美界面**: Material Design 3设计，简洁美观
- **🔊 发音播放**: 支持单词发音播放功能
- **🔁 单词翻转**: 支持单词卡片翻转记忆
- **✅ 掌握标记**: 可直接标记单词为已掌握

### 🏗️ 技术架构

- **开发语言**: Kotlin
- **架构模式**: MVVM (Model-View-ViewModel)
- **数据库**: Room Database
- **UI框架**: Material Design 3
- **导航**: Navigation Component
- **异步处理**: Kotlin Coroutines
- **数据绑定**: Data Binding & View Binding
- **网络请求**: HttpURLConnection
- **JSON解析**: Gson

## 🚀 快速开始

### 环境要求

- Android Studio Arctic Fox 或更高版本
- Android SDK 24 (Android 7.0) 或更高版本
- Kotlin 1.8.0 或更高版本

### 安装步骤

1. **克隆项目**
   ```bash
   git clone [项目地址]
   cd JustByHeart
   ```

2. **打开项目**
   - 使用Android Studio打开项目文件夹
   - 等待Gradle同步完成

3. **运行应用**
   - 连接Android设备或启动模拟器
   - 点击运行按钮或使用快捷键 `Ctrl+R`

4. **初始化数据**
   - 应用首次启动时会自动初始化单词数据
   - 也可以在"设置"页面手动初始化单词数据

## 📖 使用指南

### 首次使用

1. **数据初始化**
   - 应用首次启动时会自动加载初始词汇
   - 也可以在设置页面手动初始化单词数据
   - 当前使用词汇库包含约20个核心词汇

2. **设置学习目标**
   - 在设置页面调整"每日单词数量"
   - 推荐初学者设置为10-20个单词/天

### 日常学习流程

1. **开始学习**
   - 在主页查看今日学习进度
   - 点击"开始学习"或"继续学习"按钮

2. **单词学习**
   - 浏览单词卡片，包含英文、中文、发音、释义、例句
   - 点击卡片可翻转查看背面
   - 点击收藏按钮保存重要单词
   - 点击"已背"图标可直接标记单词为掌握状态
   - 点击喇叭图标可播放单词发音

3. **完成测试**
   - 学习完所有单词后，点击"检测"按钮
   - 完成中英互译选择题
   - 查看测试结果和正确率

4. **复习巩固**
   - 使用"复习"功能回顾历史学习内容
   - 在"收藏"页面重点复习收藏的单词

## 🏛️ 项目结构

```
app/src/main/java/com/justbyheart/vocabulary/
├── data/                          # 数据层
│   ├── entity/                    # 数据实体
│   │   ├── Word.kt               # 单词实体
│   │   ├── StudyRecord.kt        # 学习记录实体
│   │   ├── FavoriteWord.kt       # 收藏单词实体
│   │   └── DailyGoal.kt          # 每日目标实体
│   ├── dao/                       # 数据访问对象
│   │   ├── WordDao.kt            # 单词数据访问
│   │   ├── StudyRecordDao.kt     # 学习记录数据访问
│   │   ├── FavoriteWordDao.kt    # 收藏数据访问
│   │   └── DailyGoalDao.kt       # 目标数据访问
│   ├── database/                  # 数据库配置
│   │   └── VocabularyDatabase.kt # Room数据库
│   ├── repository/                # 数据仓库
│   │   └── WordRepository.kt     # 统一数据访问接口
│   └── converter/                 # 数据转换器
│       └── DateConverter.kt      # 日期转换器
├── ui/                           # UI层
│   ├── home/                     # 主页模块
│   ├── study/                    # 学习模块
│   ├── test/                     # 测试模块
│   ├── review/                   # 复习模块
│   ├── favorites/                 # 收藏模块
│   ├── settings/                 # 设置模块
│   ├── search/                   # 搜索模块
│   ├── library/                  # 词库模块
│   └── worddisplay/              # 单词详情模块
├── utils/                        # 工具类
│   └── WordDataLoader.kt         # 单词数据加载器
├── MainActivity.kt               # 主活动
└── VocabularyApplication.kt      # 应用程序类
```

## 🎨 界面设计

### 主要页面

1. **主页 (Home)**
   - 显示当前日期和学习进度
   - 每日目标完成情况
   - 快速访问学习、复习和词库功能
   - 总体学习进度展示

2. **学习页面 (Study)**
   - 单词卡片式展示，支持翻转
   - 收藏功能和进度显示
   - "已背"标记功能
   - 发音播放功能
   - 测试检测功能

3. **测试页面 (Test)**
   - 选择题形式测试
   - 实时反馈正确答案
   - 测试结果统计

4. **复习页面 (Review)**
   - 日期选择器
   - 历史学习记录查看
   - 单词列表展示

5. **收藏页面 (Favorites)**
   - 收藏单词管理
   - 一键取消收藏
   - 简洁的列表展示

6. **词库页面 (Library)**
   - 查看所有已掌握和未掌握单词
   - 按状态分类展示单词

7. **搜索页面 (Search)**
   - 模糊搜索单词
   - 实时搜索结果展示

8. **设置页面 (Settings)**
   - 每日单词数量调节
   - 数据初始化功能
   - 应用配置管理

## 🗃️ 数据库设计

### 表结构

#### words (单词表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| english | String | 英文单词 |
| chinese | String | 中文释义 |
| pronunciation | String? | 音标发音 |
| definition | String? | 英文释义 |
| example | String? | 例句 |
| exampleTranslation | String? | 例句翻译 |
| difficulty | Int | 难度等级 |
| category | String | 单词分类 |

#### study_records (学习记录表)
| 字段 | 类型 | 说明 |
|------|------|------|
| wordId | Long | 关联单词ID |
| studyDate | Date | 学习日期 |
| isCompleted | Boolean | 是否完成学习 |
| correctCount | Int | 正确答题次数 |
| wrongCount | Int | 错误答题次数 |
| lastReviewDate | Date? | 最后复习日期 |

#### favorite_words (收藏单词表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| wordId | Long | 关联单词ID |
| addedDate | Date | 添加日期 |

#### daily_goals (每日目标表)
| 字段 | 类型 | 说明 |
|------|------|------|
| date | Date | 目标日期 |
| targetWordCount | Int | 目标单词数 |
| completedWordCount | Int | 已完成单词数 |
| isCompleted | Boolean | 是否完成目标 |
| dailyWordIds | String | 当日学习的单词ID列表 |
| flippedWordIds | String | 当日已翻转的单词ID列表 |

## 🔧 开发指南

### 添加新单词

1. 编辑 `app/src/main/assets/vocabulary_words.json` 文件
2. 按照现有格式添加新的单词条目
3. 重新初始化数据或重新安装应用

### 自定义主题

1. 修改 `app/src/main/res/values/colors.xml` 中的颜色定义
2. 更新 `app/src/main/res/values/themes.xml` 中的主题配置
3. 重新编译应用查看效果

### 扩展功能

1. **添加新的测试类型**
   - 在 TestViewModel 中扩展 generateQuestions 方法
   - 添加新的题型逻辑

2. **增加统计功能**
   - 扩展 StudyRecord 实体
   - 在相应的DAO中添加统计查询方法

## 🐛 常见问题

### Q: 应用首次启动时没有单词数据？
A: 应用会自动初始化单词数据。如果失败，可进入"设置"页面手动点击"初始化单词数据"按钮。

### Q: 如何重置学习进度？
A: 目前需要清除应用数据或重新安装应用。后续版本将添加重置功能。

### Q: 可以导入自己的单词列表吗？
A: 当前版本暂不支持，可以通过修改assets中的JSON文件实现。

### Q: 应用支持离线使用吗？
A: 是的，所有功能都支持离线使用，数据存储在本地数据库中。

### Q: 单词发音无法播放怎么办？
A: 请检查网络连接，发音功能需要联网获取音频资源。

## 🤝 贡献指南

欢迎提交Issue和Pull Request来改进这个项目！

### 提交代码前请确保：

1. 代码符合Kotlin编码规范
2. 添加了适当的中文注释
3. 测试了新功能的稳定性
4. 更新了相关文档

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 GitHub Issue
- 发送邮件至：[your-email@example.com]

---

**祝您学习愉快！🎉**