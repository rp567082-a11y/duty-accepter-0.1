package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "duty_logs")
data class DutyLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val ruleTitle: String,
    val packageName: String,
    val matchedText: String,
    val actionTaken: String,
    val isSuccess: Boolean,
    val statusMessage: String
)
