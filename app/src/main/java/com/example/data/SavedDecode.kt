package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_decodes")
data class SavedDecode(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val format: String,
    val encodedText: String,
    val decodedText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val decodeSteps: String = ""
)
