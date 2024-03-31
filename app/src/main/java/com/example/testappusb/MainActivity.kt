package com.example.testappusb

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testappusb.adapters.SettingsSerialConnectDeviceViewAdapter
import com.example.testappusb.databinding.ActivityMainBinding
import com.example.testappusb.model.SettingsSerialConnectDeviceView
import com.example.testappusb.settings.ConstUsbSettings
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface

class MainActivity : AppCompatActivity() {

    lateinit var showElements: ActivityMainBinding

    companion object {
        const val ACTION_USB_PERMISSION :String = "com.android.example.USB_PERMISSION"
        private const val TAG_APP :String = "TEST_LOG"

        const val USB_WRITE_TIMEOUT_MILLIS = 5000

        /*
         * Configuration Request Types
         */
        const val REQTYPE_HOST_TO_DEVICE = 0x41
        const val REQTYPE_DEVICE_TO_HOST = 0xc1

        /*
         * Configuration Request Codes
         */
        const val SILABSER_IFC_ENABLE_REQUEST_CODE = 0x00
        const val SILABSER_SET_LINE_CTL_REQUEST_CODE = 0x03
        const val SILABSER_SET_BREAK_REQUEST_CODE = 0x05
        const val SILABSER_SET_MHS_REQUEST_CODE = 0x07
        const val SILABSER_SET_BAUDRATE = 0x1E
        const val SILABSER_FLUSH_REQUEST_CODE = 0x12
        const val SILABSER_GET_MDMSTS_REQUEST_CODE = 0x08

        const val FLUSH_READ_CODE = 0x0a
        const val FLUSH_WRITE_CODE = 0x05

        /*
         * SILABSER_IFC_ENABLE_REQUEST_CODE
         */
        const val UART_ENABLE = 0x0001
        const val UART_DISABLE = 0x0000

        /*
         * SILABSER_SET_MHS_REQUEST_CODE
         */
        const val DTR_ENABLE = 0x101
        const val DTR_DISABLE = 0x100
        const val RTS_ENABLE = 0x202
        const val RTS_DISABLE = 0x200

        /*
         * SILABSER_GET_MDMSTS_REQUEST_CODE
         */
        const val STATUS_CTS = 0x10
        const val STATUS_DSR = 0x20
        const val STATUS_RI = 0x40
        const val STATUS_CD = 0x80
    }

    private val dtr = false
    private val rts = false

    private var connection: UsbDeviceConnection? = null
    private var epIN: UsbEndpoint? = null
    private var epOUT: UsbEndpoint? = null
    private var message: String = ""


    var usbSerialDevice: UsbSerialDevice? = null


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
    /*override fun onPause() {
        super.onPause()
        unregisterReceiver(usbReceiver)
        connection?.close()
    }*/
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
        connection?.close()
        connection = null
    }

    fun onClickButtonConnect(view: View) {
        connectToUsbDevice()
    }
    fun onClickButtonMoveToData(view: View) {
        val textIn: String = showElements.textInputDataForMoveToData.text.toString()
        if (textIn.length != 0) {
            message = "input>>>" + textIn + "\n"
            showElements.textInputDataForMoveToData.setText("")
            useToConnectToDivice()
        }
    }

    fun onSelectUumBit(numBit: Boolean) {
        ConstUsbSettings.numBit = numBit
        val device: UsbDevice? = getDeviseCon()
        device?.let {
            Thread({
                usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection)
                usbSerialDevice?.open()
                if (numBit) {
                    usbSerialDevice?.setDataBits(UsbSerialInterface.DATA_BITS_8)
                } else {
                    usbSerialDevice?.setDataBits(UsbSerialInterface.DATA_BITS_7)
                }
            }).start()
        }
    }
    fun onSerialSpeed(speedIndex: Int) {
        ConstUsbSettings.speedIndex = speedIndex
        val device: UsbDevice? = getDeviseCon()
        val speedList: ArrayList<Int> = arrayListOf(300, 600, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200)
        device?.let {
            Thread({
                usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection)
                usbSerialDevice?.open()
                if (speedList.size > speedIndex) {
                    usbSerialDevice?.setBaudRate(speedList.get(speedIndex))
                }
            }).start()
        }
    }
    fun onSerialParity(parityIndex: Int) {
        ConstUsbSettings.parityIndex = parityIndex
        val device: UsbDevice? = getDeviseCon()
        device?.let {
            Thread({
                usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection)
                usbSerialDevice?.open()
                if (parityIndex == 0) {
                    usbSerialDevice?.setParity(UsbSerialInterface.PARITY_NONE)
                } else if (parityIndex == 1){
                    usbSerialDevice?.setParity(UsbSerialInterface.PARITY_EVEN)
                } else if (parityIndex == 2) {
                    usbSerialDevice?.setParity(UsbSerialInterface.PARITY_ODD)
                }
            }).start()
        }
    }
    fun onSerialStopBits(stopBitsIndex: Int) {
        ConstUsbSettings.stopBit = stopBitsIndex
        val device: UsbDevice? = getDeviseCon()
        device?.let {
            Thread({
                usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection)
                usbSerialDevice?.open()
                if (stopBitsIndex == 0) {
                    usbSerialDevice?.setStopBits(UsbSerialInterface.STOP_BITS_1)
                } else {
                    usbSerialDevice?.setStopBits(UsbSerialInterface.STOP_BITS_2)
                }
            }).start()

        }
    }

    private fun getDeviseCon(): UsbDevice? {
        val usbManager: UsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = usbManager.deviceList
        val device = deviceList.values.firstOrNull()
        return device
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

    private fun connectToUsbDevice(){
        val usbManager: UsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = usbManager.deviceList
        val device = deviceList.values.firstOrNull()
        if (device == null) {
            showButtonConnection(false)
            showAlertDialog("Устройство не обнаружено, подключите устройство")
        }

        device?.let {
            val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            registerReceiver(usbReceiver, IntentFilter(ACTION_USB_PERMISSION))
            usbManager.requestPermission(device, permissionIntent)
        }
    }
    private fun useToConnectToDivice() {
        try {
            val usbManager: UsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
            val deviceList: HashMap<String, UsbDevice> = usbManager.deviceList
            if (deviceList.size != 0) {
                if (connection == null) {
                    showButtonConnection(false)
                    showAlertDialog("Нету подключения, воспользуйтесь кнопкой ПОДКЛЮЧИТЬСЯ")
                } else {
                    Thread ({
                        val bytesToSend = message.toByteArray()
                        connection!!.bulkTransfer(epOUT, bytesToSend, bytesToSend.size, USB_WRITE_TIMEOUT_MILLIS)
                        connection?.let {
                            runOnUiThread() {
                                val textTerm: String = showElements.textDataTerm.text.toString()
                                showElements.textDataTerm.text = textTerm + message
                            }
                        }

                    }).start()
                }
            } else {
                showButtonConnection(false)
                showAlertDialog("Устройство не обнаружено, подключите устройство")
                message = ""
            }
        } catch (e: Exception) {
            showAlertDialog("В разрешении на устройство было отказано, подключите устройство, что бы разрешить и взаимодействовать с ним")
            message = ""
        }


    }
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_USB_PERMISSION) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                val usbManager: UsbManager = applicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
                var tmp: String
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    //textDataTerm.text = "Подключение произошло успешно!"
                    device?.apply {
                        Thread(Runnable {
                            runOnUiThread() {
                                showButtonConnection(true)
                            }

                            connection = usbManager.openDevice(device)

                            if (connection != null) {
                                val baudRate = 115200
                                val data = byteArrayOf(
                                    (baudRate and 0xff).toByte(),
                                    (baudRate shr 8 and 0xff).toByte(),
                                    (baudRate shr 16 and 0xff).toByte(),
                                    (baudRate shr 24 and 0xff).toByte()
                                )

                                var ret: Int = connection!!.controlTransfer(REQTYPE_HOST_TO_DEVICE, SILABSER_SET_BAUDRATE, 0, 0, data, data.size, USB_WRITE_TIMEOUT_MILLIS)
                                if (ret < 0) {
                                    Log.e(TAG_APP, "Error setting baud rate")
                                }

                                ret = connection!!.controlTransfer(REQTYPE_HOST_TO_DEVICE, SILABSER_IFC_ENABLE_REQUEST_CODE, UART_ENABLE, 0, null, 0, USB_WRITE_TIMEOUT_MILLIS)
                                if (ret < 0) {
                                    Log.e(TAG_APP, "Error enabling UART")
                                }

                                if (connection!!.claimInterface(device.getInterface(0), true)) {
                                    val usbIf: UsbInterface? = device.getInterface(0)
                                    if (usbIf != null) {
                                        for (i in 0 until usbIf.endpointCount) {
                                            val ep = usbIf.getEndpoint(i)
                                            if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                                if (ep.direction == UsbConstants.USB_DIR_IN) {
                                                    epIN = ep
                                                } else {
                                                    epOUT = ep
                                                }
                                            }
                                        }

                                        val bytesToSend = "Подключение произошло успешно".toByteArray()
                                        connection!!.bulkTransfer(epOUT, bytesToSend, bytesToSend.size, USB_WRITE_TIMEOUT_MILLIS)
                                        onSelectUumBit(ConstUsbSettings.numBit)
                                        onSerialSpeed(ConstUsbSettings.speedIndex)
                                        onSerialParity(ConstUsbSettings.parityIndex)
                                        onSerialStopBits(ConstUsbSettings.stopBit)
                                        runOnUiThread() {
                                            tmp = showElements.textDataTerm.text.toString() + "\n\nПодключение произошло успешно\n"
                                            showElements.textDataTerm.text = tmp
                                        }

                                    }
                                    else {
                                        Log.e(TAG_APP, "Failed to get device interface")
                                        runOnUiThread() {
                                            showAlertDialog("Не удалось получить интерфейс устройства")
                                            tmp = showElements.textDataTerm.text.toString() + "\n\nНе удалось получить интерфейс устройства"
                                            showElements.textDataTerm.text = tmp
                                        }
                                    }

                                }
                                else {
                                    runOnUiThread() {
                                        showAlertDialog("Не удалось захватить интерфейс устройства для обмена данными")
                                        tmp = showElements.textDataTerm.text.toString() + "\n\nНе удалось захватить интерфейс устройства для обмена данными"
                                        showElements.textDataTerm.text = tmp
                                    }
                                }
                            } else {
                                runOnUiThread() {
                                    showAlertDialog("Не удалось соединится с устройством")
                                    tmp = showElements.textDataTerm.text.toString() + "\n\nНе удалось соединится с устройством"
                                    showElements.textDataTerm.text = tmp
                                }
                            }
                        }).start()
                    }
                } else {
                    showAlertDialog("В разрешении на устройство было отказано")
                    Log.d(TAG, "permission denied for device $device")
                    tmp = showElements.textDataTerm.text.toString() + "\n\nВ разрешении на устройство было отказано"
                    showElements.textDataTerm.text = tmp
                }
            }
        }
    }


}