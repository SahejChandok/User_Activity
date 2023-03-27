package com.demo.sensor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserActivityLogs")
data class UserActivityLog (
    @PrimaryKey(autoGenerate = true) val id: Int?,
    val duration: Double?,
    val dateTime: String? // stores the start date and time.
)