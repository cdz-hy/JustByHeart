package com.ielts.vocabulary.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ielts.vocabulary.data.converter.DateConverter
import com.ielts.vocabulary.data.dao.*
import com.ielts.vocabulary.data.entity.*

/**
 * 词汇应用的主数据库类
 * 
 * 使用Room数据库框架，定义了应用的数据库结构和配置。
 * 包含四个主要实体：单词、学习记录、收藏单词和每日目标。
 * 
 * 数据库版本：1
 * 数据库名称：vocabulary_database
 */
@Database(
    entities = [
        Word::class,           // 单词实体
        StudyRecord::class,    // 学习记录实体
        FavoriteWord::class,   // 收藏单词实体
        DailyGoal::class       // 每日目标实体
    ],
    version = 1,               // 数据库版本号
    exportSchema = false       // 不导出数据库架构
)
@TypeConverters(DateConverter::class)  // 使用日期转换器处理Date类型
abstract class VocabularyDatabase : RoomDatabase() {
    
    /**
     * 获取单词数据访问对象
     * @return WordDao实例
     */
    abstract fun wordDao(): WordDao
    
    /**
     * 获取学习记录数据访问对象
     * @return StudyRecordDao实例
     */
    abstract fun studyRecordDao(): StudyRecordDao
    
    /**
     * 获取收藏单词数据访问对象
     * @return FavoriteWordDao实例
     */
    abstract fun favoriteWordDao(): FavoriteWordDao
    
    /**
     * 获取每日目标数据访问对象
     * @return DailyGoalDao实例
     */
    abstract fun dailyGoalDao(): DailyGoalDao

    companion object {
        // 使用@Volatile确保多线程环境下的可见性
        @Volatile
        private var INSTANCE: VocabularyDatabase? = null

        /**
         * 获取数据库实例（单例模式）
         * 
         * 使用双重检查锁定模式确保线程安全，
         * 在整个应用生命周期中只创建一个数据库实例。
         * 
         * @param context 应用上下文
         * @return VocabularyDatabase实例
         */
        fun getDatabase(context: Context): VocabularyDatabase {
            // 如果实例已存在，直接返回
            return INSTANCE ?: synchronized(this) {
                // 在同步块中再次检查，避免重复创建
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VocabularyDatabase::class.java,
                    "vocabulary_database"  // 数据库文件名
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}