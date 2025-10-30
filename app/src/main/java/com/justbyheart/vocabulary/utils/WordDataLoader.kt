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
    val sentence: JsonSentence?,
    @SerializedName("syno")
    val synos: JsonSynos?,
    @SerializedName("phrase")
    val phrases: JsonPhrases?,
    @SerializedName("relWord")
    val relWord: JsonRelWord?
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

data class JsonSynos(
    @SerializedName("synos")
    val synos: List<JsonSynoDetail>?
)

data class JsonSynoDetail(
    @SerializedName("pos")
    val pos: String?,
    @SerializedName("tran")
    val tran: String?,
    @SerializedName("hwds")
    val hwds: List<JsonHwd>?
)

data class JsonHwd(
    @SerializedName("w")
    val w: String?
)

data class JsonPhrases(
    @SerializedName("phrases")
    val phrases: List<JsonPhraseDetail>?
)

data class JsonPhraseDetail(
    @SerializedName("pContent")
    val pContent: String?,
    @SerializedName("pCn")
    val pCn: String?
)

data class JsonRelWord(
    @SerializedName("rels")
    val rels: List<JsonRelDetail>?
)

data class JsonRelDetail(
    @SerializedName("pos")
    val pos: String?,
    @SerializedName("words")
    val words: List<JsonRelWordDetail>?
)

data class JsonRelWordDetail(
    @SerializedName("hwd")
    val hwd: String?,
    @SerializedName("tran")
    val tran: String?
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

                // 转换同义词、短语、同根词为JSON字符串
                val synosJson = gson.toJson(detailContent.synos?.synos)
                val phrasesJson = gson.toJson(detailContent.phrases?.phrases)
                val relWordJson = gson.toJson(detailContent.relWord?.rels)

                Word(
                    english = jsonWord.headWord,
                    chinese = chineseTranslations,
                    pronunciation = pronunciation,
                    definition = englishDefinitions,
                    example = examples,
                    exampleTranslation = exampleTranslations,
                    synos = synosJson,
                    phrases = phrasesJson,
                    relWord = relWordJson,
                    category = "vocabulary"
                )
            }
        } catch (e: IOException) {
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}