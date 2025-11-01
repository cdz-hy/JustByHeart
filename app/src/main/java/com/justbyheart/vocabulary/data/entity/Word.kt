package com.justbyheart.vocabulary.data.entity

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
 * @property translations 中文释义
 * @property pronunciation 音标发音（可选）
 * @property englishDefinitions 英文释义（可选）
 * @property examples 例句 (例句1\n例句2)
 * @property exampleTranslations 例句翻译 (例句翻译1\n例句翻译2)
 * @property category 单词分类，如"general"、"academic"、"business"等
 * @property synos 同义词 (pos: 词性, tran: 中文释义, hwds: 同义词列表)
 * @property phrases 短语 (pContent: 短语, pCn: 中文释义)
 * @property relWord 同根词 (pos: 词性, words: 相关派生词及释义)
 * @property wordBank 词库来源类别
 */
@Parcelize
@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val english: String,                    // 英文单词
    val chinese: String,               // 中文释义 (pos. 中文翻译；pos. 中文翻译)
    val pronunciation: String? = null,      // 音标发音
    val definition: String? = null, // 英文释义 (pos. 英文释义；pos. 英文释义)
    val example: String? = null,           // 例句 (例句1\n例句2)
    val exampleTranslation: String? = null, // 例句翻译 (例句翻译1\n例句翻译2)
    val category: String = "general",        // 单词分类
    val synos: String? = null,              // 同义词 (pos: 词性, tran: 中文释义, hwds: 同义词列表)
    val phrases: String? = null,            // 短语 (pContent: 短语, pCn: 中文释义)
    val relWord: String? = null,             // 同根词 (pos: 词性, words: 相关派生词及释义)
    val wordBank: String = "vocabulary"     // 词库来源类别
) : Parcelable