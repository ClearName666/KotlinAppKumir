package com.example.testappusb.settings

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.File

// активный пакет команд для подсказок
class ComandsHintForTerm {
    companion object {
        private const val fileNameCommands = "commands.txt"
        var lisComand: ArrayList<String> = arrayListOf()

        // запись в файл сета с подсказками
        fun saveToFile(context: Context) {
            context.openFileOutput(fileNameCommands, Context.MODE_PRIVATE).use { output ->
                output.write(lisComand.joinToString(",").toByteArray())
            }
        }

        // чтение файла сета с подсказками
        fun loadFromFile(context: Context) {
            val file = File(context.filesDir, fileNameCommands)
            lisComand = if (!file.exists()) {
                // Файл не существует
                arrayListOf()
            } else {
                val data = context.openFileInput(fileNameCommands).bufferedReader().useLines { lines ->
                    lines.fold("") { some, text -> some + text }
                }
                ArrayList(data.split(","))
            }

        }
    }
}