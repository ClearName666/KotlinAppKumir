package com.example.testappusb.model.dataBase.commandsDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

// класс для работы с базой данных
@Dao
interface CommandsDBDao {
    @Query("SELECT * FROM commandsdb")
    fun getAll(): List<CommandsDB>

    @Insert
    fun insertAll(vararg commands: CommandsDB)

    @Delete
    fun delete(commandsDB: CommandsDB)
}