package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "couple_profile")
data class CoupleProfileEntity(
    @PrimaryKey
    val id: Long = 1,
    val partner1Name: String = "Anh",
    val partner2Name: String = "Em",
    val partner1Avatar: String = "avatar_anh",
    val partner2Avatar: String = "avatar_em",
    val startDateEpochDay: Long = 20089L, // Approx Jan 1, 2025
    val fogRadiusMeters: Int = 300,
    val notificationsEnabled: Boolean = true,
    val themeColorName: String = "Hồng phấn",
    val pairCode: String = "LM-8899"
)
