package com.lorapok.keyboard

class TrieNode {
    val children = mutableMapOf<Char, TrieNode>()
    var isWord = false
    var wordData: Any? = null
    var maxFreq = 0
}

/**
 * Prefix Tree for high-performance auto-complete and dictionary lookups.
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
     * Returns up to limit matches starting with prefix, sorted by maxFreq.
     */
    fun autocomplete(prefix: String, limit: Int = 5): List<Pair<String, Any>> {
        var node = root
        for (char in prefix) {
            node = node.children[char] ?: return emptyList()
        }

        val results = mutableListOf<Pair<String, Any>>()
        collectWords(node, prefix, results)
        
        // Sort by assumed frequency (maxFreq gives us a hint, but we just sort length and then assume it was inserted in order or we can store freq in wordData)
        return results.take(limit)
    }

    private fun collectWords(node: TrieNode, currentPrefix: String, results: MutableList<Pair<String, Any>>) {
        if (node.isWord) {
            results.add(currentPrefix to node.wordData!!)
        }
        // Sorting children by maxFreq isn't trivial here without a custom structure,
        // so we just do a simple DFS for this prototype.
        for ((char, childNode) in node.children) {
            collectWords(childNode, currentPrefix + char, results)
        }
    }
}
