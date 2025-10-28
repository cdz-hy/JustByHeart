package com.justbyheart.vocabulary.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.justbyheart.vocabulary.data.entity.FavoriteWord
import com.justbyheart.vocabulary.data.entity.Word

@Dao
interface FavoriteWordDao {
    @Query("""
        SELECT w.* FROM words w 
        INNER JOIN favorite_words f ON w.id = f.wordId 
        ORDER BY f.addedDate DESC
    """)
    fun getFavoriteWords(): LiveData<List<Word>>

    @Query("SELECT * FROM favorite_words WHERE wordId = :wordId")
    suspend fun getFavoriteByWordId(wordId: Long): FavoriteWord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favoriteWord: FavoriteWord): Long

    @Query("DELETE FROM favorite_words WHERE wordId = :wordId")
    suspend fun deleteFavoriteByWordId(wordId: Long)

    @Query("SELECT COUNT(*) FROM favorite_words WHERE wordId = :wordId")
    suspend fun isFavorite(wordId: Long): Int
}