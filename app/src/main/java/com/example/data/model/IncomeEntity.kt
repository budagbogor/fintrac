package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String = "ROUTINE", // ROUTINE, NON_ROUTINE
    val category: String = "Gaji",
    val dateEpochMs: Long = System.currentTimeMillis(),
    val notes: String = ""
)
