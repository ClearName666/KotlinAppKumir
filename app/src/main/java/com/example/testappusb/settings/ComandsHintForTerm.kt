package com.example.testappusb.settings

import android.content.Context
import java.io.File

// активный пакет команд для подсказок
class ComandsHintForTerm {
    companion object {
        const val fileNameCommands = "commands.txt"
        const val fileNameCommandsHistory = "commands_history.txt"
        var lisComand: ArrayList<String> = arrayListOf()
        var lisComandHistory: ArrayList<String> = arrayListOf()

        // запись в файл сета с подсказками
        fun saveToFile(context: Context, fileName: String) {
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
                output.write(
                    if (fileName == fileNameCommands)
                        lisComand.joinToString(",").toByteArray()
                    else
                        lisComandHistory.joinToString(",").toByteArray()
                )
            }
        }

        // чтение файла сета с подсказками
        fun loadFromFile(context: Context, fileName: String) {
            val file = File(context.filesDir, fileName)

            if (fileName == fileNameCommands) {
                lisComand = if (!file.exists()) {
                    // Файл не существует
                    arrayListOf()
                } else {
                    val data = context.openFileInput(fileName).bufferedReader().useLines { lines ->
                        lines.fold("") { some, text -> some + text }
                    }
                    ArrayList(data.split(","))
                }
            } else {
                lisComandHistory = if (!file.exists()) {
                    // Файл не существует
                    arrayListOf()
                } else {
                    val data = context.openFileInput(fileName).bufferedReader().useLines { lines ->
                        lines.fold("") { some, text -> some + text }
                    }
                    ArrayList(data.split(","))
                }
            }
        }
    }
}