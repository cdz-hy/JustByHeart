package com.justbyheart.vocabulary.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.justbyheart.vocabulary.data.entity.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

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
    val syno: JsonSyno?,
    @SerializedName("phrase")
    val phrase: JsonPhrase?,
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

// 新增的数据类用于处理同义词
data class JsonSyno(
    @SerializedName("synos")
    val synos: List<JsonSynoItem>?
)

data class JsonSynoItem(
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

// 新增的数据类用于处理短语
data class JsonPhrase(
    @SerializedName("phrases")
    val phrases: List<JsonPhraseItem>?
)

data class JsonPhraseItem(
    @SerializedName("pContent")
    val pContent: String?,
    @SerializedName("pCn")
    val pCn: String?
)

// 新增的数据类用于处理同根词
data class JsonRelWord(
    @SerializedName("rels")
    val rels: List<JsonRelItem>?
)

data class JsonRelItem(
    @SerializedName("pos")
    val pos: String?,
    @SerializedName("words")
    val words: List<JsonRelWordItem>?
)

data class JsonRelWordItem(
    @SerializedName("hwd")
    val hwd: String?,
    @SerializedName("tran")
    val tran: String?
)

object WordDataLoader {
    
    // 词库映射关系
    private val wordBankMapping = mapOf(
        "CET6_1" to "六级核心",
        "CET6_2" to "六级核心",
        "CET6_3" to "六级核心",
        "CET6_true" to "六级真题词",
        "GRE_words" to "GRE",
        "IELTS_words" to "雅思核心",
        "KaoYan_bikao" to "考研必考",
        "KaoYan_yingyu" to "考研英语"
    )
    
    /**
     * 从assets加载指定词库的单词
     * @param context 上下文
     * @param wordBankFile 词库文件名（不包含.json后缀）
     * @return 单词列表
     */
    suspend fun loadWordsFromAssets(context: Context, wordBankFile: String): List<Word> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("$wordBankFile.json").bufferedReader().use { it.readText() }
            val gson = Gson()
            val listType = com.google.gson.reflect.TypeToken.getParameterized(List::class.java, JsonWord::class.java).type
            val jsonWords: List<JsonWord> = gson.fromJson(jsonString, listType)
            
            // 打乱顺序
            val shuffledWords = jsonWords.shuffled()
            
            // 转换为Word实体对象
            shuffledWords.map { jsonWord ->
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

                // 拼接同义词信息
                val synos = detailContent.syno?.synos?.joinToString("\n") { synoItem ->
                    val pos = synoItem.pos ?: ""
                    val tran = synoItem.tran ?: ""
                    val hwds = synoItem.hwds?.joinToString(", ") { it.w ?: "" } ?: ""
                    "$pos. $tran ($hwds)"
                } ?: ""

                // 拼接短语信息
                val phrases = detailContent.phrase?.phrases?.joinToString("\n") { phraseItem ->
                    val pContent = phraseItem.pContent ?: ""
                    val pCn = phraseItem.pCn ?: ""
                    "$pContent: $pCn"
                } ?: ""

                // 拼接同根词信息
                val relWords = detailContent.relWord?.rels?.joinToString("\n") { relItem ->
                    val pos = relItem.pos ?: ""
                    val words = relItem.words?.joinToString(", ") { wordItem ->
                        "${wordItem.hwd ?: ""} (${wordItem.tran ?: ""})"
                    } ?: ""
                    "$pos. $words"
                } ?: ""

                Word(
                    english = jsonWord.headWord,
                    chinese = chineseTranslations, // 将拼接后的中文翻译赋值给chinese字段
                    pronunciation = pronunciation,
                    definition = englishDefinitions, // 将拼接后的英文释义赋值给definition字段
                    example = examples,
                    exampleTranslation = exampleTranslations,
                    category = "vocabulary", // 默认分类
                    synos = synos.ifEmpty { null },
                    phrases = phrases.ifEmpty { null },
                    relWord = relWords.ifEmpty { null },
                    wordBank = wordBankMapping[wordBankFile] ?: wordBankFile // 设置词库来源
                )
            }
        } catch (e: IOException) {
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 获取所有可用的词库列表
     * @return 词库列表
     */
    fun getAvailableWordBanks(): List<String> {
        return wordBankMapping.values.distinct()
    }
    
    /**
     * 根据显示名称获取对应的文件名列表
     * @param displayName 显示名称
     * @return 文件名列表
     */
    fun getWordBankFileNames(displayName: String): List<String> {
        return wordBankMapping.filter { it.value == displayName }.keys.toList()
    }
}