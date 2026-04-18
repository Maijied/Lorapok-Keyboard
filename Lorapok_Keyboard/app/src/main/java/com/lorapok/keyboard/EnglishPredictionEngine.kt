package com.lorapok.keyboard

/**
 * Lorapok Keuboard — English Prediction Engine
 *
 * Provides professional auto-complete and next-word prediction for English.
 */
class EnglishPredictionEngine {

    private val commonWords = listOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "I",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
        "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
        "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
        "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
        "even", "new", "want", "because", "any", "these", "give", "day", "most", "us",
        "are", "is", "was", "were", "been", "has", "had", "doing", "does", "did",
        "hello", "hi", "thanks", "thank", "sorry", "yes", "please", "maybe", "always",
        "never", "today", "tomorrow", "yesterday", "morning", "night", "love", "great"
    )

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
        return commonWords
            .filter { it.startsWith(lowerPrefix) }
            .sortedBy { it.length }
            .take(5)
            .map { if (prefix.first().isUpperCase()) it.replaceFirstChar { c -> c.uppercase() } else it }
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
