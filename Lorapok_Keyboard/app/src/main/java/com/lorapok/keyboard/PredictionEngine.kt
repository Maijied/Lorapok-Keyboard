package com.lorapok.keyboard

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Lorapok Keuboard — Prediction Engine (v3)
 *
 * Uses a SQLite database for high-scale next-word predictions.
 * Capable of handling 250,000+ bigrams with minimal memory footprint.
 */
class PredictionEngine(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {

    private var db: SQLiteDatabase? = null

    companion object {
        const val DATABASE_NAME = "bigram_model.db"
    }

    init {
        deployDatabase()
        db = readableDatabase
    }

    private fun deployDatabase() {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()
            context.assets.open(DATABASE_NAME).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Tables are pre-created in the asset DB
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldV: Int, newV: Int) {}

    fun predict(currentWord: String): List<String> {
        val suggestions = mutableListOf<String>()
        val query = "SELECT w2 FROM bigrams WHERE w1 = ? ORDER BY freq DESC LIMIT 5"
        
        try {
            db?.rawQuery(query, arrayOf(currentWord))?.use { cursor ->
                while (cursor.moveToNext()) {
                    suggestions.add(cursor.getString(0))
                }
            }
        } catch (e: Exception) {
            // Log error or fallback
        }

        return if (suggestions.isEmpty()) getCommonSuggestions() else suggestions
    }

    fun getCommonSuggestions(): List<String> {
        val suggestions = mutableListOf<String>()
        val query = "SELECT word FROM common ORDER BY freq DESC LIMIT 5"
        
        try {
            db?.rawQuery(query, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    suggestions.add(cursor.getString(0))
                }
            }
        } catch (e: Exception) {}

        return if (suggestions.isEmpty()) listOf("আমি", "তুমি", "আপনি", "ভালো", "ধন্যবাদ") else suggestions
    }

    fun learn(word1: String, word2: String) {
        // Optional: Update user-specific frequency table in the DB
    }
    
    override fun close() {
        db?.close()
        super.close()
    }
}
