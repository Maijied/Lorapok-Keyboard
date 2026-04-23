package com.lorapok.keyboard

class TrieNode {
    val children = mutableMapOf<Char, TrieNode>()
    var isWord = false
    var wordDataMap: MutableMap<String, Int> = mutableMapOf() // BengaliWord -> Frequency
    var maxFreq = 0
}

/**
 * Prefix Tree for high-performance auto-complete, dictionary lookups, and fuzzy matching.
 */
class Trie {
    private val root = TrieNode()

    fun insert(latinKey: String, bengaliWord: String, freq: Int) {
        var node = root
        for (char in latinKey) {
            node = node.children.getOrPut(char) { TrieNode() }
            if (freq > node.maxFreq) node.maxFreq = freq
        }
        node.isWord = true
        node.wordDataMap[bengaliWord] = freq
    }

    /**
     * Returns a list of candidates (BengaliWord to Freq) for a given latin key.
     */
    fun search(word: String): List<Pair<String, Int>> {
        var node = root
        for (char in word) {
            node = node.children[char] ?: return emptyList()
        }
        return if (node.isWord) {
            node.wordDataMap.toList().sortedByDescending { it.second }
        } else emptyList()
    }

    /**
     * Returns up to [limit] matches starting with [prefix], sorted by frequency (highest first).
     */
    fun autocomplete(prefix: String, limit: Int = 5): List<Pair<String, Int>> {
        var node = root
        for (char in prefix) {
            node = node.children[char] ?: return emptyList()
        }

        val results = mutableListOf<Pair<String, Int>>()
        collectWords(node, results)

        return results
            .sortedByDescending { it.second }
            .take(limit)
    }

    /**
     * Fuzzy search: returns words within [maxDistance] Levenshtein edit distance of [target].
     * Used for autocorrect — finds close matches when exact match fails.
     */
    fun fuzzySearch(target: String, maxDistance: Int = 1, limit: Int = 3): List<Pair<String, Int>> {
        val results = mutableListOf<Pair<String, Int>>()
        fuzzyDFS(root, "", target, maxDistance, results)

        return results
            .sortedByDescending { it.second }
            .take(limit)
    }

    private fun fuzzyDFS(
        node: TrieNode,
        currentWord: String,
        target: String,
        maxDistance: Int,
        results: MutableList<Pair<String, Int>>
    ) {
        if (node.isWord) {
            val dist = levenshtein(currentWord, target)
            if (dist in 1..maxDistance) {
                for ((word, freq) in node.wordDataMap) {
                    results.add(Pair(word, freq))
                }
            }
        }

        if (currentWord.length > target.length + maxDistance) return

        for ((char, childNode) in node.children) {
            fuzzyDFS(childNode, currentWord + char, target, maxDistance, results)
        }
    }

    private fun collectWords(node: TrieNode, results: MutableList<Pair<String, Int>>) {
        if (node.isWord) {
            for ((word, freq) in node.wordDataMap) {
                results.add(Pair(word, freq))
            }
        }
        for ((_, childNode) in node.children.entries.sortedByDescending { it.value.maxFreq }) {
            collectWords(childNode, results)
        }
    }

    companion object {
        /**
         * Compute Levenshtein edit distance between two strings.
         */
        fun levenshtein(a: String, b: String): Int {
            val m = a.length
            val n = b.length
            val dp = Array(m + 1) { IntArray(n + 1) }

            for (i in 0..m) dp[i][0] = i
            for (j in 0..n) dp[0][j] = j

            for (i in 1..m) {
                for (j in 1..n) {
                    val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                    dp[i][j] = minOf(
                        dp[i - 1][j] + 1,      // deletion
                        dp[i][j - 1] + 1,      // insertion
                        dp[i - 1][j - 1] + cost // substitution
                    )
                }
            }
            return dp[m][n]
        }
    }
}
