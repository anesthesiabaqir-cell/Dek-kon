package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchQueryDao {
    @Query("SELECT * FROM user_search_queries ORDER BY timestamp DESC LIMIT 40")
    fun getAllRecentQueries(): Flow<List<SearchQueryEntity>>

    @Query("SELECT * FROM user_search_queries WHERE LOWER(grammarType) = LOWER(:type) ORDER BY timestamp DESC LIMIT 40")
    fun getRecentQueriesByType(type: String): Flow<List<SearchQueryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: SearchQueryEntity): Long

    @Query("SELECT * FROM user_search_queries WHERE LOWER(query) = LOWER(:query) AND LOWER(grammarType) = LOWER(:type) LIMIT 1")
    suspend fun findQuery(query: String, type: String): SearchQueryEntity?

    @Query("DELETE FROM user_search_queries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM user_search_queries WHERE LOWER(query) = LOWER(:query)")
    suspend fun deleteByQuery(query: String)

    @Query("DELETE FROM user_search_queries")
    suspend fun clearAllQueries()
}
