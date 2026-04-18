package com.lorapok.keyboard

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

/**
 * Lorapok Keuboard — User Learning System
 *
 * Tracks word usage frequency and adapts suggestions over time.
 * All data stored locally with no cloud transmission.
 */
class UserLearningSystem(context: Context) {

    private val wordCounts = mutableMapOf<String, Int>()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lorapok_learning", Context.MODE_PRIVATE)

    init {
        loadData()
    }

    fun recordWord(word: String) {
        wordCounts[word] = (wordCounts[word] ?: 0) + 1
        // Save every 10 words
        if (wordCounts.values.sum() % 10 == 0) saveData()
    }

    fun getTopWords(count: Int = 20): List<String> {
        return wordCounts.entries
            .sortedByDescending { it.value }
            .take(count)
            .map { it.key }
    }

    fun getWordCount(word: String): Int = wordCounts[word] ?: 0

    private fun saveData() {
        val json = JSONObject(wordCounts as Map<*, *>).toString()
        prefs.edit().putString("word_counts", json).apply()
    }

    private fun loadData() {
        val json = prefs.getString("word_counts", null) ?: return
        try {
            val obj = JSONObject(json)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                wordCounts[key] = obj.getInt(key)
            }
        } catch (_: Exception) { }
    }
}
