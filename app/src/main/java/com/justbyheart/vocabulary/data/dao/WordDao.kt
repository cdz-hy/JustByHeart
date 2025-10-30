package com.justbyheart.vocabulary.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.justbyheart.vocabulary.data.entity.Word

/**
 * 单词数据访问对象 (Data Access Object)
 * 
 * 定义了对单词表进行数据库操作的接口方法。
 * 使用Room注解来生成具体的实现代码。
 */
@Dao
interface WordDao {
    
    /**
     * 获取所有单词，按ID排序
     * @return LiveData包装的单词列表，支持自动更新UI
     */
    @Query("SELECT * FROM words ORDER BY id")
    fun getAllWords(): LiveData<List<Word>>

    /**
     * 获取所有单词 (List)
     * @return 单词列表
     */
    @Query("SELECT * FROM words ORDER BY id")
    suspend fun getAllWordsList(): List<Word>

    /**
     * 根据ID获取特定单词
     * @param id 单词的唯一标识符
     * @return 对应的单词对象，如果不存在则返回null
     */
    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Long): Word?

    /**
     * 随机获取指定数量的单词
     * @param count 需要获取的单词数量
     * @return 随机排序的单词列表
     */
    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomWords(count: Int): List<Word>
    
    /**
     * 根据分类随机获取单词
     * @param category 单词分类 (如 "general", "academic" 等)
     * @param count 需要获取的单词数量
     * @return 指定分类的随机单词列表
     */
    @Query("SELECT * FROM words WHERE category = :category ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomWordsByCategory(category: String, count: Int): List<Word>

    /**
     * 插入单个单词
     * @param word 要插入的单词对象
     * @return 插入后的行ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word): Long

    /**
     * 批量插入单词
     * @param words 要插入的单词列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<Word>)

    /**
     * 更新单词信息
     * @param word 要更新的单词对象
     */
    @Update
    suspend fun updateWord(word: Word)

    /**
     * 删除单词
     * @param word 要删除的单词对象
     */
    @Delete
    suspend fun deleteWord(word: Word)

    @Query("SELECT w.* FROM words w WHERE w.id NOT IN (SELECT s.wordId FROM study_records s WHERE s.isCompleted = 1) ORDER BY w.id ASC LIMIT :count")
    suspend fun getUncompletedWords(count: Int): List<Word>

    @Query("SELECT * FROM words WHERE id IN (:ids)")
    suspend fun getWordsByIds(ids: List<Long>): List<Word>

    @Query("SELECT w.* FROM words w WHERE w.id NOT IN (SELECT s.wordId FROM study_records s WHERE s.isCompleted = 1) AND w.id NOT IN (:excludeIds) ORDER BY w.id ASC LIMIT :count")
    suspend fun getAdditionalUncompletedWords(count: Int, excludeIds: List<Long>): List<Word>

    @Query("SELECT w.* FROM words w WHERE w.id IN (SELECT s.wordId FROM study_records s WHERE s.isCompleted = 1) ORDER BY w.id ASC")
    suspend fun getCompletedWords(): List<Word>

    /**
     * 获取单词总数
     * @return 数据库中单词的总数量
     */
    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int
    
    @Query("DELETE FROM words")
    suspend fun deleteAllWords(): Int
}