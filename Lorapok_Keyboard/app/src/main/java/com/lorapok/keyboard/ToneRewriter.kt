package com.lorapok.keyboard

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.button.MaterialButton

/**
 * Lorapok Keuboard — Tone Rewriter
 *
 * Provides 8 Bengali tone transformations:
 *   Free:    Formal, Friendly, Respectful, Concise, Detailed
 *   Premium: Poetic, Humorous, Empathetic
 *
 * Uses a hybrid approach with rule-based transformations for Bengali formality levels.
 */

enum class ToneType(
    val id: String,
    val bengaliName: String,
    val emoji: String,
    val isPremium: Boolean
) {
    FORMAL("formal", "আনুষ্ঠানিক", "🎩", false),
    FRIENDLY("friendly", " বন্ধুত্বপূর্ণ", "😊", false),
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
    val processingTimeMs: Long
)

class ToneRewriter(private val context: Context) {

    private val engine = ToneRewritingEngine()
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
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_rewrite_preview, null)
        dialog.setContentView(view)

        val textOriginal = view.findViewById<TextView>(R.id.text_original)
        val textRewritten = view.findViewById<TextView>(R.id.text_rewritten)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chip_group_tones)
        val btnUseThis = view.findViewById<MaterialButton>(R.id.btn_use_this)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btn_cancel)

        textOriginal.text = selectedText

        // Handle chip selection
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(checkedId)
            val tone = ToneType.values().find { it.displayName == chip.text.toString().replace(" ⭐", "") }
            
            if (tone != null) {
                val result = engine.rewrite(selectedText, tone)
                currentResult = result
                textRewritten.text = result.rewrittenText
                textRewritten.setTextColor(Color.parseColor("#1F1F1F"))
            }
        }

        btnUseThis.setOnClickListener {
            currentResult?.let { result ->
                onAcceptListener?.invoke(result.rewrittenText)
            }
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private class ToneRewritingEngine {
        
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
            "ঊর্ধ্বতন কর্মকর্তা" to "বস", "জি" to "হ্যাঁ",
            "বিরক্ত" to "পেরা", "বন্ধু" to "মামা",
            "অপেক্ষা কর" to "একটু ওয়েট", "আরাম" to "চিল"
        )

        // Filler words to remove for concise tone
        private val fillerWords = setOf(
            "আমি মনে করি", "সম্ভবত", "হয়তো", "আসলে",
            "যদি বলি", "মূলত", "বস্তুত", "প্রকৃতপক্ষে",
            "অর্থাৎ", "যাই হোক"
        )

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
            
            return ToneResult(
                originalText = text,
                rewrittenText = rewritten,
                tone = targetTone,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
        }

        private fun toFormal(text: String): String {
            var result = text
            for ((from, to) in pronounUpgrade) result = result.replace(from, to)
            for ((from, to) in verbFormal) result = result.replace(from, to)
            for ((from, to) in wordFormal) result = result.replace(from, to)
            // Add polite question marker if question
            if (result.contains("?") || result.contains("কি")) {
                result = result.replace("কি", "কি দয়া করে")
            }
            return result
        }

        private fun toFriendly(text: String): String {
            var result = text
            for ((from, to) in pronounDowngrade) result = result.replace(from, to)
            for ((from, to) in verbCasual) result = result.replace(from, to)
            for ((from, to) in wordCasual) result = result.replace(from, to)
            if (!result.contains("😊")) result += " 😊"
            return result
        }

        private fun toRespectful(text: String): String {
            var result = toFormal(text)
            // Apply respectful verb forms
            result = result.replace("আসতে", "পধারতে")
            // Add respectful prefix
            if (!result.startsWith("দয়া করে") && !result.startsWith("অনুগ্রহ করে")) {
                result = "অনুগ্রহ করে $result"
            }
            return result
        }

        private fun toConcise(text: String): String {
            var result = text
            for (filler in fillerWords) result = result.replace(filler, "")
            result = result.replace("উপস্থিত থাকবেন", "আসবেন")
            result = result.replace("এই মুহূর্তে", "এখন")
            return result.replace(Regex("\\s+"), " ").trim()
        }

        private fun toDetailed(text: String): String {
            var result = text
            if (!result.contains("অর্থাৎ") && result.length < 50) {
                result += "। বিস্তারিত বলতে গেলে, এটি একটি গুরুত্বপূর্ণ বিষয়"
            }
            return result
        }

        private fun toPoetic(text: String): String {
            var result = text
            result = result.replace("সুন্দর", "রূপের মাধুরী")
            result = result.replace("আবহাওয়া", "প্রকৃতি")
            result = result.replace("ভালো", "মনোরম")
            result = result.replace("দেখা", "দর্শন")
            result = result.replace("খুব", "অতীব")
            return result
        }

        private fun toHumorous(text: String): String {
            return if (text.endsWith("😄") || text.endsWith("😂")) text else "$text 😄"
        }

        private fun toEmpathetic(text: String): String {
            var result = text
            if (!result.startsWith("আমি বুঝতে পারছি")) {
                result = "আমি বুঝতে পারছি। $result"
            }
            if (!result.endsWith("💝")) result += " 💝"
            return result
        }
    }
}
