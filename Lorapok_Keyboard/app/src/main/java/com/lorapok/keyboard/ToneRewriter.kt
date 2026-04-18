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
        fun rewrite(text: String, targetTone: ToneType): ToneResult {
            val startTime = System.currentTimeMillis()
            
            val rewritten = when (targetTone) {
                ToneType.FORMAL -> applyFormal(text)
                ToneType.FRIENDLY -> applyFriendly(text)
                ToneType.RESPECTFUL -> applyRespectful(text)
                ToneType.CONCISE -> applyConcise(text)
                ToneType.DETAILED -> applyDetailed(text)
                else -> text // Premium placeholders
            }
            
            return ToneResult(
                originalText = text,
                rewrittenText = rewritten,
                tone = targetTone,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
        }

        private fun applyFormal(text: String): String {
            return text.replace("তুমি", "আপনি").replace("তোমার", "আপনার").replace("করো", "করুন")
        }

        private fun applyFriendly(text: String): String {
            return if (text.endsWith("😊")) text else "$text 😊"
        }

        private fun applyRespectful(text: String): String {
            return "শ্রদ্ধেয়, " + applyFormal(text)
        }

        private fun applyConcise(text: String): String {
            return text.take(20) + (if (text.length > 20) "..." else "")
        }

        private fun applyDetailed(text: String): String {
            return text + "। আমি এটি সম্পর্কে আরও বিস্তারিত জানতে আগ্রহী।"
        }
    }
}
