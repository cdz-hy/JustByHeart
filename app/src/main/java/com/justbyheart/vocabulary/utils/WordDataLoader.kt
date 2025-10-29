package com.justbyheart.vocabulary.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.justbyheart.vocabulary.data.entity.Word
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
    val pos: String?,
    @SerializedName("tranOther")
    val tranOther: String?
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
            val jsonString = context.assets.open("vocabulary_words.json").bufferedReader().use { it.readText() }
            val gson = Gson()
            val listType = com.google.gson.reflect.TypeToken.getParameterized(List::class.java, JsonWord::class.java).type
            val jsonWords: List<JsonWord> = gson.fromJson(jsonString, listType)
            
            // 转换为Word实体对象
            jsonWords.map { jsonWord ->
                val detailContent = jsonWord.content.word.content

                // 拼接中文翻译 (pos. 中文翻译；pos. 中文翻译)
                val chineseTranslations = detailContent.translations?.joinToString("；") { tran ->
                    "${tran.pos ?: ""}. ${tran.chinese ?: ""}"
                } ?: ""

                // 拼接英文释义 (pos. 英文释义；pos. 英文释义)
                val englishDefinitions = detailContent.translations?.joinToString("；") { tran ->
                    "${tran.pos ?: ""}. ${tran.tranOther ?: ""}"
                } ?: ""

                // 拼接例句
                val examples = detailContent.sentence?.sentences?.joinToString("\n") { sentence ->
                    sentence.content ?: ""
                } ?: ""

                // 拼接例句翻译
                val exampleTranslations = detailContent.sentence?.sentences?.joinToString("\n") { sentence ->
                    sentence.translation ?: ""
                } ?: ""

                // 格式化音标
                val pronunciation = (detailContent.ukPhone ?: detailContent.usPhone)?.let { "/$it/" }

                Word(
                    english = jsonWord.headWord,
                    chinese = chineseTranslations, // 将拼接后的中文翻译赋值给chinese字段
                    pronunciation = pronunciation,
                    definition = englishDefinitions, // 将拼接后的英文释义赋值给definition字段
                    example = examples,
                    exampleTranslation = exampleTranslations,
                    difficulty = 1, // 默认难度
                    category = "vocabulary" // 默认分类
                )
            }
        } catch (e: IOException) {
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}