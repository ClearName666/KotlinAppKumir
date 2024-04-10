package com.example.testappusb

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import com.example.testappusb.databinding.ActivitySaveHintsCommandsBinding

class SettingCommandHintActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val showElements = ActivitySaveHintsCommandsBinding.inflate(layoutInflater)
        setContentView(showElements.root)
    }

    fun onClickButtonReturnedMain(view: View) {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }
}
