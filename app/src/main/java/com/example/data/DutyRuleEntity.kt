package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "duty_rules")
data class DutyRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val keyword: String,
    val targetPackage: String = "*",
    val autoClickText: String,
    val delayMs: Long = 100,
    val isEnabled: Boolean = true,
    val priority: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)
