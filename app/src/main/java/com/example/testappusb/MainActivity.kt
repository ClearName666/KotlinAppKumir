package com.example.testappusb

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

import android.os.Build
import android.os.Bundle
import android.text.BoringLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.View
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testappusb.adapters.AdaptersMainActivity.SaveTextCommandViewAdapter
import com.example.testappusb.adapters.AdaptersMainActivity.SettingsSerialConnectDeviceViewAdapter
import com.example.testappusb.databinding.ActivityMainBinding
import com.example.testappusb.gestures.SwipeGestureDetector
import com.example.testappusb.gestures.SwipeGestureDetectorIntarface
import com.example.testappusb.model.recyclerModelForMainActivity.SaveTextCommandView
import com.example.testappusb.model.recyclerModelForMainActivity.SettingsSerialConnectDeviceView
import com.example.testappusb.settings.ComandsHintForTerm
import com.example.testappusb.usb.Usb
import com.example.testappusb.usb.UsbActivityInterface

//  SERIAL TERMENALL серийный терминалл
class MainActivity : AppCompatActivity(), UsbActivityInterface, ItemsButtonTextSet, SwipeGestureDetectorIntarface {

    private lateinit var showElements: ActivityMainBinding

    override val usb: Usb = Usb(this)

    private var flagWorkTextSaveCommands: Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showElements = ActivityMainBinding.inflate(layoutInflater)
        setContentView(showElements.root)

        // добавления выборки с настроками в горизонтальный скролл
        val settingsList = arrayListOf(
            SettingsSerialConnectDeviceView(
                getString(R.string.bit_number),
                arrayListOf(
                    getString(R.string.eight),
                    getString(R.string.seven)
                )
            ),
            SettingsSerialConnectDeviceView(
                getString(R.string.speed),
                arrayListOf(
                    getString(R.string.speed_300),
                    getString(R.string.speed_600),
                    getString(R.string.speed_1200),
                    getString(R.string.speed_2400),
                    getString(R.string.speed_4800),
                    getString(R.string.speed_9600),
                    getString(R.string.speed_19200),
                    getString(R.string.speed_38400),
                    getString(R.string.speed_57600),
                    getString(R.string.speed_115200)
                )
            ),
            SettingsSerialConnectDeviceView(
                getString(R.string.parity),
                arrayListOf(
                    getString(R.string.none),
                    getString(R.string.even),
                    getString(R.string.odd)
                )
            ),
            SettingsSerialConnectDeviceView(
                getString(R.string.stop_bits),
                arrayListOf(
                    getString(R.string.one),
                    getString(R.string.two)
                )
            ),
            SettingsSerialConnectDeviceView(
                getString(R.string.line_end_translation),
                arrayListOf(
                    getString(R.string.cr),
                    getString(R.string.lf),
                    getString(R.string.crlf),
                    getString(R.string.lfcr)
                )
            ),
            SettingsSerialConnectDeviceView(
                getString(R.string.receive_line_end_translation),
                arrayListOf(
                    getString(R.string.cr),
                    getString(R.string.lf),
                    getString(R.string.crlf),
                    getString(R.string.lfcr)
                )
            ),
            SettingsSerialConnectDeviceView(
                getString(R.string.dtr),
                arrayListOf(
                    getString(R.string.no),
                    getString(R.string.yes)
                )
            ),
            SettingsSerialConnectDeviceView(
                getString(R.string.rts),
                arrayListOf(
                    getString(R.string.no),
                    getString(R.string.yes)
                )
            ),
            SettingsSerialConnectDeviceView(
                getString(R.string.flow),
                arrayListOf(
                    getString(R.string.FLOW_CONTROL_OFF),
                    getString(R.string.FLOW_CONTROL_RTS_CTS),
                    getString(R.string.FLOW_CONTROL_DSR_DTR)
                )
            )
        )
        val adapter = SettingsSerialConnectDeviceViewAdapter(this, settingsList)
        showElements.settingsRecyclerView.adapter = adapter

        // горизонтальное расположение элементов в скролинге настроек
        showElements.settingsRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false)
        showElements.historyScrollComandText.layoutManager = LinearLayoutManager(this)

        // очищение по зажатию
        showElements.textDataTerm.setOnLongClickListener {
            showAlertDialogClearTerm()
            true
        }

        // закрытие настроек при клики в пустоту или вне настроек
        showElements.SettingsLayontExit.setOnClickListener {
            showElements.SettingsLayontExit.visibility = View.GONE
        }

        // настрока swich отправки at команд
        showElements.switchAtCommand.setOnCheckedChangeListener {_, isChecked ->
            usb.flagAtCommandYesNo = isChecked
        }



        // свайпер для перехода с свода коман на историю
        val gestureDetector = GestureDetector(this, SwipeGestureDetector(this))
        showElements.swipeHintCom.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        // получаем данные из файла с активными подсказками команд
        ComandsHintForTerm.loadFromFile(this, ComandsHintForTerm.fileNameCommands)
        // история команд
        ComandsHintForTerm.loadFromFile(this, ComandsHintForTerm.fileNameCommandsHistory)

        // слущатель изменения текста в inputText
        showElements.textInputDataForMoveToData.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                updateAdapterHintsCommands()
            }
        })
    }

    /*override fun onRestart() {
        ComandsHintForTerm.loadFromFile(this, ComandsHintForTerm.fileNameCommands)
        ComandsHintForTerm.loadFromFile(this, ComandsHintForTerm.fileNameCommandsHistory)
        super.onRestart()
    }*/

    override fun onDestroy() {
        flagWorkTextSaveCommands = false
        ComandsHintForTerm.saveToFile(this, ComandsHintForTerm.fileNameCommandsHistory)
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
            if (usb.writeDevice(textIn)) {
                // сохраниения истори команд
                ComandsHintForTerm.lisComandHistory.add(
                    showElements.textInputDataForMoveToData.text.toString()
                )

                showElements.textInputDataForMoveToData.setText("")
            }
        }
    }

    // функция для кнопки с установкой пакета команд помошника
    fun onClickButtonMoveToSettings(view: View) {
        val i = Intent(this, SettingCommandHintActivity::class.java)
        i.getStringExtra("flag")
        //usb.onClear()
        startActivityForResult(i, RESULT_OK)
    }

    // функция для кнопки с тчисткой терминала
    fun onClickButtonClearTerm(view: View) {
        clearTerm()
    }
    fun clearTerm() {
        showElements.textDataTerm.text = ""
        showElements.SettingsLayontExit.visibility = View.GONE
    }

    // функция для очищения textInput
    fun onClickButtonCleartextInput(view: View) {
        showElements.textInputDataForMoveToData.setText("")
    }

    // открытие настроек
    fun onClickSettingsView(view: View) {
        showElements.SettingsLayontExit.visibility = View.VISIBLE
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
    override fun withdrawalsShow(msg: String, notshowdis: Boolean) {
        showButtonConnection(notshowdis)
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

    // метод для отображения dsr и cts
    override fun printDSR_CTS(dsr: Int, cts: Int) {

        when(dsr) {
            0 -> showElements.imageDSR.setImageResource(R.drawable.noneflag)
            1 -> showElements.imageDSR.setImageResource(R.drawable.redflag)
            2 -> showElements.imageDSR.setImageResource(R.drawable.greenflag)
        }

        when(cts) {
            0 -> showElements.imageCTS.setImageResource(R.drawable.noneflag)
            1 -> showElements.imageCTS.setImageResource(R.drawable.redflag)
            2 -> showElements.imageCTS.setImageResource(R.drawable.greenflag)
        }
    }

    // функция при отключении от дивайс насильно
    override fun disconnected() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.distonnected))
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create()
        builder.show()
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

    // диалоговое окно для вывод предупреждения о том что произодет отчистка терминла
    private fun showAlertDialogClearTerm() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.okClear))

        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
            clearTerm()
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    // вывод диалоговых окон с сообщениями пользователю
    private fun showAlertDialog(msg: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(msg)

        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
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

        // курсор в конец
        showElements.textInputDataForMoveToData.text?.let {
            showElements.textInputDataForMoveToData.setSelection(
                it.length
            )
        }
    }

    // метод для обнавления адаптера подсказок
    private fun updateAdapterHintsCommands() {

        // если находимся в режиме всех команд то обновляем адаптер
        val startText: String = showElements.textInputDataForMoveToData.text.toString()
        var listSaveTextCommandView: ArrayList<SaveTextCommandView> = arrayListOf()

        // отсартировака команд по совпадениям команды введенной пользователем
        if (startText.isNotEmpty()) {
            val listHint: List<String> = ComandsHintForTerm.lisComand.filter {
                it.contains(startText) && it != startText
            }
            listSaveTextCommandView = ArrayList(listHint.map {
                SaveTextCommandView(it)
            })
        }

        // если нечего нет то элемент не виден и не занимает место
        if (listSaveTextCommandView.size == 0) {
            showElements.historyScrollComandText.visibility = View.GONE
            showElements.swipeHintCom.visibility = View.GONE

        } else {
            showElements.swipeHintCom.visibility = View.VISIBLE
            showElements.historyScrollComandText.visibility = View.VISIBLE
            val adapterSaveTextCommand = SaveTextCommandViewAdapter(
                this@MainActivity,
                listSaveTextCommandView
            )
            showElements.historyScrollComandText.adapter = adapterSaveTextCommand
        }
    }

    // метод обрабоотки свайпа
    override fun onSwipeAction(flagDirection: Boolean) {
        if (!flagDirection && ComandsHintForTerm.lisComandHistory.isNotEmpty()) { // если свайп в права
            val listSaveTextCommandView: ArrayList<SaveTextCommandView> =
                ArrayList(ComandsHintForTerm.lisComandHistory.map {
                    SaveTextCommandView(it)
                })

            // если нечего нет то элемент не виден и не занимает место
            if (listSaveTextCommandView.size == 0) {
                showElements.historyScrollComandText.visibility = View.GONE
                showElements.swipeHintCom.visibility = View.GONE
            } else {
                showElements.swipeHintCom.visibility = View.VISIBLE
                showElements.historyScrollComandText.visibility = View.VISIBLE
                val adapterSaveTextCommand = SaveTextCommandViewAdapter(
                    this@MainActivity,
                    listSaveTextCommandView
                )
                showElements.historyScrollComandText.adapter = adapterSaveTextCommand
            }
        } else {
            updateAdapterHintsCommands()
        }
    }
}