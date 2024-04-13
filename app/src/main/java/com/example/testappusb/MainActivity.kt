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
import com.example.testappusb.adapters.AdaptersMainActivity.SaveTextCommandViewAdapter
import com.example.testappusb.adapters.AdaptersMainActivity.SettingsSerialConnectDeviceViewAdapter
import com.example.testappusb.databinding.ActivityMainBinding
import com.example.testappusb.model.recyclerModelForMainActivity.SaveTextCommandView
import com.example.testappusb.model.recyclerModelForMainActivity.SettingsSerialConnectDeviceView
import com.example.testappusb.settings.ComandsHintForTerm
import com.example.testappusb.usb.Usb
import com.example.testappusb.usb.UsbActivityInterface

//  SERIAL TERMENALL серийный терминалл
class MainActivity : AppCompatActivity(), UsbActivityInterface, ItemsButtonTextSet {


    companion object {
        const val TIMEOUT_TEXT_COMMAND_SAVE_UPDATE: Long = 200
    }

    private lateinit var showElements: ActivityMainBinding

    override val usb: Usb = Usb(this)

    private var flagWorkTextSaveCommands: Boolean = true
    private var curentTextForTermInput: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showElements = ActivityMainBinding.inflate(layoutInflater)
        setContentView(showElements.root)

        // добавления выборки с настроками в горизонтальный скролл
        val settingsList = arrayListOf(
            SettingsSerialConnectDeviceView("число бит", arrayListOf("8", "7")),
            SettingsSerialConnectDeviceView("скорость", arrayListOf(
                "300", "600", "1200", "2400", "4800",
                "9600", "19200", "38400", "57600", "115200")),
            SettingsSerialConnectDeviceView("четность", arrayListOf("None", "Even", "Odd")),
            SettingsSerialConnectDeviceView("стоп бит", arrayListOf("1", "2")),
            SettingsSerialConnectDeviceView("перев. стр", arrayListOf(
                "CR", "LF",
                "CRLF", "LFCR")),
            SettingsSerialConnectDeviceView("прием перев. стр", arrayListOf(
                "CR", "LF",
                "CRLF", "LFCR")),
            SettingsSerialConnectDeviceView("DTR", arrayListOf("нет", "да")),
            SettingsSerialConnectDeviceView("RTS", arrayListOf("нет", "да"))
        )
        val adapter = SettingsSerialConnectDeviceViewAdapter(this, settingsList)
        showElements.settingsRecyclerView.adapter = adapter

        // горизонтальное расположение элементов в скролинге настроек
        showElements.settingsRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false)
        showElements.historyScrollComandText.layoutManager = LinearLayoutManager(this)

        // получаем данные из файла с активными подсказками команд
        ComandsHintForTerm.loadFromFile(this)

        // поток для обновления подсказок команд
        Thread {

            while (flagWorkTextSaveCommands) {
                Thread.sleep(TIMEOUT_TEXT_COMMAND_SAVE_UPDATE)

                val startText: String = showElements.textInputDataForMoveToData.text.toString()
                // если что то изменилось в инпуте текста
                if (curentTextForTermInput != startText) {
                    var listSaveTextCommandView: ArrayList<SaveTextCommandView> = arrayListOf()

                    // отсартировака команд по началу команды введенной пользователем
                    if (startText.isNotEmpty()) {
                        val listHint: List<String> = ComandsHintForTerm.lisComand.filter {
                            it.startsWith(startText) && it != startText
                        }
                        listSaveTextCommandView = ArrayList(listHint.map {
                            SaveTextCommandView(it)
                        })
                    }

                    runOnUiThread {
                        val adapterSaveTextCommand = SaveTextCommandViewAdapter(this, listSaveTextCommandView)
                        showElements.historyScrollComandText.adapter = adapterSaveTextCommand
                        curentTextForTermInput = startText
                    }
                }

            }
        }.start()

    }

    override fun onRestart() {
        ComandsHintForTerm.loadFromFile(this)
        super.onRestart()
    }

    override fun onDestroy() {
        flagWorkTextSaveCommands = false
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

    // функция для кнопки с установкой пакета команд помошника
    fun onClickButtonMoveToSettings(view: View) {
        val i = Intent(this, SettingCommandHintActivity::class.java)
        usb.onClear()
        startActivity(i)
    }

    // функция для кнопки с тчисткой терминала
    fun onClickButtonClearTerm(view: View) {
        showElements.textDataTerm.text = ""
    }


    // функция для отображения статуса подключения к девайсу на кнопки
    override fun showButtonConnection(con: Boolean) {
        if (con) {
            showElements.textConnect.text = getString(R.string.mainActivityText_disconnect)
            /*showElements.buttonConnect.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.green))*/
        } else {
            showElements.textConnect.text = getString(R.string.mainActivityText_connect)
            /*showElements.buttonConnect.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.red))*/

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

    // установка текста в инпуте ввода команд
    override fun setTextFromButton(text: String) {
        showElements.textInputDataForMoveToData.setText(text)
    }


}