package com.example.testappusb.usb

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.provider.Settings.Global.getString
import android.util.Log

import com.example.testappusb.R
import com.example.testappusb.settings.ConstUsbSettings
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import com.felhr.usbserial.UsbSerialInterface.UsbReadCallback
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Usb(private val context: Context) {

    val ACTION_USB_PERMISSION: String = "com.android.example.USB_PERMISSION"
    companion object {
        const val TIMEOUT_CHECK_CONNECT: Long = 100 // таймаут для проверки подключения
        const val TIMEOUT_MOVE_AT: Long = 3000
        const val TIMEOUT_IGNORE_AT: Long = 30

        val speedList: ArrayList<Int> = arrayListOf(
            300, 600, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200) // скорости в бодах

    }

    // переводы строк
    private var lineFeed = "\r"
    private var lineFeedRead = "\r"


    private var connection: UsbDeviceConnection? = null
    private var usbSerialDevice: UsbSerialDevice? = null
    private var deviceUsb: UsbDevice? = null

    // поток для usb
    private val executorUsb: ExecutorService = Executors.newSingleThreadExecutor()


    private var flagAtCommand: Boolean = true
    private var flagIgnorRead: Boolean = false

    // настрока сериал порта <ЧИСЛО БИТ>
    fun onSelectUumBit(numBit: Boolean) {
        ConstUsbSettings.numBit = numBit

        usbSerialDevice?.let {
            when (numBit) {
                true -> usbSerialDevice?.setDataBits(UsbSerialInterface.DATA_BITS_8)
                false -> usbSerialDevice?.setDataBits(UsbSerialInterface.DATA_BITS_7)
            }
        }
    }

    // настрока сериал порта <СКОРОСТЬ В БОДАХ>
    fun onSerialSpeed(speedIndex: Int) {
        ConstUsbSettings.speedIndex = speedIndex

        usbSerialDevice?.let {
            if (speedList.size > speedIndex) {
                usbSerialDevice?.setBaudRate(speedList[speedIndex])
            }
        }
    }

    // настрока сериал порта <ЧЕТНОСТЬ>
    fun onSerialParity(parityIndex: Int) {
        ConstUsbSettings.parityIndex = parityIndex

        usbSerialDevice?.let {
            when (parityIndex) {
                0 -> usbSerialDevice?.setParity(UsbSerialInterface.PARITY_NONE)
                1 -> usbSerialDevice?.setParity(UsbSerialInterface.PARITY_EVEN)
                2 -> usbSerialDevice?.setParity(UsbSerialInterface.PARITY_ODD)
                else -> {}
            }
        }
    }

    // настрока сериал порта <СТОП БИТЫ>
    fun onSerialStopBits(stopBitsIndex: Int) {
        ConstUsbSettings.stopBit = stopBitsIndex

        usbSerialDevice?.let {
            when (stopBitsIndex) {
                0 -> usbSerialDevice?.setStopBits(UsbSerialInterface.STOP_BITS_1)
                1 -> usbSerialDevice?.setStopBits(UsbSerialInterface.STOP_BITS_2)
                else -> {}
            }
        }
    }

    // настрока перевода строки при отправки данных
    fun onSerialLineFeed(lineFeedIndex: Int) {
        when (lineFeedIndex) {
            0 -> lineFeed = "\r"
            1 -> lineFeed = "\n"
            2 -> lineFeed = "\r\n"
            3 -> lineFeed = "\n\r"
            else -> {}
        }
    }

    // настрока перевода строки при получении данных
    fun onSerialLineFeedRead(lineFeedIndex: Int) {
        when (lineFeedIndex) {
            0 -> lineFeedRead = "\r"
            1 -> lineFeedRead = "\n"
            2 -> lineFeedRead = "\r\n"
            3 -> lineFeedRead = "\n\r"
        }
    }

    // настрока сериал порта <DTR>
    fun onSerialDTR(indexDTR: Int) {
        ConstUsbSettings.dtr = indexDTR

        usbSerialDevice?.let {
            when (indexDTR) {
                0 -> usbSerialDevice?.setDTR(false)
                1 -> usbSerialDevice?.setDTR(true)
                else -> {}
            }
        }
    }

    // настрока сериал порта <RTS>
    fun onSerialRTS(indexRTS: Int) {
        ConstUsbSettings.rts = indexRTS

        usbSerialDevice?.let {
            when (indexRTS) {
                0 -> usbSerialDevice?.setRTS(false)
                1 -> usbSerialDevice?.setRTS(true)
                else -> {}
            }
        }
    }

    // настройка серийного порта при подключении
    fun onStartSerialSetting() {
        onSelectUumBit(ConstUsbSettings.numBit)
        onSerialSpeed(ConstUsbSettings.speedIndex)
        onSerialParity(ConstUsbSettings.parityIndex)
        onSerialStopBits(ConstUsbSettings.stopBit)
        onSerialRTS(ConstUsbSettings.rts)
        onSerialDTR(ConstUsbSettings.dtr)
    }


    // проверка подклюения девайса к устройству
    fun checkConnectToDevice(): Boolean {
        if (context is UsbActivityInterface) {
            val usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            val devises: HashMap<String, UsbDevice> = usbManager.deviceList

            for (device in devises) {
                if (device.value.deviceName == deviceUsb?.deviceName) {
                    return true
                }
            }
        }
        onClear()
        return false
    }
    // очищение ресурсов после отклчения диваса
    fun onClear() {
        flagAtCommand = false
        connection?.close()
        connection = null
        usbSerialDevice?.close()
        usbSerialDevice = null
        deviceUsb = null
        if (context is UsbActivityInterface) {
            (context as Activity).runOnUiThread {
                context.showButtonConnection(false)
                context.showDeviceName("")
            }
        }
    }
    // удаление всего что связано с usb
    fun onDestroy() {
        context.unregisterReceiver(usbReceiver)
        onClear()
        executorUsb.shutdown()
    }

    // отправка данных в сериал порт
    fun writeDevice(message: String, flagPrint: Boolean = true) {
        executorUsb.execute {
            try {
                if (usbSerialDevice == null) {
                    printWithdrawalsShow(context.getString(R.string.Usb_NoneConnect))
                } else {
                    val bytesToSend = (message + lineFeed).toByteArray()
                    usbSerialDevice?.write(bytesToSend)

                    if (flagPrint) {
                        printUIThread("input>>>$message$lineFeed")
                    }

                }
            } catch (e: Exception) {
                printWithdrawalsShow("${context.getString(R.string.Usb_ErrorWriteData)} ${e.message}")
            }
        }
    }

    // отправка полученных и отправленых данных в ui радительский поток
    private fun printUIThread(msg: String) {
        if (context is UsbActivityInterface) {
            (context as Activity).runOnUiThread {
                context.printData(msg)
            }
        }
    }
    // отправка сообщений в ui радительский поток
    private fun printWithdrawalsShow(msg: String) {
        if (context is UsbActivityInterface) {
            (context as Activity).runOnUiThread {
                context.withdrawalsShow(msg)
            }
        }
    }

    // регистрация широковещятельного приемника
    val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_USB_PERMISSION) {

                val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager?
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                // если есть разрешение на использования устройства
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    device?.apply {
                        connection = usbManager?.openDevice(device)
                        if (connection != null) {
                            try {
                                usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(
                                    device, connection)
                                usbSerialDevice?.open()

                                (context as Activity).runOnUiThread {
                                    if (context is UsbActivityInterface) {
                                        context.showDeviceName(device.productName.toString())
                                    }
                                }



                                usbSerialDevice?.let {
                                    if (it.open()) {
                                        val readCallback = UsbReadCallback { bytes ->
                                            if (!flagIgnorRead) {
                                                printUIThread("output>>>" + String(bytes, Charsets.UTF_8))
                                            }
                                        }

                                        it.read(readCallback)
                                    }
                                }
                                if (context is UsbActivityInterface) {
                                    context.showButtonConnection(true)
                                }

                                onStartSerialSetting()
                                deviceUsb = device

                                // поток для отправки в фоновом режиме at команды
                                Thread {
                                    flagAtCommand = true
                                    while (flagAtCommand) {
                                        Thread.sleep(TIMEOUT_MOVE_AT)
                                        if (checkConnectToDevice() && flagAtCommand) {
                                            writeDevice(context.getString(R.string.at), false)

                                            flagIgnorRead = true
                                            Thread.sleep(TIMEOUT_IGNORE_AT)
                                            flagIgnorRead = false
                                        }
                                    }
                                }.start()

                                // постоянная проверка подключения к устройству
                                Thread {
                                    if (context is UsbActivityInterface) {
                                        while (checkConnectToDevice()) {
                                            Thread.sleep(TIMEOUT_CHECK_CONNECT)
                                        }
                                    }
                                }.start()

                            } catch (e: IOException) {
                                printWithdrawalsShow(context.getString(R.string.Usb_ErrorConnect))

                                onClear()
                            }

                        }
                    }
                }
            }
        }
    }
}