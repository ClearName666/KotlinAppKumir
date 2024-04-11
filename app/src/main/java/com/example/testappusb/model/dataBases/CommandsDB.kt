package com.example.testappusb.model.dataBases

import androidx.room.Entity
import androidx.room.PrimaryKey


// модель для базы данных которая будт хранить подсказки для комманд
@Entity
data class CommandsDB(
    @PrimaryKey val id: Int,
    val commands: String,
    val name: String
)