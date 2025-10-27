package com.ielts.vocabulary.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.util.Date

/**
 * 收藏单词实体类
 * 
 * 记录用户收藏的单词信息，包括收藏的单词ID和添加时间。
 * 通过外键与Word表关联，确保数据一致性。
 * 
 * @property id 收藏记录的唯一标识符
 * @property wordId 被收藏的单词ID，外键引用Word表
 * @property addedDate 添加到收藏夹的日期，默认为当前时间
 */
@Entity(
    tableName = "favorite_words",
    foreignKeys = [
        ForeignKey(
            entity = Word::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE  // 级联删除：删除单词时同时删除收藏记录
        )
    ]
)
data class FavoriteWord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wordId: Long,                    // 被收藏的单词ID
    val addedDate: Date = Date()         // 收藏日期，默认为当前时间
)