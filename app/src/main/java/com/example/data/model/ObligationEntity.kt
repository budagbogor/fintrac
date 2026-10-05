package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "obligations")
data class ObligationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String = "SEWA", // SEWA, CICILAN, LISTRIK, AIR, INTERNET, LAINNYA
    val dueDayOfMonth: Int = 1,
    val isActive: Boolean = true,
    val notes: String = ""
)
