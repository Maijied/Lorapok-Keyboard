package com.lorapok.keyboard

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.widget.GridLayout

/**
 * Lorapok Keuboard — Gboard-style AI Keyboard
 *
 * Professional Slate/Indigo design with:
 * - Refined Phonetic Engine (Gboard behavior)
 * - Number/Symbol Layout
 * - Emoji Picker
 * - Language Switching
 */
class LorapokKeyboardService : InputMethodService() {

    enum class KeyboardState { QWERTY, SYMBOLS, EMOJIS }

    private lateinit var phoneticEngine: PhoneticEngine
    private lateinit var predictionEngine: PredictionEngine
    private lateinit var userLearning: UserLearningSystem
    private lateinit var toneRewriter: ToneRewriter

    private var currentState = KeyboardState.QWERTY
    private var isBengaliMode = true
    private var isShiftActive = false
    private var phoneticBuffer = StringBuilder()

    private lateinit var rootLayout: LinearLayout
    private lateinit var suggestionBar: LinearLayout
    private var vibrator: Vibrator? = null

    // ── User Preferences ──
    private var hapticEnabled = true
    private var soundEnabled = true
    private var autocorrectEnabled = true

    // ── Professional Color Palette ──
    private val colorKeyboardBg = Color.parseColor("#121212")
    private val colorKeyBg = Color.parseColor("#1E1E1E")
    private val colorKeyActive = Color.parseColor("#333333")
    private val colorSpecialKeyBg = Color.parseColor("#2C2C2C")
    private val colorAccent = Color.parseColor("#6366F1")
    private val colorTextPrimary = Color.parseColor("#F8FAFC")
    private val colorTextSecondary = Color.parseColor("#94A3B8")
    private val colorKeyBorder = Color.parseColor("#334155")

    private val qwertyRows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("z", "x", "c", "v", "b", "n", "m")
    )

    private val symbolRows = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/"),
        listOf("*", "\"", "'", ":", ";", "!", "?", ",", ".")
    )

    private val emojiList = listOf(
        "😊", "😂", "🥰", "😍", "😒", "😭", "😘", "🙌", "👍", "🙏",
        "🔥", "✨", "❤️", "🤣", "🤔", "🙄", "💖", "💙", "🌸", "🎉",
        "🎂", "☕", "🍕", "🍔", "⚽", "🚗", "🏠", "💻", "📱", "🇧🇩"
    )

    override fun onCreate() {
        super.onCreate()
        loadPreferences()
        phoneticEngine = PhoneticEngine(this).apply {
            autocorrectEnabled = this@LorapokKeyboardService.autocorrectEnabled
        }
        predictionEngine = PredictionEngine(this)
        englishPredictionEngine = EnglishPredictionEngine(this)
        userLearning = UserLearningSystem(this)
        toneRewriter = ToneRewriter(this).apply {
            setOnAcceptListener { rewritten ->
                currentInputConnection?.commitText(rewritten, 1)
            }
        }
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun loadPreferences() {
        val prefs = getSharedPreferences(KeyboardPrefs.PREFS_NAME, Context.MODE_PRIVATE)
        isBengaliMode = prefs.getBoolean(KeyboardPrefs.KEY_DEFAULT_BENGALI, true)
        hapticEnabled = prefs.getBoolean(KeyboardPrefs.KEY_HAPTIC_ENABLED, true)
        soundEnabled = prefs.getBoolean(KeyboardPrefs.KEY_SOUND_ENABLED, true)
        autocorrectEnabled = prefs.getBoolean(KeyboardPrefs.KEY_AUTOCORRECT_ENABLED, true)
    }

    override fun onCreateInputView(): View {
        rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(colorKeyboardBg)
            setPadding(dp(4), dp(6), dp(4), dp(48)) // Increased bottom padding to avoid nav bar overlap
        }
        buildKeyboard()
        return rootLayout
    }

    override fun onStartInput(info: EditorInfo?, restarting: Boolean) {
        super.onStartInput(info, restarting)
        loadPreferences()
        phoneticEngine.autocorrectEnabled = autocorrectEnabled
        phoneticBuffer.clear()
        currentState = KeyboardState.QWERTY
        showDefaultSuggestions()
    }

    private fun buildKeyboard() {
        rootLayout.removeAllViews()

        // ── Suggestion bar ──
        suggestionBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), 0, dp(8), 0)
            background = makeRoundRect(colorKeyBg, dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(42)
            ).apply { bottomMargin = dp(6) }
        }
        rootLayout.addView(suggestionBar)
        updateSuggestions(predictionEngine.getCommonSuggestions())

        when (currentState) {
            KeyboardState.QWERTY -> buildQwertyLayout()
            KeyboardState.SYMBOLS -> buildSymbolsLayout()
            KeyboardState.EMOJIS -> buildEmojiLayout()
        }
    }

    private fun buildQwertyLayout() {
        // Row 1: 10 char keys — full width, each weight 1.0 (total = 10)
        val row1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(4) }
        }
        for (key in qwertyRows[0]) {
            row1.addView(makeCharKey(key, 1f))
        }
        rootLayout.addView(row1)

        // Row 2: 9 char keys centered — spacer(0.5) + 9 keys(1.0 each) + spacer(0.5) = total 10
        val row2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(4) }
        }
        row2.addView(makeSpacer(0.5f))
        for (key in qwertyRows[1]) {
            row2.addView(makeCharKey(key, 1f))
        }
        row2.addView(makeSpacer(0.5f))
        rootLayout.addView(row2)

        // Row 3: ⇧(1.5) + 7 char keys(1.0 each) + ⌫(1.5) = total 10
        val row3 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(4) }
        }
        row3.addView(makeSpecialKey("⇧", 1.5f) { toggleShift() })
        for (key in qwertyRows[2]) {
            row3.addView(makeCharKey(key, 1f))
        }
        row3.addView(makeSpecialKey("⌫", 1.5f) { handleBackspace() })
        rootLayout.addView(row3)

        buildBottomRow()
    }

    private fun buildSymbolsLayout() {
        for ((rowIdx, row) in symbolRows.withIndex()) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(4) }
            }

            for (key in row) {
                // Use Bengali numbers in Bengali mode
                val displayKey = if (isBengaliMode) convertToBengaliNumber(key) else key
                rowLayout.addView(makeCharKey(displayKey, 1f))
            }

            // Row 3 has 9 chars + ⌫ = 10 total (same as other rows)
            if (rowIdx == 2) {
                rowLayout.addView(makeSpecialKey("⌫", 1f) { handleBackspace() })
            }
            rootLayout.addView(rowLayout)
        }
        buildBottomRow()
    }

    private fun buildEmojiLayout() {
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(160)
            )
        }
        val grid = GridLayout(this).apply {
            columnCount = 6
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        for (emoji in emojiList) {
            val btn = Button(this).apply {
                text = emoji
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                background = null
                layoutParams = GridLayout.LayoutParams().apply {
                    width = dp(50)
                    height = dp(50)
                }
                setOnClickListener { feedback(); commitText(emoji) }
            }
            grid.addView(btn)
        }
        scrollView.addView(grid)
        rootLayout.addView(scrollView)
        buildBottomRow()
    }

    private fun buildBottomRow() {
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val symbolLabel = if (currentState == KeyboardState.SYMBOLS) "ABC" else "?123"
        bottomRow.addView(makeSpecialKey(symbolLabel, 1.2f) { 
            commitCurrentBuffer()
            currentState = if (currentState == KeyboardState.SYMBOLS) KeyboardState.QWERTY else KeyboardState.SYMBOLS
            buildKeyboard()
        })
        
        val emojiLabel = if (currentState == KeyboardState.EMOJIS) "ABC" else "😊"
        bottomRow.addView(makeSpecialKey(emojiLabel, 1f) { 
            commitCurrentBuffer()
            currentState = if (currentState == KeyboardState.EMOJIS) KeyboardState.QWERTY else KeyboardState.EMOJIS
            buildKeyboard()
        })
        
        bottomRow.addView(makeSpecialKey(",", 1f) { feedback(); commitText(",") })
        bottomRow.addView(makeSpaceKey(3f))
        
        val periodLabel = if (isBengaliMode) "।" else "."
        bottomRow.addView(makeSpecialKey(periodLabel, 1f) { feedback(); commitText(periodLabel) })
        bottomRow.addView(makeSpecialKey("↵", 1.2f) { feedback(); handleEnter() })
        bottomRow.addView(makeSpecialKey("✨", 0.8f) { handleToneRewrite() })
        bottomRow.addView(makeSpecialKey("⚙", 0.8f) { feedback(); openSettings() })
        
        rootLayout.addView(bottomRow)
    }

    private fun makeCharKey(label: String, weight: Float): Button {
        return Button(this).apply {
            text = if (isShiftActive && label.length == 1) label.uppercase() else label
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTextColor(colorTextPrimary)
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            isAllCaps = false
            background = makeKeyDrawable(colorKeyBg, colorKeyActive)
            stateListAnimator = null
            elevation = dp(2).toFloat()
            layoutParams = LinearLayout.LayoutParams(0, dp(58), weight).apply {
                setMargins(dp(3), dp(2), dp(3), dp(2))
            }
            setOnClickListener { feedback(); handleCharInput(label) }
        }
    }

    private fun makeSpecialKey(label: String, weight: Float, action: () -> Unit): Button {
        return Button(this).apply {
            text = label
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(colorTextPrimary)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            isAllCaps = false
            background = makeKeyDrawable(colorSpecialKeyBg, colorKeyActive)
            stateListAnimator = null
            layoutParams = LinearLayout.LayoutParams(0, dp(58), weight).apply {
                setMargins(dp(3), dp(2), dp(3), dp(2))
            }
            
            if (label == "⌫") {
                var isDeleting = false
                val handler = android.os.Handler(android.os.Looper.getMainLooper())
                val deleteRunnable = object : Runnable {
                    override fun run() {
                        if (isDeleting) {
                            action()
                            handler.postDelayed(this, 50)
                        }
                    }
                }
                setOnTouchListener { v, event ->
                    when (event.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            feedback()
                            action()
                            isDeleting = true
                            handler.postDelayed(deleteRunnable, 400)
                            v.isPressed = true
                            true
                        }
                        android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                            isDeleting = false
                            handler.removeCallbacks(deleteRunnable)
                            v.isPressed = false
                            true
                        }
                        else -> false
                    }
                }
            } else {
                setOnClickListener { feedback(); action() }
            }
        }
    }

    private fun makeSpaceKey(weight: Float): Button {
        return Button(this).apply {
            text = if (isBengaliMode) "🌐 বাংলা" else "🌐 English"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(colorTextSecondary)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            isAllCaps = false
            background = makeKeyDrawable(colorKeyBg, colorKeyActive)
            stateListAnimator = null
            elevation = dp(2).toFloat()
            layoutParams = LinearLayout.LayoutParams(0, dp(58), weight).apply {
                setMargins(dp(3), dp(2), dp(3), dp(2))
            }
            setOnClickListener { feedback(); handleSpace() }
            setOnLongClickListener { feedback(); toggleLanguage(); true }
        }
    }

    private fun makeKeyDrawable(normalColor: Int, pressedColor: Int): StateListDrawable {
        val normal = GradientDrawable().apply {
            setColor(normalColor)
            cornerRadius = dp(8).toFloat()
            setStroke(1, colorKeyBorder)
        }
        val pressed = GradientDrawable().apply {
            setColor(pressedColor)
            cornerRadius = dp(8).toFloat()
            setStroke(2, colorAccent)
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressed)
            addState(intArrayOf(), normal)
        }
    }

    private fun makeRoundRect(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }
    }

    private fun makeSpacer(weight: Float): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1, weight)
        }
    }

    private lateinit var englishPredictionEngine: EnglishPredictionEngine

    // ... replace input methods
    private fun handleCharInput(key: String) {
        val ic = currentInputConnection ?: return
        val char = if (isShiftActive) key.uppercase() else key.lowercase()
        val lowerChar = char.lowercase()

        if (currentState == KeyboardState.QWERTY) {
            phoneticBuffer.append(lowerChar)
            val bufferStr = phoneticBuffer.toString()

            if (isBengaliMode) {
                val candidates = phoneticEngine.convert(bufferStr)
                if (candidates.isNotEmpty()) {
                    val bestMatch = candidates.first()
                    ic.setComposingText(bestMatch, 1)
                    
                    val suggestions = mutableListOf<String>()
                    suggestions.addAll(candidates)
                    if (!suggestions.contains(bufferStr)) suggestions.add(bufferStr)
                    suggestions.addAll(predictionEngine.predict(bestMatch))
                    updateSuggestions(suggestions.distinct())
                } else {
                    ic.commitText(char, 1)
                    phoneticBuffer.clear()
                }
            } else {
                // English Mode: Autocomplete
                ic.setComposingText(bufferStr, 1)
                val suggestions = mutableListOf(bufferStr)
                suggestions.addAll(englishPredictionEngine.autocomplete(bufferStr))
                updateSuggestions(suggestions.distinct())
            }
        } else {
            ic.commitText(char, 1)
        }
        if (isShiftActive) { isShiftActive = false; buildKeyboard() }
    }

    private fun playClickSound() {
        if (!soundEnabled) return
        val am = getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
        am?.playSoundEffect(android.media.AudioManager.FX_KEYPRESS_STANDARD)
    }

    private fun handleSpace() {
        val ic = currentInputConnection ?: return
        if (phoneticBuffer.isNotEmpty()) {
            ic.finishComposingText()
            val bufferStr = phoneticBuffer.toString()
            
            if (isBengaliMode) {
                val candidates = phoneticEngine.convert(bufferStr)
                val finalWord = if (candidates.isNotEmpty()) candidates.first() else bufferStr
                userLearning.recordWord(finalWord)
            } else {
                // English: commit buffer directly (we don't auto-correct aggressively yet)
                val finalWord = bufferStr
                userLearning.recordWord(finalWord)
            }
            phoneticBuffer.clear()
        }
        ic.commitText(" ", 1)
        
        // Show next word predictions based on the last word
        val lastText = ic.getTextBeforeCursor(20, 0)?.toString()?.trim() ?: ""
        val lastWord = lastText.split(" ").lastOrNull() ?: ""
        
        if (isBengaliMode) {
            if (lastWord.isNotEmpty()) {
                updateSuggestions(predictionEngine.predict(lastWord))
            } else {
                showDefaultSuggestions()
            }
        } else {
            if (lastWord.isNotEmpty()) {
                val englishPredictions = englishPredictionEngine.predictNext(lastWord)
                if (englishPredictions.isNotEmpty()) {
                    updateSuggestions(englishPredictions)
                } else {
                    showDefaultSuggestions()
                }
            } else {
                showDefaultSuggestions()
            }
        }
    }

    private fun showDefaultSuggestions() {
        if (isBengaliMode) {
            updateSuggestions(predictionEngine.getCommonSuggestions())
        } else {
            updateSuggestions(englishPredictionEngine.getDefaultSuggestions())
        }
    }

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return
        if (phoneticBuffer.isNotEmpty()) {
            phoneticBuffer.deleteCharAt(phoneticBuffer.length - 1)
            if (phoneticBuffer.isEmpty()) {
                ic.setComposingText("", 1)
                ic.finishComposingText()
                showDefaultSuggestions()
            } else {
                val bufferStr = phoneticBuffer.toString()
                if (isBengaliMode) {
                    val candidates = phoneticEngine.convert(bufferStr)
                    val bestMatch = if (candidates.isNotEmpty()) candidates.first() else bufferStr
                    ic.setComposingText(bestMatch, 1)
                    
                    val suggestions = mutableListOf<String>()
                    suggestions.addAll(candidates)
                    if (!suggestions.contains(bufferStr)) suggestions.add(bufferStr)
                    updateSuggestions(suggestions.distinct())
                } else {
                    ic.setComposingText(bufferStr, 1)
                    val suggestions = mutableListOf(bufferStr)
                    suggestions.addAll(englishPredictionEngine.autocomplete(bufferStr))
                    updateSuggestions(suggestions.distinct())
                }
            }
        } else {
            ic.deleteSurroundingText(1, 0)
            showDefaultSuggestions()
        }
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        if (phoneticBuffer.isNotEmpty()) ic.finishComposingText()
        phoneticBuffer.clear()
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    private fun commitText(text: String) {
        if (phoneticBuffer.isNotEmpty()) currentInputConnection?.finishComposingText()
        phoneticBuffer.clear()
        currentInputConnection?.commitText(text, 1)
    }

    private fun updateSuggestions(suggestions: List<String>) {
        suggestionBar.removeAllViews()
        for (word in suggestions.take(5)) {
            val tv = TextView(this).apply {
                text = word
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextColor(colorAccent)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                gravity = Gravity.CENTER
                setPadding(dp(12), 0, dp(12), 0)
                layoutParams = LinearLayout.LayoutParams(0, -1, 1f)
                setOnClickListener { feedback(); applySuggestion(word) }
            }
            suggestionBar.addView(tv)
        }
    }

    private fun applySuggestion(word: String) {
        val ic = currentInputConnection ?: return
        if (phoneticBuffer.isNotEmpty()) {
            ic.finishComposingText()
            phoneticBuffer.clear()
        }
        ic.commitText("$word ", 1)
        userLearning.recordWord(word)
        
        if (isBengaliMode) {
            updateSuggestions(predictionEngine.predict(word))
        } else {
            val englishPredictions = englishPredictionEngine.predictNext(word)
            if (englishPredictions.isNotEmpty()) {
                updateSuggestions(englishPredictions)
            } else {
                showDefaultSuggestions()
            }
        }
    }

    private fun commitCurrentBuffer() {
        if (phoneticBuffer.isNotEmpty()) {
            currentInputConnection?.finishComposingText()
            val bufferStr = phoneticBuffer.toString()
            if (isBengaliMode) {
                val candidates = phoneticEngine.convert(bufferStr)
                val finalWord = if (candidates.isNotEmpty()) candidates.first() else bufferStr
                userLearning.recordWord(finalWord)
            } else {
                userLearning.recordWord(bufferStr)
            }
            phoneticBuffer.clear()
        }
    }

    private fun toggleLanguage() { 
        commitCurrentBuffer()
        isBengaliMode = !isBengaliMode
        buildKeyboard() 
        showDefaultSuggestions()
    }

    private fun convertToBengaliNumber(input: String): String {
        return input
            .replace('0', '০')
            .replace('1', '১')
            .replace('2', '২')
            .replace('3', '৩')
            .replace('4', '৪')
            .replace('5', '৫')
            .replace('6', '৬')
            .replace('7', '৭')
            .replace('8', '৮')
            .replace('9', '৯')
    }
    
    private fun toggleShift() { isShiftActive = !isShiftActive; buildKeyboard() }

    private fun feedback() {
        haptic()
        playClickSound()
    }

    private fun haptic() {
        if (!hapticEnabled) return
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
            } else { @Suppress("DEPRECATION") v.vibrate(12) }
        }
    }

    private fun playClickSound() {
        if (!soundEnabled) return
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
    }

    private fun dp(value: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics
    ).toInt()
}
