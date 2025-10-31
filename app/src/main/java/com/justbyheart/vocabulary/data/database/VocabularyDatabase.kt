package com.justbyheart.vocabulary.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.justbyheart.vocabulary.data.converter.DateConverter
import com.justbyheart.vocabulary.data.dao.*
import com.justbyheart.vocabulary.data.entity.*

/**
 * 词汇应用的主数据库类
 * 
 * 使用Room数据库框架，定义了应用的数据库结构和配置。
 * 包含四个主要实体：单词、学习记录、收藏单词和每日目标。
 * 
 * 数据库版本：4 (更新了Word实体类，添加了wordBank字段)
 * 数据库名称：vocabulary_database
 */
@Database(
    entities = [
        Word::class,           // 单词实体
        StudyRecord::class,    // 学习记录实体
        FavoriteWord::class,   // 收藏单词实体
        DailyGoal::class       // 每日目标实体
    ],
    version = 4,               // 数据库版本号 (从3更新到4)
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
        // 数据库名称
        private const val DATABASE_NAME = "vocabulary_database"
        
        // 数据库版本1到2的迁移
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 为Word表添加新列
                db.execSQL("ALTER TABLE words ADD COLUMN synos TEXT")
                db.execSQL("ALTER TABLE words ADD COLUMN phrases TEXT")
                db.execSQL("ALTER TABLE words ADD COLUMN relWord TEXT")
            }
        }
        
        // 数据库版本2到3的迁移
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 为Word表添加wordBank列
                db.execSQL("ALTER TABLE words ADD COLUMN wordBank TEXT DEFAULT 'vocabulary'")
            }
        }
        
        // 数据库版本3到4的迁移
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 更新现有记录的wordBank字段
                db.execSQL("UPDATE words SET wordBank = '六级核心' WHERE wordBank = 'vocabulary'")
            }
        }
        
        @Volatile
        private var INSTANCE: VocabularyDatabase? = null
        
        /**
         * 获取数据库实例（单例模式）
         * 
         * 使用双重检查锁定确保线程安全，同时避免不必要的同步开销。
         * 
         * @param context 应用上下文
         * @return VocabularyDatabase实例
         */
        fun getDatabase(context: Context): VocabularyDatabase {
            // 如果已经有实例，直接返回
            return INSTANCE ?: synchronized(this) {
                // 再次检查实例是否已创建（双重检查锁定）
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VocabularyDatabase::class.java,
                    DATABASE_NAME
                )
                // 添加数据库迁移
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}