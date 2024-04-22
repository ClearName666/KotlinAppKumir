package com.example.testappusb

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testappusb.adapters.AdaptersSettingCommandHintActivity.HintCommandsSetViewAdapter
import com.example.testappusb.databinding.ActivitySaveHintsCommandsBinding
import com.example.testappusb.model.dataBases.dataBase_Commands.CommandsDB
import com.example.testappusb.model.dataBases.dataBase_Commands.DataBaseCommands
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
            if (count != 3) {
                // на всякий случай удяляем все что было до этого
                dbCommands.commandsDBDao().deleteAll()

                // записываем деффолтные данные
                dbCommands.commandsDBDao().insertAll(
                    CommandsDB(1,
                        ConstDataStartHintForDataBaseCommands.m32GsmModemList,
                        ConstDataStartHintForDataBaseCommands.m32GsmModemName
                    )
                )
                dbCommands.commandsDBDao().insertAll(
                    CommandsDB(2,
                        ConstDataStartHintForDataBaseCommands.m32GsmLiteModemList,
                        ConstDataStartHintForDataBaseCommands.m32GsmLiteModemName
                    ),
                )
                dbCommands.commandsDBDao().insertAll(
                    CommandsDB(3,
                        ConstDataStartHintForDataBaseCommands.pm81ModemList,
                        ConstDataStartHintForDataBaseCommands.pm81ModemName
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
        i.putExtra("flag", "ok")
        setResult(Activity.RESULT_OK, i)
        finish()
    }
}
