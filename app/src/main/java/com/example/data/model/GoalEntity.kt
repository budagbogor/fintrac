package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "EMERGENCY_FUND", // EMERGENCY_FUND, HOUSE_DP, RETIREMENT, CUSTOM
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetHorizonMonths: Int = 12,
    val riskProfile: String = "MODERATE", // CONSERVATIVE, MODERATE, AGGRESSIVE
    val expectedCagrPercentage: Double = 8.0
)
