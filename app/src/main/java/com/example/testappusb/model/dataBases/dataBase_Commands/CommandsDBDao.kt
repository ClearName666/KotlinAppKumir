package com.example.testappusb.model.dataBases.dataBase_Commands

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


    @Query("DELETE FROM CommandsDB")
    fun deleteAll()

    @Delete
    fun delete(commandsDB: CommandsDB)

    @Query("SELECT COUNT(id) FROM commandsdb")
    fun getCount(): Int
}