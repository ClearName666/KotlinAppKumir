package com.example.testappusb.model.dataBase.commandsDB

import android.content.Context
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

        // Коллбек для начального заполнения базы данных
        private val roomCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Запуск в фоновом потоке
                Executors.newSingleThreadExecutor().execute {
                    // добавление встроеных подсказок из ConstDataStartHintForDataBaseCommands
                    INSTANCE?.commandsDBDao()?.insertAll(
                        CommandsDB(
                        1,
                        ConstDataStartHintForDataBaseCommands.m32GsmModemList,
                        ConstDataStartHintForDataBaseCommands.m32GsmModemName)
                    )
                }
            }
        }

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