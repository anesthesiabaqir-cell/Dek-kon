package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<WordHistoryEntity>>

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC")
    suspend fun getAllHistoryList(): List<WordHistoryEntity>

    @Query("SELECT * FROM search_history WHERE LOWER(type) = LOWER(:type) ORDER BY timestamp DESC")
    fun getHistoryByType(type: String): Flow<List<WordHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WordHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<WordHistoryEntity>)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM search_history WHERE LOWER(word) = LOWER(:word)")
    suspend fun deleteByWord(word: String)

    @Query("DELETE FROM search_history WHERE LOWER(word) = LOWER(:word) AND LOWER(type) = LOWER(:type)")
    suspend fun deleteByWordAndType(word: String, type: String)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()

    @Query("DELETE FROM search_history WHERE LOWER(type) = LOWER(:type)")
    suspend fun clearByType(type: String)

    @Query("SELECT * FROM search_history WHERE LOWER(word) = LOWER(:word) LIMIT 1")
    suspend fun findByWord(word: String): WordHistoryEntity?

    @Query("SELECT * FROM search_history WHERE LOWER(word) = LOWER(:word) AND LOWER(type) = LOWER(:type) LIMIT 1")
    suspend fun findByWordAndType(word: String, type: String): WordHistoryEntity?
}

