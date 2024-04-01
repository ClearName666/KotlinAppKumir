package com.example.testappusb

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.BoringLayout
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testappusb.adapters.SettingsSerialConnectDeviceViewAdapter
import com.example.testappusb.databinding.ActivityMainBinding
import com.example.testappusb.model.SettingsSerialConnectDeviceView
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    lateinit var showElements: ActivityMainBinding
    private var message: String = ""

    val executorUsb = Executors.newSingleThreadExecutor()
    val mainHandler: Handler = Handler(Looper.getMainLooper())
    val usb: Usb = Usb(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showElements = ActivityMainBinding.inflate(getLayoutInflater())
        setContentView(showElements.root)

        val settingsList = arrayListOf(
            SettingsSerialConnectDeviceView(1, arrayListOf("число бит 8", "число бит 7")),
            SettingsSerialConnectDeviceView(2, arrayListOf("скорость 300", "скорость 600", "скорость 1200", "скорость 2400", "скорость 4800", "скорость 9600", "скорость 19200", "скорость 38400", "скорость 57600", "скорость 115200")),
            SettingsSerialConnectDeviceView(3, arrayListOf("четность None", "четность Even", "четность Odd")),
            SettingsSerialConnectDeviceView(4, arrayListOf("стоп бит 1", "стоп бит 2"))
        )

        val adapter = SettingsSerialConnectDeviceViewAdapter(this, settingsList)
        showElements.settingsRecyclerView.adapter = adapter
        showElements.settingsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        /*val listSpinner: ArrayList<Spinner> = adapter.listSpinner
        listSpinner.get(0).onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == 0) {
                    onSelectUumBit(false)
                } else {
                    onSelectUumBit(true)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        listSpinner.get(1).onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                onSerialSpeed(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        listSpinner.get(2).onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                onSerialParity(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        listSpinner.get(3).onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                onSerialStopBits(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }*/
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usb.usbReceiver)
        usb.connection?.close()
        usb.connection = null
        usb.usbSerialDevice?.close()
        usb.usbSerialDevice = null
        executorUsb.shutdown()
    }

    fun onClickButtonConnect(view: View) {
        connectToUsbDevice()
    }
    fun onClickButtonMoveToData(view: View) {
        val textIn: String = showElements.textInputDataForMoveToData.text.toString()
        if (textIn.length != 0) {
            message = "input>>>" + textIn + "\n"
            showElements.textInputDataForMoveToData.setText("")
            var response: String? = null
            executorUsb.execute {
                response = usb.useToConnectToDivice(message)
                if (response != "") {
                    mainHandler.post {
                        showButtonConnection(false)
                        showAlertDialog(response.toString())
                    }
                } else {
                    mainHandler.post {
                        val termText: String = showElements.textDataTerm.text.toString()
                        showElements.textDataTerm.text = termText + message
                    }
                }
            }
        }
    }


    fun showButtonConnection(con: Boolean) {
        if (con) {
            showElements.buttonConnect.text = "Подключено"
            showElements.buttonConnect.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
        } else {
            showElements.buttonConnect.text = "Подключить"
            showElements.buttonConnect.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red))
        }
    }
    fun showAlertDialog(msg: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(msg)
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
    fun showReadData(data: String) {
        val textTerm: String = showElements.textDataTerm.text.toString()
        val message: String = "output>>>" + data + "\n"
        showElements.textDataTerm.text = textTerm + message
    }

    private fun connectToUsbDevice(){
        val usbManager: UsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = usbManager.deviceList
        val device = deviceList.values.firstOrNull()
        if (device == null) {
            showButtonConnection(false)
            showAlertDialog("Устройство не обнаружено, подключите устройство")
        }

        device?.let {
            try {
                val permissionIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Для Android 12 (API уровень 31)
                    PendingIntent.getBroadcast(this, 0, Intent(usb.ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE)
                } else {
                    // Для Android ниже 12
                    PendingIntent.getBroadcast(this, 0, Intent(usb.ACTION_USB_PERMISSION), 0)
                }
                //val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(usb.ACTION_USB_PERMISSION), 0)
                registerReceiver(usb.usbReceiver, IntentFilter(usb.ACTION_USB_PERMISSION))
                usbManager.requestPermission(device, permissionIntent)
            } catch (e: Exception) {
                showAlertDialog("При подключении произошла ошибка")
            }

        }
    }



}