package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val fullName: String = "Pengguna FinAdvisor",
    val riskProfile: String = "MODERATE", // CONSERVATIVE, MODERATE, AGGRESSIVE
    val monthlyTargetSavingsRatePercent: Double = 20.0,
    val aiProvider: String = "Gemini", // Gemini, OpenAI, Claude, Custom
    val aiModel: String = "gemini-1.5-flash",
    val apiKey: String = "",
    val aiBaseUrl: String = "https://api.sumopod.com/v1",
    val preferShariaAndGold: Boolean = true
)

