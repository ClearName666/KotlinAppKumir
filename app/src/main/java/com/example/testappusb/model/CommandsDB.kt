package com.example.testappusb.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CommandsDB(
    @PrimaryKey val id: Int,
    val commands: String,
    val name: String
)