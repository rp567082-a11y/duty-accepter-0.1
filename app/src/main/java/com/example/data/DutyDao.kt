package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DutyDao {

    // Duty Rules
    @Query("SELECT * FROM duty_rules ORDER BY priority DESC, createdAt DESC")
    fun getAllRules(): Flow<List<DutyRuleEntity>>

    @Query("SELECT * FROM duty_rules WHERE isEnabled = 1 ORDER BY priority DESC")
    suspend fun getActiveRulesList(): List<DutyRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: DutyRuleEntity): Long

    @Update
    suspend fun updateRule(rule: DutyRuleEntity)

    @Query("DELETE FROM duty_rules WHERE id = :ruleId")
    suspend fun deleteRule(ruleId: Long)

    @Query("UPDATE duty_rules SET isEnabled = :isEnabled WHERE id = :ruleId")
    suspend fun toggleRule(ruleId: Long, isEnabled: Boolean)

    // Duty Logs
    @Query("SELECT * FROM duty_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllLogs(): Flow<List<DutyLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DutyLogEntity)

    @Query("DELETE FROM duty_logs")
    suspend fun clearAllLogs()

    // Subscription Status
    @Query("SELECT * FROM subscription_status WHERE id = 1 LIMIT 1")
    fun getSubscriptionStatus(): Flow<SubscriptionEntity?>

    @Query("SELECT * FROM subscription_status WHERE id = 1 LIMIT 1")
    suspend fun getSubscriptionDirect(): SubscriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSubscription(status: SubscriptionEntity)
}
