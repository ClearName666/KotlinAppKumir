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
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testappusb.adapters.SettingsSerialConnectDeviceViewAdapter
import com.example.testappusb.databinding.ActivityMainBinding
import com.example.testappusb.model.SettingsSerialConnectDeviceView

//  SERIAL TERMENALL серийный терминалл
class MainActivity : AppCompatActivity(), UsbActivityInterface {

    private lateinit var showElements: ActivityMainBinding

    override val usb: Usb = Usb(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showElements = ActivityMainBinding.inflate(layoutInflater)
        setContentView(showElements.root)

        // добавления выборки с настроками в горизонтальный скролл
        val settingsList = arrayListOf(
            SettingsSerialConnectDeviceView(arrayListOf(
                "число бит 8",
                "число бит 7")),
            SettingsSerialConnectDeviceView(arrayListOf(
                "скорость 300",
                "скорость 600",
                "скорость 1200",
                "скорость 2400",
                "скорость 4800",
                "скорость 9600",
                "скорость 19200",
                "скорость 38400",
                "скорость 57600",
                "скорость 115200")),
            SettingsSerialConnectDeviceView(arrayListOf(
                "четность None",
                "четность Even",
                "четность Odd")),
            SettingsSerialConnectDeviceView(arrayListOf(
                "стоп бит 1",
                "стоп бит 2")),
            SettingsSerialConnectDeviceView(arrayListOf(
                "перев. стр CR",
                "перев. стр LF",
                "перев. стр CRLF",
                "перев. стр LFCR")),
            SettingsSerialConnectDeviceView(arrayListOf(
                "прием перев. стр CR",
                "прием перев. стр LF",
                "прием перев. стр CRLF",
                "прием перев. стр LFCR")),
            SettingsSerialConnectDeviceView(arrayListOf(
                "DTR нет",
                "DTR да")),
            SettingsSerialConnectDeviceView(arrayListOf(
                "RTS нет",
                "RTS да"))
        )
        val adapter = SettingsSerialConnectDeviceViewAdapter(this, settingsList)
        showElements.settingsRecyclerView.adapter = adapter

        // горизонтальное расположение элементов в скролинге настроек
        showElements.settingsRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false)
    }

    override fun onDestroy() {
        usb.onDestroy() // уничтожение обекта usb
        super.onDestroy()
    }

    // функция для кнопки подключения к дивайсу
    fun onClickButtonConnect(view: View) {
        if (!usb.checkConnectToDevice()) {

            val usbManager: UsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
            val deviceList = usbManager.deviceList.values

            if (deviceList.isNotEmpty()) {

                // заполнения массива именами подключенных дивайсов
                val nameDeviceList: ArrayList<String> = arrayListOf()
                for (device in deviceList) {
                    nameDeviceList.add(device.productName.toString())
                }

                showAlertDialogChoiceDevices(nameDeviceList)
            } else {
                showButtonConnection(false)
                showAlertDialog(getString(R.string.mainActivityText_NoneDevice))
            }
        } else {
            usb.onClear()
            showButtonConnection(false)
        }
    }
    // функция для кнопки отправки сообщения в серийный порт
    fun onClickButtonMoveToData(view: View) {
        val textIn: String = showElements.textInputDataForMoveToData.text.toString()
        if (textIn.isNotEmpty()) {
            showElements.textInputDataForMoveToData.setText("")

            usb.writeDevice(textIn)
        }
    }


    // функция для отображения статуса подключения к девайсу на кнопки
    override fun showButtonConnection(con: Boolean) {
        if (con) {
            showElements.buttonConnect.text = getString(R.string.mainActivityText_disconnect)
            showElements.buttonConnect.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.green))
        } else {
            showElements.buttonConnect.text = getString(R.string.mainActivityText_connect)
            showElements.buttonConnect.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.red))

            showElements.textDataTerm.text = ""
        }
    }
    // отображения имени подключенного девайса
    override fun showDeviceName(deviceName: String) {
        showElements.textDeviceName.text = deviceName
    }
    // вывод ошибок при работе с девасом
    override fun withdrawalsShow(msg: String) {
        showButtonConnection(false)
        showAlertDialog(msg)
    }
    // вывод полученых данных из серийного порта
    override fun printData(data: String) {
        val termText: String = showElements.textDataTerm.text.toString() + data
        showElements.textDataTerm.text = termText

        showTermTextBottom()
    }
    // подключения и регистрация широковещятельного приемника
    override fun connectToUsbDevice(device: UsbDevice) {
        val usbManager: UsbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        try {
            val permissionIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Для Android 12 (API уровень 31)
                PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(usb.ACTION_USB_PERMISSION),
                    PendingIntent.FLAG_MUTABLE)
            } else {
                // Для Android ниже 12
                PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(usb.ACTION_USB_PERMISSION),
                    0)
            }

            registerReceiver(usb.usbReceiver, IntentFilter(usb.ACTION_USB_PERMISSION))
            usbManager.requestPermission(device, permissionIntent)

        } catch (e: Exception) {
            showAlertDialog(getString(R.string.mainActivityText_ErrorConnect))
        }
    }


    // выборка к какому дивайсу подключиться
    private fun showAlertDialogChoiceDevices(list: ArrayList<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.mainActivityText_SubmitDevice))

        builder.setItems(list.toArray(arrayOfNulls<String>(list.size))) { _, which ->

            val usbManager: UsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
            val deviceList: HashMap<String, UsbDevice> = usbManager.deviceList

            try {
                val device: UsbDevice = deviceList.values.toList()[which]
                if (device.productName.toString() == list[which]) {
                    connectToUsbDevice(device)
                } else {
                    showAlertDialog(getString(R.string.mainActivityText_ExtractDevice))
                }

            } catch (e: IndexOutOfBoundsException) {
                showAlertDialog(getString(R.string.mainActivityText_ExtractDevice))
            }

        }

        val dialog = builder.create()
        dialog.show()
    }

    // вывод диалоговых окон с сообщениями пользователю
    private fun showAlertDialog(msg: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(msg)

        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    // прокрутка терминала вниз
    private fun showTermTextBottom() {
        showElements.scrollTermText.post {
            showElements.scrollTermText.fullScroll(View.FOCUS_DOWN)
        }
    }





}