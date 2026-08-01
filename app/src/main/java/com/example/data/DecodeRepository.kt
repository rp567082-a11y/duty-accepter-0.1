package com.example.data

import kotlinx.coroutines.flow.Flow

class DecodeRepository(private val dao: SavedDecodeDao) {
    val allSavedDecodes: Flow<List<SavedDecode>> = dao.getAllSavedDecodes()
    val favoriteDecodes: Flow<List<SavedDecode>> = dao.getFavoriteDecodes()

    suspend fun saveDecode(decode: SavedDecode): Long = dao.insert(decode)

    suspend fun updateDecode(decode: SavedDecode) = dao.update(decode)

    suspend fun deleteDecode(decode: SavedDecode) = dao.delete(decode)

    suspend fun deleteById(id: Int) = dao.deleteById(id)

    suspend fun clearHistory() = dao.clearAll()
}
