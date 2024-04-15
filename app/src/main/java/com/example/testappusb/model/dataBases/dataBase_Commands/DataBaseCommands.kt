package com.example.testappusb.model.dataBases.dataBase_Commands

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


// класс для доступа к базе дынных
@Database(entities = [CommandsDB::class], version = 1)
abstract class DataBaseCommands : RoomDatabase() {
    abstract fun commandsDBDao(): CommandsDBDao

    companion object {
        @Volatile
        private var INSTANCE: DataBaseCommands? = null

        fun getDatabase(context: Context): DataBaseCommands {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DataBaseCommands::class.java,
                    "database_commands"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}