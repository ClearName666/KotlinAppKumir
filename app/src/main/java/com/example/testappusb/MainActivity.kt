package com.example.testappusb

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
            SettingsSerialConnectDeviceView(4, arrayListOf("стоп бит 1", "стоп бит 2")),
            SettingsSerialConnectDeviceView(5, arrayListOf("перев. стр CR", "перев. стр TF", "перев. стр CRTF")
        )
        )

        val adapter = SettingsSerialConnectDeviceViewAdapter(this, settingsList)
        showElements.settingsRecyclerView.adapter = adapter
        showElements.settingsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usb.usbReceiver)
        usb.onDestroy()
        executorUsb.shutdown()
    }

    fun onClickButtonConnect(view: View) {
        if (usb.deviceConnect == null) {
            val usbManager: UsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
            val deviceList = usbManager.deviceList.values
            if (deviceList.size != 0) {
                val nameDeviceList: ArrayList<String> = arrayListOf()
                for (device in deviceList) {
                    nameDeviceList.add(device.productName.toString())
                }
                showAlertDialogChoiceDevices("Выберете устройство для подключения", nameDeviceList)
            } else {
                showButtonConnection(false)
                showAlertDialog("Устройство не обнаружено, подключите устройство")
            }
        } else {
            usb.onDestroy()
        }
    }
    fun onClickButtonMoveToData(view: View) {
        val textIn: String = showElements.textInputDataForMoveToData.text.toString()
        if (textIn.length != 0) {
            message = "input>>>" + textIn + "\n"
            showElements.textInputDataForMoveToData.setText("")
            var response: String? = null
            executorUsb.execute {
                response = usb.useToConnectToDivice(textIn)
                if (response != "") {
                    mainHandler.post {
                        showButtonConnection(false)
                        showAlertDialog(response.toString())
                    }
                } else {
                    mainHandler.post {
                        val termText: String = showElements.textDataTerm.text.toString()
                        showElements.textDataTerm.text = termText + message
                        showTermTextBottom()
                    }
                }
            }
        }
    }


    fun showButtonConnection(con: Boolean) {
        if (con) {
            showElements.buttonConnect.text = "Отключиться"
            showElements.buttonConnect.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))

        } else {
            showElements.buttonConnect.text = "Подключить"
            showElements.buttonConnect.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red))
            showElements.textDataTerm.text = ">>> Данные\n"
        }
    }
    fun showAlertDialogChoiceDevices(msg: String, list: ArrayList<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(msg)
        val choise = list.toArray(arrayOfNulls<String>(list.size))
        builder.setItems(choise) { dialog, which ->
            val usbManager: UsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
            val deviceList: HashMap<String, UsbDevice> = usbManager.deviceList
            try {
                val device: UsbDevice = deviceList.values.toList().get(which)
                device?.let {
                    connectToUsbDevice(device)
                }
            } catch (e: IndexOutOfBoundsException) {
                showAlertDialog("Устройство было извлечено из USB-порта. Пожалуйста, подключите его снова")
            }

        }
        val dialog = builder.create()
        dialog.show()
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
        showTermTextBottom()
    }
    fun showDeviceName(deviceName: String) {
        showElements.textDeviceName.text = deviceName
    }
    fun showTermTextBottom() {
        showElements.scrollTermText.post {
            showElements.scrollTermText.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun connectToUsbDevice(device: UsbDevice) {
        val usbManager: UsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
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