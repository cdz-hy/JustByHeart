package com.justbyheart.vocabulary.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.justbyheart.vocabulary.data.entity.Word

/**
 * 单词数据访问对象(DAO)
 * 
 * 定义了对单词表进行增删改查操作的方法接口
 * Room会自动生成这些方法的实现代码
 */
@Dao
interface WordDao {
    
    /**
     * 获取所有单词
     * @return 包含所有单词的LiveData列表
     */
    @Query("SELECT * FROM words")
    fun getAllWords(): LiveData<List<Word>>
    
    /**
     * 根据ID获取单词
     * @param id 单词ID
     * @return 对应的单词对象
     */
    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Long): Word?

    /**
     * 随机获取指定数量的单词
     * @param count 需要获取的单词数量
     * @return 随机单词列表
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
     * 根据词库获取所有单词
     * @param wordBank 词库名称
     * @return 指定词库的所有单词
     */
    @Query("SELECT * FROM words WHERE wordBank = :wordBank")
    suspend fun getWordsByWordBank(wordBank: String): List<Word>
    
    /**
     * 根据词库随机获取指定数量的单词
     * @param wordBank 词库名称
     * @param count 需要获取的单词数量
     * @return 指定词库的随机单词列表
     */
    @Query("SELECT * FROM words WHERE wordBank = :wordBank ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomWordsByWordBank(wordBank: String, count: Int): List<Word>
    
    /**
     * 根据英文单词和词库查找单词
     * @param english 英文单词
     * @param wordBank 词库名称
     * @return 匹配的单词对象
     */
    @Query("SELECT * FROM words WHERE english = :english AND wordBank = :wordBank LIMIT 1")
    suspend fun getWordByEnglishAndWordBank(english: String, wordBank: String): Word?
    
    /**
     * 检查指定词库是否存在单词
     * @param wordBank 词库名称
     * @return 该词库中的单词数量
     */
    @Query("SELECT COUNT(*) FROM words WHERE wordBank = :wordBank")
    suspend fun getWordCountByWordBank(wordBank: String): Int
    
    /**
     * 在指定词库中搜索单词
     * @param query 搜索关键词
     * @param wordBank 词库名称
     * @return 匹配的单词列表
     */
    @Query("SELECT * FROM words WHERE wordBank = :wordBank AND (english LIKE '%' || :query || '%' OR chinese LIKE '%' || :query || '%')")
    suspend fun searchWordsInWordBank(query: String, wordBank: String): List<Word>

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
    
    // 添加按词库过滤的未完成单词查询
    @Query("SELECT w.* FROM words w WHERE w.wordBank = :wordBank AND w.id NOT IN (SELECT s.wordId FROM study_records s WHERE s.isCompleted = 1) ORDER BY w.id ASC LIMIT :count")
    suspend fun getUncompletedWordsByWordBank(wordBank: String, count: Int): List<Word>
    
    // 添加按词库过滤的额外未完成单词查询
    @Query("SELECT w.* FROM words w WHERE w.wordBank = :wordBank AND w.id NOT IN (SELECT s.wordId FROM study_records s WHERE s.isCompleted = 1) AND w.id NOT IN (:excludeIds) ORDER BY w.id ASC LIMIT :count")
    suspend fun getAdditionalUncompletedWordsByWordBank(wordBank: String, count: Int, excludeIds: List<Long>): List<Word>

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
    
    /**
     * 删除指定词库的所有单词
     * @param wordBank 词库名称
     */
    @Query("DELETE FROM words WHERE wordBank = :wordBank")
    suspend fun deleteWordsByWordBank(wordBank: String): Int
}