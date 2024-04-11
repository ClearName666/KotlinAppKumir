package com.example.testappusb

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testappusb.adapters.AdaptersSettingCommandHintActivity.HintCommandsSetViewAdapter
import com.example.testappusb.databinding.ActivitySaveHintsCommandsBinding
import com.example.testappusb.model.dataBases.CommandsDB
import com.example.testappusb.model.dataBases.DataBaseCommands
import com.example.testappusb.model.recyclerModelSettingComHintActivity.HintCommandsSetView
import com.example.testappusb.settings.ConstDataStartHintForDataBaseCommands
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// выбор присета подсказок
class SettingCommandHintActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val showElements = ActivitySaveHintsCommandsBinding.inflate(layoutInflater)
        setContentView(showElements.root)


        // карутина что бы читать информацию с базы данных
        lifecycleScope.launch(Dispatchers.IO) {
            val dbCommands = DataBaseCommands.getDatabase(this@SettingCommandHintActivity)

            // если база данных пуста то заполняем ее начальными данными
            val count = dbCommands.commandsDBDao().getCount()
            if (count == 0) {
                dbCommands.commandsDBDao().insertAll(
                    CommandsDB(1,
                        ConstDataStartHintForDataBaseCommands.m32GsmModemList,
                        ConstDataStartHintForDataBaseCommands.m32GsmModemName
                    )
                )
            }

            // получаем данные
            val data = dbCommands.commandsDBDao().getAll()
            val hintCommandsList = data.map { HintCommandsSetView(it.name)}

            // выводим данные в recyclerView
            withContext(Dispatchers.Main) {
                showElements.itemsSetHint.layoutManager = LinearLayoutManager(this@SettingCommandHintActivity)
                val hintCommandsSetViewAdapter = HintCommandsSetViewAdapter(this@SettingCommandHintActivity, hintCommandsList)
                showElements.itemsSetHint.adapter = hintCommandsSetViewAdapter
            }
        }
    }

    fun onClickButtonReturnedMain(view: View) {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }
}
