package com.lorapok.keyboard

/**
 * Lorapok Keuboard — Prediction Engine
 *
 * Provides next-word predictions using bigram frequency tables.
 * Falls back to most common Bengali words when no context is available.
 *
 * Future: Will integrate TFLite Bi-LSTM model for neural predictions.
 */
class PredictionEngine {

    private val bigramMap = mutableMapOf<String, MutableList<String>>()

    init {
        // Expanded Bengali bigram predictions
        val defaults = mapOf(
            "আমি" to listOf("ভালো", "যাব", "চাই", "করি", "আছি", "ভাত", "পড়ি"),
            "তুমি" to listOf("কেমন", "যাও", "এসো", "করো", "আছো", "কি", "কোথায়"),
            "আপনি" to listOf("কি", "কেমন", "আসুন", "করুন", "আছেন", "কোথায়"),
            "সে" to listOf("যায়", "আসে", "করে", "বলে", "থাকে", "কোথায়"),
            "আমরা" to listOf("সবাই", "যাব", "করব", "চাই", "আছি", "ভাত"),
            "ভালো" to listOf("আছি", "লাগছে", "করো", "হয়েছে", "থাকো", "বাসি"),
            "কেমন" to listOf("আছো", "আছেন", "হয়েছে", "লাগছে", "চলছে", "আছেন"),
            "আজ" to listOf("আমি", "আমরা", "সকালে", "রাতে", "কি", "ছুটি"),
            "কাল" to listOf("আমি", "যাব", "আসব", "দেখা", "হবে", "সকালে"),
            "ধন্যবাদ" to listOf("আপনাকে", "ভাই", "অনেক", "সবাইকে", "তোমাকে", "দিদি"),
            "কি" to listOf("করছো", "হয়েছে", "চাও", "বলবে", "হলো", "খবর"),
            "এই" to listOf("যে", "তো", "দেখো", "রকম", "ভাবে", "বার"),
            "বাংলাদেশ" to listOf("আমার", "দেশ", "জিন্দাবাদ", "ক্রিকেট", "আর্মি"),
            "ঢাকা" to listOf("শহর", "বিশ্ববিদ্যালয়", "মেট্রোরেল", "জ্যাম"),
            "শুভ" to listOf("সকাল", "রাত্রি", "জন্মদিন", "নববর্ষ", "বিবাহ"),
            "আমার" to listOf("নাম", "দেশ", "বাড়ি", "ভাই", "মা", "বাবা")
        )
        for ((key, values) in defaults) {
            bigramMap[key] = values.toMutableList()
        }
    }

    /**
     * Predict next words based on the current word context.
     */
    fun predict(currentWord: String): List<String> {
        return bigramMap[currentWord]?.take(5) ?: getCommonSuggestions()
    }

    /**
     * Get default common Bengali word suggestions.
     */
    fun getCommonSuggestions(): List<String> {
        return listOf("আমি", "তুমি", "আপনি", "ভালো", "ধন্যবাদ")
    }

    /**
     * Learn a new bigram from user typing patterns.
     */
    fun learn(word1: String, word2: String) {
        val list = bigramMap.getOrPut(word1) { mutableListOf() }
        list.remove(word2) // remove if exists to re-add at front
        list.add(0, word2)
        if (list.size > 10) list.removeAt(list.size - 1)
    }
}
