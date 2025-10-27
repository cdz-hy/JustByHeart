package com.ielts.vocabulary.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.util.Date

/**
 * 学习记录实体类
 * 
 * 记录用户对每个单词的学习情况，包括学习日期、完成状态、正确/错误次数等。
 * 通过外键与Word表关联，当单词被删除时，相关的学习记录也会被级联删除。
 * 
 * @property id 学习记录的唯一标识符
 * @property wordId 关联的单词ID，外键引用Word表
 * @property studyDate 学习日期
 * @property isCompleted 是否已完成学习
 * @property correctCount 在测试中答对的次数
 * @property wrongCount 在测试中答错的次数
 * @property lastReviewDate 最后一次复习的日期（可选）
 */
@Entity(
    tableName = "study_records",
    foreignKeys = [
        ForeignKey(
            entity = Word::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE  // 级联删除：删除单词时同时删除相关学习记录
        )
    ]
)
data class StudyRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wordId: Long,                    // 关联的单词ID
    val studyDate: Date,                 // 学习日期
    val isCompleted: Boolean = false,    // 是否完成学习
    val correctCount: Int = 0,           // 正确答题次数
    val wrongCount: Int = 0,             // 错误答题次数
    val lastReviewDate: Date? = null     // 最后复习日期
)