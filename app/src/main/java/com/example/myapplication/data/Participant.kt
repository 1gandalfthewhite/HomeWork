package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "participants")
data class Participant(
    @PrimaryKey
    val userId: Int,
    val fullName: String,
    val title: String, // Prof., Dr., Student
    val registrationType: Int, // 1-Full, 2-Student, 3-None
    val photoPath: String? = null
)
