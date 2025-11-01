package com.justbyheart.vocabulary.data.model

import com.google.gson.annotations.SerializedName

/**
 * 导出数据模型 - 代表一个词库的数据
 */
data class ExportWordBank(
    @SerializedName("词库名称")
    val wordBankName: String,
    
    @SerializedName("已背单词")
    val memorizedWords: List<List<String>>,
    
    @SerializedName("收藏单词")
    val favoriteWords: List<String>
)

/**
 * 导出数据模型 - 代表完整的导出数据
 */
typealias ExportData = List<ExportWordBank>