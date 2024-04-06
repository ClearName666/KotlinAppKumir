package com.example.testappusb

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.example.testappusb.settings.ConstUsbSettings
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import com.felhr.usbserial.UsbSerialInterface.UsbReadCallback
import java.io.IOException
import java.util.concurrent.Executors

class Usb(val context: Context) {

    val ACTION_USB_PERMISSION: String = "com.android.example.USB_PERMISSION"
    /*companion object {

        private const val TAG_APP: String = "TEST_LOG"



        const val USB_WRITE_TIMEOUT_MILLIS = 5000

        const val BUFFER_READ = 10240
        const val TIMEOUT_READ = 500

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
    }*/

    //private val dtr = false
    //private val rts = false
    private var lineFeed = "\r"
    private var lineFeedRead = "\r"

    var connection: UsbDeviceConnection? = null
    var deviceConnect: UsbDevice? = null
    var usbSerialDevice: UsbSerialDevice? = null


    val executorUsb = Executors.newSingleThreadExecutor()

    fun onSelectUumBit(numBit: Boolean) {
        ConstUsbSettings.numBit = numBit
        usbSerialDevice?.let {
            if (numBit) {
                usbSerialDevice?.setDataBits(UsbSerialInterface.DATA_BITS_8)
            } else {
                usbSerialDevice?.setDataBits(UsbSerialInterface.DATA_BITS_7)
            }
        }
    }

    fun onSerialSpeed(speedIndex: Int) {
        ConstUsbSettings.speedIndex = speedIndex
        val speedList: ArrayList<Int> =
            arrayListOf(300, 600, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200)
        usbSerialDevice?.let {
            if (speedList.size > speedIndex) {
                usbSerialDevice?.setBaudRate(speedList.get(speedIndex))
            }
        }
    }

    fun onSerialParity(parityIndex: Int) {
        ConstUsbSettings.parityIndex = parityIndex
        usbSerialDevice?.let {
            if (parityIndex == 0) {
                usbSerialDevice?.setParity(UsbSerialInterface.PARITY_NONE)
            } else if (parityIndex == 1) {
                usbSerialDevice?.setParity(UsbSerialInterface.PARITY_EVEN)
            } else if (parityIndex == 2) {
                usbSerialDevice?.setParity(UsbSerialInterface.PARITY_ODD)
            } else {

            }
        }
    }

    fun onSerialStopBits(stopBitsIndex: Int) {
        ConstUsbSettings.stopBit = stopBitsIndex
        usbSerialDevice?.let {
            if (stopBitsIndex == 0) {
                usbSerialDevice?.setStopBits(UsbSerialInterface.STOP_BITS_1)
            } else {
                usbSerialDevice?.setStopBits(UsbSerialInterface.STOP_BITS_2)
            }
        }
    }
    fun onSerialLineFeed(lineFeedIndex: Int) {
        if (lineFeedIndex == 0) {
            lineFeed = "\r"
        } else if (lineFeedIndex == 1) {
            lineFeed = "\n"
        } else if (lineFeedIndex == 2){
            lineFeed = "\r\n"
        } else {
            lineFeed = "\n\r"
        }
    }
    fun onSerialLineFeedRead(lineFeedIndex: Int) {
        if (lineFeedIndex == 0) {
            lineFeedRead = "\r"
        } else if (lineFeedIndex == 1) {
            lineFeedRead = "\n"
        } else if (lineFeedIndex == 2){
            lineFeedRead = "\r\n"
        } else {
            lineFeedRead = "\n\r"
        }
    }
    fun onSerialDTR(IndexDTR: Int) {
        ConstUsbSettings.dtr = IndexDTR
        usbSerialDevice?.let {
            if (IndexDTR == 0) {
                usbSerialDevice?.setDTR(false)
            } else {
                usbSerialDevice?.setDTR(true)
            }
        }
    }
    fun onSerialRTS(IndexRTS: Int) {
        ConstUsbSettings.rts = IndexRTS
        usbSerialDevice?.let {
            if (IndexRTS == 0) {
                usbSerialDevice?.setRTS(false)
            } else {
                usbSerialDevice?.setRTS(true)
            }
        }
    }
    fun onStartSerialSetting() {
        onSelectUumBit(ConstUsbSettings.numBit)
        onSerialSpeed(ConstUsbSettings.speedIndex)
        onSerialParity(ConstUsbSettings.parityIndex)
        onSerialStopBits(ConstUsbSettings.stopBit)
    }

    fun checkConnectToDevice(): Boolean {
        if (context is UsbActivityInterface) {
            val usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            val devises: HashMap<String, UsbDevice> = usbManager.deviceList
            for (device in devises) {
                if (device.value.deviceName == deviceConnect?.deviceName) {
                    return true
                }
            }
        }
        onDestroy()
        return false
    }
    fun onDestroy() {
        connection?.close()
        connection = null
        usbSerialDevice?.close()
        usbSerialDevice = null
        deviceConnect = null
        if (context is UsbActivityInterface) {
            (context as Activity).runOnUiThread {
                context.showButtonConnection(false)
                context.showDeviceName("")
            }
        }
    }

    fun useToConnectToDivice(message: String) {
        executorUsb.execute {
            try {
                if (usbSerialDevice == null) {
                    if (context is UsbActivityInterface) {
                        (context as Activity).runOnUiThread {
                            context.withdrawalsShow("Нету подключения, воспользуйтесь кнопкой ПОДКЛЮЧИТЬСЯ")
                        }
                    }

                } else {
                    val bytesToSend = (message + lineFeed).toByteArray()
                    usbSerialDevice?.write(bytesToSend)
                    printUIThread("input>>>" + message + lineFeed)
                }
            } catch (e: Exception) {
                if (context is UsbActivityInterface) {
                    (context as Activity).runOnUiThread {
                        context.withdrawalsShow("В разрешении на устройство было отказано, подключите устройство, что бы разрешить и взаимодействовать с ним")
                    }
                }
            }
        }
    }

    private fun printUIThread(msg: String) {
        if (context is UsbActivityInterface) {
            (context as Activity).runOnUiThread {
                context.printData(msg)
            }
        }
    }

    val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_USB_PERMISSION) {
                usbSerialDevice?.close()
                val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager?
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (context is UsbActivityInterface) {
                        context.showButtonConnection(true)
                    }
                    device?.apply {

                        connection = usbManager?.openDevice(device)
                        if (connection != null) {
                            try {
                                usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection)
                                usbSerialDevice?.open()
                                deviceConnect = device
                                onStartSerialSetting()

                                (context as Activity).runOnUiThread {
                                    if (context is UsbActivityInterface) {
                                        context.showDeviceName(device.productName.toString())
                                    }
                                }
                                Thread {
                                    if (context is UsbActivityInterface) {
                                        while (checkConnectToDevice()) {}
                                    }
                                }.start()
                                usbSerialDevice?.let {
                                    if (it.open()) {
                                        val readCallback = UsbReadCallback { bytes ->
                                            val receivedData: String = String(bytes, Charsets.UTF_8)
                                            printUIThread("output>>>" + receivedData + lineFeedRead)
                                        }
                                        it.read(readCallback)
                                    }
                                }
                            } catch (e: IOException) {
                                if (context is UsbActivityInterface) {
                                    (context as Activity).runOnUiThread {
                                        context.withdrawalsShow("Произошла ошибка при попытки подключения")
                                    }
                                }
                                onDestroy()
                            }

                        }
                    }
                }
            }
        }
    }
}