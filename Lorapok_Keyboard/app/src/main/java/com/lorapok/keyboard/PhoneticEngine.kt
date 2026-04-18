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

    private val commonWords: Map<String, String>
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
                if (bengaliKeys.hasNext()) {
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
        val candidates = mutableSetOf<String>()

        // 1. Lexicon Dictionary match (highest priority, using Trie)
        val dictMatches = dictionaryTrie.autocomplete(lower, 3)
        // If the exact typed string matches a prefix perfectly, we add those bengali words
        // Wait, for 'convert', we only want exact matches of the buffer if possible, or prefix matches.
        // Gboard shows exactly matched conversions first.
        val exactMatch = dictionaryTrie.search(lower)
        if (exactMatch != null) {
            candidates.add(exactMatch as String)
        }

        // 2. Generate transliterations
        // We generate two paths: one strict Avro (a=""), one Gboard style (a="া")
        val gboardStyle = generateTransliteration(lower, treatSingleAAsAa = true)
        if (gboardStyle.isNotEmpty()) candidates.add(gboardStyle)

        val strictAvro = generateTransliteration(lower, treatSingleAAsAa = false)
        if (strictAvro.isNotEmpty()) candidates.add(strictAvro)

        // 3. Add autocomplete prefix predictions if typing is incomplete
        for (match in dictMatches) {
            candidates.add(match.second as String)
        }

        return candidates.toList()
    }


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
