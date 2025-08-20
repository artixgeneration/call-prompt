package com.artix.callnote.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey val phoneNumber: String,
    val content: String,
    val updatedAt: Long
)
