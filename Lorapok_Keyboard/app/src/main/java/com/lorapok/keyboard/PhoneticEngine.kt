package com.lorapok.keyboard

import android.content.Context
import org.json.JSONObject
import java.io.InputStreamReader

/**
 * Lorapok Keuboard — Phonetic Conversion Engine (Improved)
 *
 * Converts Latin character sequences to Bengali using Avro-style phonetic rules.
 * Handles:
 * - Vowels at start vs Vowel signs (kar) after consonants
 * - Longest-match-first for conjuncts
 * - Common word dictionary
 */
class PhoneticEngine(context: Context) {

    private val vowels: Map<String, String>
    private val vowelSigns: Map<String, String>
    private val consonants: Map<String, String>
    private val conjuncts: List<Pair<String, String>>
    private val dictionaryTrie = Trie()

    init {
        val json = loadJsonFromAssets(context, "avro_phonetic_rules.json")
        vowels = parseSection(json, "vowels")
        vowelSigns = parseSection(json, "vowelSigns")
        consonants = parseSection(json, "consonants")
        
        val conjunctMap = parseSection(json, "conjuncts")
        conjuncts = conjunctMap.entries
            .sortedByDescending { it.key.length }
            .map { it.key to it.value }

        loadDictionary(context)
    }

    private fun loadDictionary(context: Context) {
        try {
            val stream = context.assets.open("bengali_dictionary.json")
            val reader = InputStreamReader(stream, Charsets.UTF_8)
            val content = reader.readText()
            reader.close()

            val json = JSONObject(content)
            val keys = json.keys()
            while (keys.hasNext()) {
                val latin = keys.next()
                val bengaliObj = json.getJSONObject(latin)
                val bengaliKeys = bengaliObj.keys()
                while (bengaliKeys.hasNext()) {
                    val bengaliWord = bengaliKeys.next()
                    val freq = bengaliObj.getInt(bengaliWord)
                    dictionaryTrie.insert(latin.lowercase(), bengaliWord, freq)
                }
            }
        } catch (e: Exception) {}
    }

    /**
     * Convert Latin input to Bengali, returning a list of candidates (Gboard style).
     */
    fun convert(input: String): List<String> {
        val lower = input.lowercase()
        val candidates = mutableListOf<String>()

        // 1. Lexicon Dictionary matches (highest priority)
        val dictMatches = dictionaryTrie.search(lower)
        for (match in dictMatches) {
            candidates.add(match.first)
        }

        // 2. Generate transliterations
        val gboardStyle = generateTransliteration(lower, treatSingleAAsAa = true)
        if (gboardStyle.isNotEmpty() && !candidates.contains(gboardStyle)) {
            candidates.add(gboardStyle)
        }

        val strictAvro = generateTransliteration(lower, treatSingleAAsAa = false)
        if (strictAvro.isNotEmpty() && !candidates.contains(strictAvro)) {
            candidates.add(strictAvro)
        }

        // 3. Autocomplete prefix predictions (if dictionary matches are few)
        if (candidates.size < 5) {
            val prefixMatches = dictionaryTrie.autocomplete(lower, 5)
            for (match in prefixMatches) {
                if (!candidates.contains(match.first)) {
                    candidates.add(match.first)
                }
            }
        }

        // 4. Fuzzy autocorrect: if no candidates yet, suggest close matches
        if (candidates.isEmpty() && autocorrectEnabled && lower.length >= 3) {
            val fuzzyMatches = dictionaryTrie.fuzzySearch(lower, maxDistance = 1, limit = 3)
            for (match in fuzzyMatches) {
                candidates.add(match.first)
            }
        }

        return candidates.distinct()
    }

    /** Toggle autocorrect on/off (controlled by settings) */
    var autocorrectEnabled = true


    private fun generateTransliteration(lower: String, treatSingleAAsAa: Boolean): String {
        val result = StringBuilder()
        var i = 0
        var lastWasConsonant = false

        while (i < lower.length) {
            var matched = false

            // Try conjuncts
            for ((key, value) in conjuncts) {
                if (lower.startsWith(key, i)) {
                    result.append(value)
                    i += key.length
                    lastWasConsonant = true
                    matched = true
                    break
                }
            }
            if (matched) continue

            // Try consonants
            val remaining = lower.substring(i)
            
            // Explicit Modifiers
            if (remaining.startsWith("^")) {
                result.append("ঁ")
                i += 1
                lastWasConsonant = false
                continue
            }
            if (remaining.startsWith(",,")) {
                result.append("্")
                i += 2
                lastWasConsonant = false
                continue
            }
            if (remaining.startsWith(":")) {
                result.append("ঃ")
                i += 1
                lastWasConsonant = false
                continue
            }

            val consonantMatch = consonants.keys.filter { remaining.startsWith(it) }
                .maxByOrNull { it.length }
            
            if (consonantMatch != null) {
                result.append(consonants[consonantMatch])
                i += consonantMatch.length
                lastWasConsonant = true
                continue
            }

            // Try vowels/signs
            val vowelMatch = vowels.keys.filter { remaining.startsWith(it) }
                .maxByOrNull { it.length }

            if (vowelMatch != null) {
                var value = ""
                if (lastWasConsonant) {
                    value = vowelSigns[vowelMatch] ?: ""
                    // Gboard hack: if user typed 'a', they often mean 'া'
                    if (treatSingleAAsAa && vowelMatch == "a") {
                        value = "া"
                    }
                } else {
                    value = vowels[vowelMatch] ?: ""
                    // Gboard hack: if user typed 'a' at start, they often mean 'আ'
                    if (treatSingleAAsAa && vowelMatch == "a" && i == 0) {
                        value = "আ"
                    }
                }
                result.append(value)
                i += vowelMatch.length
                lastWasConsonant = false
                continue
            }

            result.append(lower[i])
            lastWasConsonant = false
            i++
        }
        return result.toString()
    }

    fun isComplete(buffer: String): Boolean {
        val lower = buffer.lowercase()
        if (dictionaryTrie.search(lower) != null) return true
        
        val hasLongerRule = conjuncts.any { it.first.startsWith(lower) && it.first.length > lower.length } ||
                consonants.keys.any { it.startsWith(lower) && it.length > lower.length } ||
                vowels.keys.any { it.startsWith(lower) && it.length > lower.length }
        
        return !hasLongerRule
    }


    private fun loadJsonFromAssets(context: Context, filename: String): JSONObject {
        return try {
            val stream = context.assets.open(filename)
            val reader = InputStreamReader(stream, Charsets.UTF_8)
            val content = reader.readText()
            reader.close()
            JSONObject(content)
        } catch (e: Exception) { JSONObject() }
    }

    private fun parseSection(json: JSONObject, section: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val obj = json.getJSONObject(section)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.getString(key)
            }
        } catch (_: Exception) {}
        return map
    }
}
