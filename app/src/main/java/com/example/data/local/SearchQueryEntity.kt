package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a user's previous search term / query.
 * Tracks the search query string, grammar type (Nomen or Verb), timestamp,
 * and number of times looked up so users can quickly re-access previous searches.
 */
@Entity(
    tableName = "user_search_queries",
    indices = [Index(value = ["query", "grammarType"], unique = true)]
)
data class SearchQueryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val grammarType: String = "Nomen", // "Nomen" or "Verb"
    val timestamp: Long = System.currentTimeMillis(),
    val lookupCount: Int = 1
)
