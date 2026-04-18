package com.lorapok.keyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.*
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.os.VibrationEffect
import android.os.Vibrator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.io.InputStreamReader

/**
 * Lorapok Keuboard — Bengali AI Keyboard Service
 * 
 * Main InputMethodService implementing:
 * - Phonetic conversion (Avro-style: ami → আমি)
 * - AI-powered word prediction
 * - User learning system
 * - Language switching (Bengali ↔ English)
 * - Gesture support & haptic feedback
 * 
 * Min SDK: API 21 (Android 5.0)
 * Memory target: <50 MB
 */
class BengaliKeyboardService : InputMethodService() {

    // ── Engines ──
    private lateinit var phoneticEngine: PhoneticEngine
    private lateinit var predictionEngine: PredictionEngine
    private lateinit var userLearning: UserLearningSystem

    // ── State ──
    private var isBengaliMode = true
    private var isShiftActive = false
    private var phoneticBuffer = StringBuilder()
    private var currentSuggestions = mutableListOf<String>()

    // ── UI ──
    private lateinit var keyboardLayout: LinearLayout
    private lateinit var suggestionBar: LinearLayout
    private lateinit var vibrator: Vibrator

    // ── Layout ──
    private val qwertyRows = listOf(
        listOf("q","w","e","r","t","y","u","i","o","p"),
        listOf("a","s","d","f","g","h","j","k","l"),
        listOf("z","x","c","v","b","n","m")
    )

    // ══════════════════════════════════════════════
    // Lifecycle
    // ══════════════════════════════════════════════

    override fun onCreate() {
        super.onCreate()
        phoneticEngine = PhoneticEngine(this)
        predictionEngine = PredictionEngine(this)
        userLearning = UserLearningSystem(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onCreateInputView(): View {
        keyboardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F4F4F4"))
            setPadding(4, 4, 4, 8)
        }
        buildKeyboardUI()
        return keyboardLayout
    }

    override fun onStartInput(info: EditorInfo?, restarting: Boolean) {
        super.onStartInput(info, restarting)
        phoneticBuffer.clear()
        currentSuggestions.clear()
    }

    // ══════════════════════════════════════════════
    // UI Builder
    // ══════════════════════════════════════════════

    private fun buildKeyboardUI() {
        keyboardLayout.removeAllViews()

        // Suggestion bar
        suggestionBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 8, 8, 8)
            setBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(44)
            )
        }
        keyboardLayout.addView(suggestionBar)
        updateSuggestions(listOf("আমি", "তুমি", "আপনি"))

        // QWERTY rows
        for ((rowIndex, row) in qwertyRows.withIndex()) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                setPadding(2, 2, 2, 2)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Shift key on row 3
            if (rowIndex == 2) {
                rowLayout.addView(createSpecialKey("⇧", 1.3f) { toggleShift() })
            }

            for (key in row) {
                rowLayout.addView(createKey(key, 1f))
            }

            // Backspace on row 1
            if (rowIndex == 0) {
                rowLayout.addView(createSpecialKey("⌫", 1.3f) { handleBackspace() })
            }
            // Enter on row 2
            if (rowIndex == 1) {
                rowLayout.addView(createSpecialKey("↵", 1.5f) { handleEnter() })
            }

            keyboardLayout.addView(rowLayout)
        }

        // Bottom row: 123, 🌐, space, 😊, ✨
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(2, 4, 2, 4)
        }
        bottomRow.addView(createSpecialKey("123", 1.2f) { /* TODO: number layout */ })
        bottomRow.addView(createSpecialKey("🌐", 1f) { toggleLanguage() })
        bottomRow.addView(createSpaceKey(4f))
        bottomRow.addView(createSpecialKey("😊", 1f) { /* TODO: emoji */ })
        bottomRow.addView(createSpecialKey("✨", 1f) { /* TODO: tone rewriter */ })
        keyboardLayout.addView(bottomRow)
    }

    private fun createKey(label: String, weight: Float): Button {
        return Button(this).apply {
            text = if (isShiftActive) label.uppercase() else label
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTextColor(Color.parseColor("#1F1F1F"))
            isAllCaps = false
            background = createKeyBackground("#FFFFFF")
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(48), weight).apply {
                setMargins(2, 2, 2, 2)
            }
            setOnClickListener {
                hapticFeedback()
                handleKeyPress(label)
            }
        }
    }

    private fun createSpecialKey(label: String, weight: Float, action: () -> Unit): Button {
        return Button(this).apply {
            text = label
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(Color.WHITE)
            isAllCaps = false
            background = createKeyBackground("#5F6368")
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(48), weight).apply {
                setMargins(2, 2, 2, 2)
            }
            setOnClickListener {
                hapticFeedback()
                action()
            }
        }
    }

    private fun createSpaceKey(weight: Float): Button {
        return Button(this).apply {
            text = if (isBengaliMode) "বাংলা" else "English"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(Color.parseColor("#1F1F1F"))
            isAllCaps = false
            background = createKeyBackground("#FFFFFF")
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(48), weight).apply {
                setMargins(2, 2, 2, 2)
            }
            setOnClickListener {
                hapticFeedback()
                handleSpace()
            }
        }
    }

    private fun createKeyBackground(color: String): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor(color))
            cornerRadius = dpToPx(6).toFloat()
            setStroke(1, Color.parseColor("#E0E0E0"))
        }
    }

    // ══════════════════════════════════════════════
    // Input Handling
    // ══════════════════════════════════════════════

    private fun handleKeyPress(key: String) {
        val ic = currentInputConnection ?: return
        val char = if (isShiftActive) key.uppercase() else key

        if (isBengaliMode) {
            phoneticBuffer.append(char.lowercase())
            val converted = phoneticEngine.convert(phoneticBuffer.toString())
            if (converted != null) {
                // Delete the buffer characters already committed
                val deleteCount = phoneticBuffer.length - 1
                if (deleteCount > 0) {
                    ic.deleteSurroundingText(deleteCount, 0)
                }
                ic.commitText(converted, 1)
                
                // Update predictions
                val predictions = predictionEngine.predict(converted)
                updateSuggestions(predictions)

                // Check if this completes a word or if we should keep buffering
                if (phoneticEngine.isComplete(phoneticBuffer.toString())) {
                    phoneticBuffer.clear()
                }
            } else {
                ic.commitText(char, 1)
            }
        } else {
            ic.commitText(char, 1)
            phoneticBuffer.clear()
        }

        if (isShiftActive) {
            isShiftActive = false
        }
    }

    private fun handleSpace() {
        val ic = currentInputConnection ?: return
        if (isBengaliMode && phoneticBuffer.isNotEmpty()) {
            val converted = phoneticEngine.convert(phoneticBuffer.toString())
            if (converted != null) {
                val deleteCount = phoneticBuffer.length
                if (deleteCount > 0) {
                    ic.deleteSurroundingText(deleteCount, 0)
                }
                ic.commitText(converted, 1)
                userLearning.recordWord(converted)
            }
            phoneticBuffer.clear()
        }
        ic.commitText(" ", 1)
        updateSuggestions(predictionEngine.getCommonSuggestions())
    }

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return
        if (phoneticBuffer.isNotEmpty()) {
            phoneticBuffer.deleteCharAt(phoneticBuffer.length - 1)
        }
        ic.deleteSurroundingText(1, 0)
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        phoneticBuffer.clear()
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    private fun toggleLanguage() {
        isBengaliMode = !isBengaliMode
        phoneticBuffer.clear()
        buildKeyboardUI()
    }

    private fun toggleShift() {
        isShiftActive = !isShiftActive
        buildKeyboardUI()
    }

    // ══════════════════════════════════════════════
    // Suggestions
    // ══════════════════════════════════════════════

    private fun updateSuggestions(suggestions: List<String>) {
        suggestionBar.removeAllViews()
        currentSuggestions = suggestions.toMutableList()

        for (suggestion in suggestions.take(5)) {
            val btn = TextView(this).apply {
                text = suggestion
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextColor(Color.parseColor("#1A73E8"))
                gravity = Gravity.CENTER
                setPadding(dpToPx(12), dpToPx(4), dpToPx(12), dpToPx(4))
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
                )
                setOnClickListener {
                    hapticFeedback()
                    applySuggestion(suggestion)
                }
            }
            suggestionBar.addView(btn)
        }
    }

    private fun applySuggestion(word: String) {
        val ic = currentInputConnection ?: return
        // Delete current partial input
        if (phoneticBuffer.isNotEmpty()) {
            ic.deleteSurroundingText(phoneticBuffer.length, 0)
            phoneticBuffer.clear()
        }
        ic.commitText("$word ", 1)
        userLearning.recordWord(word)
        updateSuggestions(predictionEngine.predict(word))
    }

    // ══════════════════════════════════════════════
    // Utilities
    // ══════════════════════════════════════════════

    private fun hapticFeedback() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
        ).toInt()
    }
}


// ══════════════════════════════════════════════════
// Phonetic Engine
// ══════════════════════════════════════════════════

class PhoneticEngine(private val context: Context) {

    private val rules: Map<String, String>
    private val commonWords: Map<String, String>
    private val consonants: Map<String, String>
    private val vowels: Map<String, String>
    private val conjuncts: Map<String, String>

    init {
        val json = loadJsonFromAssets("avro_phonetic_rules.json")
        vowels = parseSection(json, "vowels")
        consonants = parseSection(json, "consonants")
        conjuncts = parseSection(json, "conjuncts")
        commonWords = parseSection(json, "commonWords")

        // Merge all rules, sorted by key length descending (longest match first)
        val allRules = mutableMapOf<String, String>()
        allRules.putAll(conjuncts)
        allRules.putAll(consonants)
        allRules.putAll(vowels)
        rules = allRules.toSortedMap(compareByDescending { it.length })
    }

    fun convert(input: String): String? {
        // Check common words first
        commonWords[input.lowercase()]?.let { return it }

        // Longest-match-first algorithm
        var result = StringBuilder()
        var i = 0
        val text = input.lowercase()
        var matched = false

        while (i < text.length) {
            var found = false
            // Try longest match first (max key length ~5)
            for (len in minOf(5, text.length - i) downTo 1) {
                val substr = text.substring(i, i + len)
                rules[substr]?.let {
                    result.append(it)
                    i += len
                    found = true
                    matched = true
                    return@let
                }
                if (found) break
            }
            if (!found) {
                result.append(text[i])
                i++
            }
        }

        return if (matched) result.toString() else null
    }

    fun isComplete(buffer: String): Boolean {
        // Check if the buffer cannot produce a longer match
        val lower = buffer.lowercase()
        if (commonWords.containsKey(lower)) return true
        // Check if any rule starts with this buffer
        return !rules.keys.any { it.startsWith(lower) && it.length > lower.length }
    }

    private fun loadJsonFromAssets(filename: String): JSONObject {
        return try {
            val stream = context.assets.open(filename)
            val reader = InputStreamReader(stream, Charsets.UTF_8)
            val content = reader.readText()
            reader.close()
            JSONObject(content)
        } catch (e: Exception) {
            JSONObject()
        }
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
        } catch (e: Exception) { /* section not found */ }
        return map
    }
}


// ══════════════════════════════════════════════════
// Prediction Engine
// ══════════════════════════════════════════════════

class PredictionEngine(private val context: Context) {

    // Frequency-based predictions (works without ML model)
    private val wordFrequency = mutableMapOf<String, Int>()
    private val bigramMap = mutableMapOf<String, MutableList<String>>()

    // Common Bengali word pairs for initial predictions
    private val defaultBigrams = mapOf(
        "আমি" to listOf("ভালো", "যাব", "চাই", "করি", "আছি"),
        "তুমি" to listOf("কেমন", "যাও", "এসো", "করো", "আছো"),
        "আপনি" to listOf("কি", "কেমন", "আসুন", "করুন", "আছেন"),
        "সে" to listOf("যায়", "আসে", "করে", "বলে", "থাকে"),
        "আমরা" to listOf("সবাই", "যাব", "করব", "চাই", "আছি"),
        "ভালো" to listOf("আছি", "লাগছে", "করো", "হয়েছে", "থাকো"),
        "কেমন" to listOf("আছো", "আছেন", "হয়েছে", "লাগছে", "চলছে"),
        "আজ" to listOf("আমি", "আমরা", "সকালে", "রাতে", "কি"),
        "কাল" to listOf("আমি", "যাব", "আসব", "দেখা", "হবে"),
        "ধন্যবাদ" to listOf("আপনাকে", "ভাই", "অনেক", "সবাইকে", "তোমাকে")
    )

    init {
        // Initialize with default bigrams
        bigramMap.putAll(defaultBigrams.mapValues { it.value.toMutableList() })
    }

    fun predict(currentWord: String): List<String> {
        // Check bigram predictions
        bigramMap[currentWord]?.let {
            return it.take(5)
        }
        // Fall back to frequency-based
        return getCommonSuggestions()
    }

    fun getCommonSuggestions(): List<String> {
        if (wordFrequency.isNotEmpty()) {
            return wordFrequency.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { it.key }
        }
        return listOf("আমি", "তুমি", "আপনি", "ভালো", "ধন্যবাদ")
    }

    fun learn(word1: String, word2: String) {
        bigramMap.getOrPut(word1) { mutableListOf() }.let { list ->
            if (word2 !in list) list.add(0, word2)
            if (list.size > 10) list.removeAt(list.size - 1)
        }
    }
}


// ══════════════════════════════════════════════════
// User Learning System
// ══════════════════════════════════════════════════

class UserLearningSystem(private val context: Context) {

    private val wordCounts = mutableMapOf<String, Int>()
    private var lastWord: String? = null
    private val prefs by lazy {
        context.getSharedPreferences("lorapok_learning", Context.MODE_PRIVATE)
    }

    init {
        loadData()
    }

    fun recordWord(word: String) {
        wordCounts[word] = (wordCounts[word] ?: 0) + 1
        lastWord = word
        // Save periodically (every 10 words)
        if (wordCounts.values.sum() % 10 == 0) saveData()
    }

    fun getTopWords(count: Int = 20): List<String> {
        return wordCounts.entries
            .sortedByDescending { it.value }
            .take(count)
            .map { it.key }
    }

    private fun saveData() {
        val json = Gson().toJson(wordCounts)
        prefs.edit().putString("word_counts", json).apply()
    }

    private fun loadData() {
        val json = prefs.getString("word_counts", null) ?: return
        try {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            val loaded: Map<String, Int> = Gson().fromJson(json, type)
            wordCounts.putAll(loaded)
        } catch (e: Exception) { /* ignore corrupt data */ }
    }
}
