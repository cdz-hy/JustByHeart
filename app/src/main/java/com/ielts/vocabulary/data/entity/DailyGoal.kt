package com.ielts.vocabulary.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 每日学习目标实体类
 * 
 * 记录用户每天的学习目标和完成情况，用于跟踪学习进度。
 * 每个日期对应一个学习目标记录。
 * 
 * @property id 目标记录的唯一标识符
 * @property date 目标日期
 * @property targetWordCount 目标学习单词数量，默认为10个
 * @property completedWordCount 已完成的单词数量
 * @property isCompleted 是否完成当日目标
 */
@Entity(tableName = "daily_goals")
data class DailyGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date,                      // 目标日期
    val targetWordCount: Int = 10,       // 目标单词数量
    val completedWordCount: Int = 0,     // 已完成单词数量
    val isCompleted: Boolean = false     // 是否完成目标
)