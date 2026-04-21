package com.lorapok.keyboard

class TrieNode {
    val children = mutableMapOf<Char, TrieNode>()
    var isWord = false
    var wordData: Any? = null
    var freq = 0
    var maxFreq = 0
}

/**
 * Prefix Tree for high-performance auto-complete, dictionary lookups, and fuzzy matching.
 */
class Trie {
    private val root = TrieNode()

    fun insert(word: String, data: Any, freq: Int) {
        var node = root
        for (char in word) {
            node = node.children.getOrPut(char) { TrieNode() }
            if (freq > node.maxFreq) node.maxFreq = freq
        }
        node.isWord = true
        node.wordData = data
        node.freq = freq
    }

    /**
     * Returns exact match data or null.
     */
    fun search(word: String): Any? {
        var node = root
        for (char in word) {
            node = node.children[char] ?: return null
        }
        return if (node.isWord) node.wordData else null
    }

    /**
     * Returns up to [limit] matches starting with [prefix], sorted by frequency (highest first).
     */
    fun autocomplete(prefix: String, limit: Int = 5): List<Pair<String, Any>> {
        var node = root
        for (char in prefix) {
            node = node.children[char] ?: return emptyList()
        }

        val results = mutableListOf<Triple<String, Any, Int>>()
        collectWords(node, prefix, results)

        return results
            .sortedByDescending { it.third }
            .take(limit)
            .map { it.first to it.second }
    }

    /**
     * Fuzzy search: returns words within [maxDistance] Levenshtein edit distance of [target].
     * Used for autocorrect — finds close matches when exact match fails.
     */
    fun fuzzySearch(target: String, maxDistance: Int = 1, limit: Int = 3): List<Pair<String, Any>> {
        val results = mutableListOf<Triple<String, Any, Int>>()
        fuzzyDFS(root, "", target, maxDistance, results)

        return results
            .sortedByDescending { it.third }
            .take(limit)
            .map { it.first to it.second }
    }

    private fun fuzzyDFS(
        node: TrieNode,
        currentWord: String,
        target: String,
        maxDistance: Int,
        results: MutableList<Triple<String, Any, Int>>
    ) {
        if (node.isWord) {
            val dist = levenshtein(currentWord, target)
            if (dist in 1..maxDistance) {
                results.add(Triple(currentWord, node.wordData!!, node.freq))
            }
        }

        // Prune: if current word is already much longer than target + maxDistance, stop
        if (currentWord.length > target.length + maxDistance) return

        for ((char, childNode) in node.children) {
            fuzzyDFS(childNode, currentWord + char, target, maxDistance, results)
        }
    }

    private fun collectWords(node: TrieNode, currentPrefix: String, results: MutableList<Triple<String, Any, Int>>) {
        if (node.isWord) {
            results.add(Triple(currentPrefix, node.wordData!!, node.freq))
        }
        // Traverse children sorted by maxFreq for better early results
        for ((char, childNode) in node.children.entries.sortedByDescending { it.value.maxFreq }) {
            collectWords(childNode, currentPrefix + char, results)
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
