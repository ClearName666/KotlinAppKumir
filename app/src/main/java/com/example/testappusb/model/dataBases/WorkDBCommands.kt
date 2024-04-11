package com.example.testappusb.model.dataBases

import android.content.Context
import android.util.Log
import com.example.testappusb.model.recyclerModelSettingComHintActivity.HintCommandsSetView
import com.example.testappusb.settings.ComandsHintForTerm


// класс для спицефической работой с базой данных commands
class WorkDBCommands {
    fun setHintCommands(context: Context, position: Int) {

        // поток что бы читать информацию с базы данных
        Thread {
            val dbCommands = DataBaseCommands.getDatabase(context)

            // получаем данные
            val data = dbCommands.commandsDBDao().getAll()
            val hintCommandsList = data.map { HintCommandsSetView(it.commands) }
            //Log.d("DataBase", hintCommandsList[position].text)

            val listCommands: ArrayList<String> =
                ArrayList(hintCommandsList[position].text.split("\n"))
            //Log.d("DataBase", listCommands.joinToString(","))

            ComandsHintForTerm.lisComand = listCommands
            ComandsHintForTerm.saveToFile(context)

        }.start()
    }
}