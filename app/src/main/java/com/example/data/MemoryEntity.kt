package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val locationName: String,
    val dateEpochDay: Long,
    val latitude: Double,
    val longitude: Double,
    val revealRadiusMeters: Int = 300,
    val description: String,
    val coverImageUri: String,
    val imagesJson: String, // Comma-separated or JSON list of image URIs/drawables
    val mood: String = "Hạnh phúc 🥰",
    val createdAt: Long = System.currentTimeMillis()
)
