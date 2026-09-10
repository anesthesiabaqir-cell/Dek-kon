package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class WordHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val type: String = "Nomen",           // "Nomen" or "Verb"
    val data: String = "",                // Full JSON object containing AI response
    val timestamp: Long = System.currentTimeMillis(),
    val gender: String = "",              // For Nouns: Maskulin/Feminin/Neutrum; for Verbs: Hilfsverb (haben/sein)
    val genderArticle: String = "",       // For Nouns: der/die/das; for Verbs: Partizip II
    val meaningEnglish: String = "",
    val rawJsonResult: String = ""        // Backwards compatibility alias for data
)

