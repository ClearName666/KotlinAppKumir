package com.example.testappusb.model.dataBases

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.testappusb.settings.ConstDataStartHintForDataBaseCommands
import java.util.concurrent.Executors


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