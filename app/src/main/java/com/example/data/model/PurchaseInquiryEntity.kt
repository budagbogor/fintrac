package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_inquiries")
data class PurchaseInquiryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemName: String,
    val price: Double,
    val urgency: String, // WANT, NEED
    val decision: String, // BUY_NOW, POSTPONE, AVOID
    val postponeMonths: Int = 0,
    val opportunityCost3Yr: Double = 0.0,
    val opportunityCost5Yr: Double = 0.0,
    val aiRationale: String = "",
    val timestampMs: Long = System.currentTimeMillis()
)
