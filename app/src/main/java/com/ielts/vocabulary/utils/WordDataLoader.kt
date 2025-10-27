package com.ielts.vocabulary.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.ielts.vocabulary.data.entity.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

// 新的JSON数据结构适配类
data class JsonWord(
    @SerializedName("wordRank")
    val wordRank: Int,
    @SerializedName("headWord")
    val headWord: String,
    @SerializedName("content")
    val content: JsonWordContent
)

data class JsonWordContent(
    @SerializedName("word")
    val word: JsonWordDetail
)

data class JsonWordDetail(
    @SerializedName("wordHead")
    val wordHead: String,
    @SerializedName("content")
    val content: JsonWordDetailContent
)

data class JsonWordDetailContent(
    @SerializedName("usphone")
    val usPhone: String?,
    @SerializedName("ukphone")
    val ukPhone: String?,
    @SerializedName("trans")
    val translations: List<JsonTranslation>?,
    @SerializedName("sentence")
    val sentence: JsonSentence?
)

data class JsonTranslation(
    @SerializedName("tranCn")
    val chinese: String?,
    @SerializedName("pos")
    val pos: String?
)

data class JsonSentence(
    @SerializedName("sentences")
    val sentences: List<JsonSentenceDetail>?
)

data class JsonSentenceDetail(
    @SerializedName("sContent")
    val content: String?,
    @SerializedName("sCn")
    val translation: String?
)

object WordDataLoader {
    
    suspend fun loadWordsFromAssets(context: Context): List<Word> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("ielts_words.json").bufferedReader().use { it.readText() }
            val gson = Gson()
            val listType = com.google.gson.reflect.TypeToken.getParameterized(List::class.java, JsonWord::class.java).type
            val jsonWords: List<JsonWord> = gson.fromJson(jsonString, listType)
            
            // 转换为Word实体对象
            jsonWords.map { jsonWord ->
                val detailContent = jsonWord.content.word.content
                val firstTranslation = detailContent.translations?.firstOrNull()
                val firstSentence = detailContent.sentence?.sentences?.firstOrNull()
                
                Word(
                    english = jsonWord.headWord,
                    chinese = firstTranslation?.chinese ?: "",
                    pronunciation = detailContent.ukPhone ?: detailContent.usPhone,
                    definition = firstTranslation?.pos,
                    example = firstSentence?.content,
                    exampleTranslation = firstSentence?.translation,
                    difficulty = 1, // 默认难度
                    category = "ielts" // 默认分类
                )
            }
        } catch (e: IOException) {
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}