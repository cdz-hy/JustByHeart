package com.ielts.vocabulary.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 单词实体类
 * 
 * 这个数据类代表了应用中的一个单词对象，包含了单词的所有相关信息。
 * 使用Room数据库进行持久化存储，实现Parcelable接口以支持在Activity/Fragment间传递。
 * 
 * @property id 单词的唯一标识符，由数据库自动生成
 * @property english 英文单词
 * @property chinese 中文释义
 * @property pronunciation 音标发音（可选）
 * @property definition 英文释义（可选）
 * @property example 例句（可选）
 * @property exampleTranslation 例句翻译（可选）
 * @property difficulty 难度等级，范围1-5，1为最简单，5为最难
 * @property category 单词分类，如"general"、"academic"、"business"等
 */
@Parcelize
@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val english: String,                    // 英文单词
    val chinese: String,                    // 中文释义
    val pronunciation: String? = null,      // 音标发音
    val definition: String? = null,         // 英文释义
    val example: String? = null,            // 例句
    val exampleTranslation: String? = null, // 例句翻译
    val difficulty: Int = 1,                // 难度等级 (1-5)
    val category: String = "general"        // 单词分类
) : Parcelable