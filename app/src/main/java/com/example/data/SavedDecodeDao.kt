package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedDecodeDao {
    @Query("SELECT * FROM saved_decodes ORDER BY timestamp DESC")
    fun getAllSavedDecodes(): Flow<List<SavedDecode>>

    @Query("SELECT * FROM saved_decodes WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteDecodes(): Flow<List<SavedDecode>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(decode: SavedDecode): Long

    @Update
    suspend fun update(decode: SavedDecode)

    @Delete
    suspend fun delete(decode: SavedDecode)

    @Query("DELETE FROM saved_decodes WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM saved_decodes")
    suspend fun clearAll()
}
