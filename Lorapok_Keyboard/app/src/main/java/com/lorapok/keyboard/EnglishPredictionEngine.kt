package com.lorapok.keyboard

import android.content.Context
import org.json.JSONObject
import java.io.InputStreamReader

/**
 * Lorapok Keuboard — English Prediction Engine
 *
 * Uses data-driven dictionaries via a Trie for high-performance predictions.
 */
class EnglishPredictionEngine(context: Context) {

    private val dictionaryTrie = Trie()

    init {
        loadDictionary(context)
    }

    private fun loadDictionary(context: Context) {
        try {
            val stream = context.assets.open("english_dictionary.json")
            val reader = InputStreamReader(stream, Charsets.UTF_8)
            val content = reader.readText()
            reader.close()

            val json = JSONObject(content)
            val keys = json.keys()
            while (keys.hasNext()) {
                val word = keys.next()
                val freq = json.getInt(word)
                dictionaryTrie.insert(word.lowercase(), word, freq)
            }
        } catch (e: Exception) {
            // Fallback if asset is missing
            val defaults = listOf("the", "be", "to", "of", "and", "a", "in")
            defaults.forEachIndexed { i, w -> dictionaryTrie.insert(w, w, 100 - i) }
        }
    }


    private val bigramMap = mapOf(
        "I" to listOf("am", "will", "have", "think", "don't", "can"),
        "you" to listOf("are", "can", "will", "have", "should", "know"),
        "he" to listOf("is", "will", "has", "was", "can", "said"),
        "she" to listOf("is", "will", "has", "was", "can", "said"),
        "it" to listOf("is", "was", "will", "has", "can", "looks"),
        "we" to listOf("are", "can", "will", "have", "should", "need"),
        "they" to listOf("are", "will", "have", "were", "can", "said"),
        "this" to listOf("is", "was", "will", "has", "can", "means"),
        "that" to listOf("is", "was", "will", "has", "can", "means"),
        "there" to listOf("is", "are", "was", "were", "will", "has"),
        "how" to listOf("are", "is", "much", "many", "do", "can"),
        "what" to listOf("is", "are", "do", "can", "time", "about"),
        "where" to listOf("is", "are", "do", "can", "did", "have"),
        "when" to listOf("is", "are", "do", "can", "did", "have"),
        "why" to listOf("is", "are", "do", "can", "did", "have"),
        "who" to listOf("is", "are", "do", "can", "did", "has"),
        "can" to listOf("you", "I", "we", "it", "they", "be"),
        "will" to listOf("you", "I", "we", "it", "they", "be"),
        "do" to listOf("you", "I", "we", "it", "they", "not"),
        "are" to listOf("you", "we", "they", "there", "not"),
        "is" to listOf("it", "he", "she", "there", "not", "that"),
        "am" to listOf("I", "not", "sure", "sorry", "glad", "going"),
        "have" to listOf("you", "I", "we", "they", "to", "a"),
        "has" to listOf("it", "he", "she", "been", "to", "a"),
        "had" to listOf("to", "a", "been", "the", "an", "some"),
        "was" to listOf("it", "he", "she", "a", "the", "not"),
        "were" to listOf("you", "we", "they", "a", "the", "not"),
        "be" to listOf("a", "the", "able", "sure", "careful", "good"),
        "been" to listOf("a", "the", "able", "doing", "working", "trying"),
        "good" to listOf("morning", "night", "luck", "job", "idea", "time"),
        "thank" to listOf("you", "goodness", "God", "heavens", "very"),
        "thanks" to listOf("for", "a", "so", "again", "very", "much"),
        "very" to listOf("much", "good", "well", "nice", "happy", "bad"),
        "so" to listOf("much", "many", "good", "bad", "happy", "sad"),
        "much" to listOf("better", "worse", "more", "less", "time", "money"),
        "many" to listOf("thanks", "people", "times", "years", "things", "more"),
        "more" to listOf("than", "time", "money", "people", "things", "information"),
        "less" to listOf("than", "time", "money", "people", "things", "information"),
        "a" to listOf("lot", "good", "great", "little", "few", "bit"),
        "an" to listOf("idea", "example", "hour", "important", "interesting", "old"),
        "the" to listOf("same", "best", "first", "last", "next", "other"),
        "to" to listOf("be", "do", "have", "go", "get", "make"),
        "of" to listOf("the", "a", "an", "my", "your", "his"),
        "in" to listOf("the", "a", "an", "my", "your", "his"),
        "for" to listOf("the", "a", "an", "my", "your", "his"),
        "on" to listOf("the", "a", "an", "my", "your", "his"),
        "with" to listOf("the", "a", "an", "my", "your", "his"),
        "at" to listOf("the", "a", "an", "my", "your", "his"),
        "from" to listOf("the", "a", "an", "my", "your", "his"),
        "by" to listOf("the", "a", "an", "my", "your", "his"),
        "about" to listOf("the", "a", "an", "my", "your", "his"),
        "into" to listOf("the", "a", "an", "my", "your", "his"),
        "over" to listOf("the", "a", "an", "my", "your", "his"),
        "after" to listOf("the", "a", "an", "my", "your", "his")
    )

    fun autocomplete(prefix: String): List<String> {
        if (prefix.isEmpty()) return emptyList()
        val lowerPrefix = prefix.lowercase()
        val results = dictionaryTrie.autocomplete(lowerPrefix, 5)
        
        return results.map { 
            val word = it.second as String
            if (prefix.first().isUpperCase()) word.replaceFirstChar { c -> c.uppercase() } else word
        }
    }

    fun predictNext(currentWord: String): List<String> {
        val lower = currentWord.lowercase()
        val predictions = bigramMap[lower] ?: bigramMap[currentWord] ?: emptyList()
        return predictions.take(5)
    }

    fun getDefaultSuggestions(): List<String> {
        return listOf("I", "The", "You", "And", "It")
    }
}
