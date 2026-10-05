package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_promos")
data class PromoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantName: String,
    val promoTitle: String,
    val discountDetails: String,
    val howToGet: String,
    val informationLink: String,
    val validityPeriod: String,
    val isOnline: Boolean, // true for online, false for offline
    val category: String = "Lainnya", // e.g. Kafe, Restoran, Mall, Hotel, E-Commerce, Online, Lainnya
    val isSaved: Boolean = true,
    val timestampMs: Long = System.currentTimeMillis()
)
