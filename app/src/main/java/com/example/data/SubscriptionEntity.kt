package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscription_status")
data class SubscriptionEntity(
    @PrimaryKey val id: Int = 1,
    val planTier: String = "FREE_TRIAL", // FREE_TRIAL, PRO, UNLIMITED_VIP
    val isSubscribed: Boolean = false,
    val expiryTimestamp: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L), // 7 days trial
    val usedCoupon: String? = null,
    val maxRulesAllowed: Int = 3,
    val dailyClicksCount: Int = 0,
    val lastResetDate: Long = System.currentTimeMillis()
)
