package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String = "MAKANAN", // MAKANAN, TRANSPORT, HIBURAN, BELANJA, KESEHATAN, LAINNYA
    val dateEpochMs: Long = System.currentTimeMillis(),
    val notes: String = ""
)
