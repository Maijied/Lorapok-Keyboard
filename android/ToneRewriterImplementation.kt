package com.lorapok.keyboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * Lorapok Keuboard — Tone Rewriter Implementation (Android)
 *
 * Provides 8 Bengali tone transformations:
 *   Free:    Formal, Friendly, Respectful, Concise, Detailed
 *   Premium: Poetic, Humorous, Empathetic
 *
 * Uses a hybrid approach:
 *   - Rule-based engine for pronoun/verb/formality transformations (<100ms)
 *   - Neural engine placeholder for complex tones (<500ms)
 */

// ══════════════════════════════════════════════════
// Data Models
// ══════════════════════════════════════════════════

enum class ToneType(
    val id: String,
    val bengaliName: String,
    val emoji: String,
    val isPremium: Boolean
) {
    FORMAL("formal", "আনুষ্ঠানিক", "🎩", false),
    FRIENDLY("friendly", "বন্ধুত্বপূর্ণ", "😊", false),
    RESPECTFUL("respectful", "শ্রদ্ধাশীল", "🙏", false),
    CONCISE("concise", "সংক্ষিপ্ত", "⚡", false),
    DETAILED("detailed", "বিস্তারিত", "📝", false),
    POETIC("poetic", "কাব্যিক", "📚", true),
    HUMOROUS("humorous", "হাস্যরসাত্মক", "😄", true),
    EMPATHETIC("empathetic", "সহানুভূতিশীল", "💝", true);

    val displayName get() = "$emoji $bengaliName"
}

data class ToneResult(
    val originalText: String,
    val rewrittenText: String,
    val tone: ToneType,
    val confidence: Float,
    val processingTimeMs: Long
)

// ══════════════════════════════════════════════════
// Bengali Tone Rules (Rule-Based Engine)
// ══════════════════════════════════════════════════

object BengaliToneRules {

    // Pronoun conversion tables
    private val pronounUpgrade = mapOf(
        "তুই" to "তুমি", "তুমি" to "আপনি", "তোর" to "তোমার",
        "তোমার" to "আপনার", "তোকে" to "তোমাকে", "তোমাকে" to "আপনাকে",
        "তোরা" to "তোমরা", "তোমরা" to "আপনারা"
    )

    private val pronounDowngrade = mapOf(
        "আপনি" to "তুমি", "আপনার" to "তোমার", "আপনাকে" to "তোমাকে",
        "আপনারা" to "তোমরা", "তুমি" to "তুই", "তোমার" to "তোর",
        "তোমাকে" to "তোকে", "তোমরা" to "তোরা"
    )

    // Verb formality mapping
    private val verbFormal = mapOf(
        "আসবে" to "উপস্থিত থাকবেন", "যাবে" to "যাবেন",
        "করবে" to "করবেন", "বলবে" to "বলবেন",
        "দেবে" to "দেবেন", "খাবে" to "খাবেন",
        "পারবে" to "পারবেন", "আসো" to "আসুন",
        "যাও" to "যান", "করো" to "করুন",
        "বলো" to "বলুন", "দাও" to "দিন",
        "খাও" to "খান", "বসো" to "বসুন",
        "আসবি" to "আসবেন", "যাবি" to "যাবেন",
        "করবি" to "করবেন", "পারবি" to "পারবেন"
    )

    private val verbCasual = mapOf(
        "উপস্থিত থাকবেন" to "আসবে", "যাবেন" to "যাবে",
        "করবেন" to "করবে", "বলবেন" to "বলবে",
        "দেবেন" to "দেবে", "খাবেন" to "খাবে",
        "পারবেন" to "পারবে", "আসুন" to "আসো",
        "যান" to "যাও", "করুন" to "করো",
        "বলুন" to "বলো", "দিন" to "দাও"
    )

    // Word formality mapping
    private val wordFormal = mapOf(
        "কাল" to "আগামীকাল", "আজকে" to "আজ",
        "এখনি" to "এই মুহূর্তে", "অফিস" to "কার্যালয়",
        "মিটিং" to "সভা", "বস" to "ঊর্ধ্বতন কর্মকর্তা",
        "ওকে" to "ঠিক আছে", "হ্যাঁ" to "জি",
        "না" to "না, ধন্যবাদ"
    )

    private val wordCasual = mapOf(
        "আগামীকাল" to "কাল", "এই মুহূর্তে" to "এখনি",
        "কার্যালয়" to "অফিস", "সভা" to "মিটিং",
        "ঊর্ধ্বতন কর্মকর্তা" to "বস", "জি" to "হ্যাঁ"
    )

    // Filler words to remove for concise tone
    private val fillerWords = setOf(
        "আমি মনে করি", "সম্ভবত", "হয়তো", "আসলে",
        "যদি বলি", "মূলত", "বস্তুত", "প্রকৃতপক্ষে",
        "অর্থাৎ", "যাই হোক"
    )

    // Respectful prefixes/suffixes
    private val respectfulAdditions = mapOf(
        "আসতে" to "পধারতে", "যেতে" to "যেতে",
        "বলতে" to "বলতে", "দেখতে" to "দেখতে"
    )

    fun applyPronounUpgrade(text: String): String {
        var result = text
        for ((from, to) in pronounUpgrade) {
            result = result.replace(from, to)
        }
        return result
    }

    fun applyPronounDowngrade(text: String): String {
        var result = text
        for ((from, to) in pronounDowngrade) {
            result = result.replace(from, to)
        }
        return result
    }

    fun applyVerbFormalize(text: String): String {
        var result = text
        for ((from, to) in verbFormal) {
            result = result.replace(from, to)
        }
        return result
    }

    fun applyVerbCasualize(text: String): String {
        var result = text
        for ((from, to) in verbCasual) {
            result = result.replace(from, to)
        }
        return result
    }

    fun applyWordFormalize(text: String): String {
        var result = text
        for ((from, to) in wordFormal) {
            result = result.replace(from, to)
        }
        return result
    }

    fun applyWordCasualize(text: String): String {
        var result = text
        for ((from, to) in wordCasual) {
            result = result.replace(from, to)
        }
        return result
    }

    fun removeFillers(text: String): String {
        var result = text
        for (filler in fillerWords) {
            result = result.replace(filler, "")
        }
        return result.replace(Regex("\\s+"), " ").trim()
    }
}

// ══════════════════════════════════════════════════
// Tone Rewriting Engine
// ══════════════════════════════════════════════════

class ToneRewritingEngine(private val context: Context) {

    fun rewrite(text: String, targetTone: ToneType): ToneResult {
        val startTime = System.currentTimeMillis()

        val rewritten = when (targetTone) {
            ToneType.FORMAL -> toFormal(text)
            ToneType.FRIENDLY -> toFriendly(text)
            ToneType.RESPECTFUL -> toRespectful(text)
            ToneType.CONCISE -> toConcise(text)
            ToneType.DETAILED -> toDetailed(text)
            ToneType.POETIC -> toPoetic(text)
            ToneType.HUMOROUS -> toHumorous(text)
            ToneType.EMPATHETIC -> toEmpathetic(text)
        }

        val elapsed = System.currentTimeMillis() - startTime

        return ToneResult(
            originalText = text,
            rewrittenText = rewritten,
            tone = targetTone,
            confidence = 0.85f,
            processingTimeMs = elapsed
        )
    }

    private fun toFormal(text: String): String {
        var result = BengaliToneRules.applyPronounUpgrade(text)
        result = BengaliToneRules.applyVerbFormalize(result)
        result = BengaliToneRules.applyWordFormalize(result)
        // Add polite question marker if question
        if (result.contains("?") || result.contains("কি")) {
            result = result.replace("কি", "কি দয়া করে")
        }
        return result
    }

    private fun toFriendly(text: String): String {
        var result = BengaliToneRules.applyPronounDowngrade(text)
        result = BengaliToneRules.applyVerbCasualize(result)
        result = BengaliToneRules.applyWordCasualize(result)
        // Add friendly emoji
        if (!result.contains("😊")) result += " 😊"
        return result
    }

    private fun toRespectful(text: String): String {
        var result = BengaliToneRules.applyPronounUpgrade(text)
        result = BengaliToneRules.applyVerbFormalize(result)
        // Apply respectful verb forms
        result = result.replace("আসতে", "পধারতে")
        // Add respectful prefix
        if (!result.startsWith("দয়া করে") && !result.startsWith("অনুগ্রহ করে")) {
            result = "অনুগ্রহ করে $result"
        }
        return result
    }

    private fun toConcise(text: String): String {
        var result = BengaliToneRules.removeFillers(text)
        // Shorten common phrases
        result = result.replace("উপস্থিত থাকবেন", "আসবেন")
        result = result.replace("এই মুহূর্তে", "এখন")
        result = result.replace(Regex("\\s+"), " ").trim()
        return result
    }

    private fun toDetailed(text: String): String {
        // Add elaboration markers
        var result = text
        if (!result.contains("অর্থাৎ") && result.length < 50) {
            result += "। বিস্তারিত বলতে গেলে, এটি একটি গুরুত্বপূর্ণ বিষয়"
        }
        return result
    }

    private fun toPoetic(text: String): String {
        // Simple poetic word substitutions (neural engine would do better)
        var result = text
        result = result.replace("সুন্দর", "রূপের মাধুরী")
        result = result.replace("আবহাওয়া", "প্রকৃতি")
        result = result.replace("ভালো", "মনোরম")
        result = result.replace("দেখা", "দর্শন")
        result = result.replace("খুব", "অতীব")
        return result
    }

    private fun toHumorous(text: String): String {
        var result = text
        // Add playful markers
        if (!result.endsWith("😄") && !result.endsWith("😂")) {
            result += " 😄"
        }
        return result
    }

    private fun toEmpathetic(text: String): String {
        var result = text
        // Add empathetic prefix
        if (!result.startsWith("আমি বুঝতে পারছি")) {
            result = "আমি বুঝতে পারছি। $result"
        }
        if (!result.endsWith("💝")) result += " 💝"
        return result
    }
}

// ══════════════════════════════════════════════════
// Tone Rewriter View (UI Component)
// ══════════════════════════════════════════════════

class ToneRewriterView(private val context: Context) {

    private val engine = ToneRewritingEngine(context)
    private var currentResult: ToneResult? = null
    private var onAcceptListener: ((String) -> Unit)? = null

    fun setOnAcceptListener(listener: (String) -> Unit) {
        onAcceptListener = listener
    }

    /**
     * Show the tone rewriter bottom sheet dialog.
     */
    fun show(selectedText: String) {
        val dialog = BottomSheetDialog(context)
        val rootLayout = buildDialogUI(selectedText, dialog)
        dialog.setContentView(rootLayout)
        dialog.show()
    }

    private fun buildDialogUI(text: String, dialog: BottomSheetDialog): LinearLayout {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(Color.WHITE)
        }

        // Title
        root.addView(TextView(context).apply {
            this.text = "✨ টোন পরিবর্তন করুন"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTextColor(Color.parseColor("#1F1F1F"))
            setPadding(0, 0, 0, dp(8))
        })

        // Original text
        root.addView(TextView(context).apply {
            this.text = "মূল: $text"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(Color.parseColor("#666666"))
            setPadding(0, 0, 0, dp(12))
        })

        // Tone chips
        val chipGroup = ChipGroup(context).apply {
            isSingleSelection = true
            setPadding(0, 0, 0, dp(12))
        }

        for (tone in ToneType.values()) {
            val chip = Chip(context).apply {
                this.text = tone.displayName
                isCheckable = true
                isEnabled = !tone.isPremium // Disable premium tones (for now)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        val result = engine.rewrite(text, tone)
                        currentResult = result
                        updatePreview(root, result)
                    }
                }
            }
            chipGroup.addView(chip)
        }
        root.addView(chipGroup)

        // Preview area (placeholder)
        root.addView(TextView(context).apply {
            tag = "preview_text"
            this.text = "☝️ উপরে একটি টোন নির্বাচন করুন"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(Color.parseColor("#1A73E8"))
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F0F4FF"))
                cornerRadius = dp(8).toFloat()
            }
        })

        // Buttons row
        val buttonRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(16), 0, 0)
        }

        buttonRow.addView(Button(context).apply {
            this.text = "বাতিল"
            setOnClickListener { dialog.dismiss() }
        })

        buttonRow.addView(Button(context).apply {
            this.text = "ব্যবহার করুন"
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1A73E8"))
                cornerRadius = dp(8).toFloat()
            }
            setOnClickListener {
                currentResult?.let { result ->
                    onAcceptListener?.invoke(result.rewrittenText)
                }
                dialog.dismiss()
            }
        })

        root.addView(buttonRow)
        return root
    }

    private fun updatePreview(root: LinearLayout, result: ToneResult) {
        val preview = root.findViewWithTag<TextView>("preview_text")
        preview?.text = "${result.tone.emoji} ${result.rewrittenText}\n\n⏱️ ${result.processingTimeMs}ms"
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
