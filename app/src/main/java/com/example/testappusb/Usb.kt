package com.example.testappusb

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.util.Log
import com.example.testappusb.settings.ConstUsbSettings
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import java.io.IOException

class Usb(val context: Context) {

    val ACTION_USB_PERMISSION: String = "com.android.example.USB_PERMISSION"
    companion object {

        private const val TAG_APP: String = "TEST_LOG"



        const val USB_WRITE_TIMEOUT_MILLIS = 5000

        const val BUFFER_READ = 4096
        const val TIMEOUT_READ = 5

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
    private var lineFeed = "\n"

    var connection: UsbDeviceConnection? = null
    var deviceConnect: UsbDevice? = null
    private var epIN: UsbEndpoint? = null
    private var epOUT: UsbEndpoint? = null
    var usbSerialDevice: UsbSerialDevice? = null

    var flagRead: Boolean = false

    fun onSelectUumBit(numBit: Boolean) {
        ConstUsbSettings.numBit = numBit
        deviceConnect?.let {
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
        deviceConnect?.let {
            if (speedList.size > speedIndex) {
                usbSerialDevice?.setBaudRate(speedList.get(speedIndex))
            }
        }
    }

    fun onSerialParity(parityIndex: Int) {
        ConstUsbSettings.parityIndex = parityIndex
        deviceConnect?.let {
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
        deviceConnect?.let {
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
        } else {
            lineFeed = "\r\n"
        }
    }


    fun checkConnectToDevice(): Boolean {
        if (context is MainActivity) {
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
        flagRead = false
        connection?.close()
        connection = null
        usbSerialDevice?.close()
        usbSerialDevice = null
        deviceConnect = null
        (context as Activity).runOnUiThread {
            if (context is MainActivity) {
                context.showDeviceName("")
            }
        }
    }

    fun useToConnectToDivice(message: String): String {
        try {
            if (connection == null) {
                return "Нету подключения, воспользуйтесь кнопкой ПОДКЛЮЧИТЬСЯ"
            } else {
                val bytesToSend = (message + lineFeed).toByteArray()
                connection!!.bulkTransfer(
                    epOUT, bytesToSend, bytesToSend.size,
                    USB_WRITE_TIMEOUT_MILLIS
                )
            }
        } catch (e: Exception) {
            return "В разрешении на устройство было отказано, подключите устройство, что бы разрешить и взаимодействовать с ним"
        }
        return ""
    }
    fun readToConnectToDevice(context: Context) {
        val buffer = ByteArray(BUFFER_READ)
        Thread {
            if (context is MainActivity) {
                while (flagRead) {
                    val bytes: Int? =
                        connection?.bulkTransfer(epIN, buffer, buffer.size, TIMEOUT_READ)
                    if (bytes != null && bytes > 0) {
                        (context as Activity).runOnUiThread {
                            val str: String = String(buffer, 0, bytes, Charsets.UTF_8)
                            context.showReadData(str)
                        }
                    }
                    if (!checkConnectToDevice()) {
                        (context as Activity).runOnUiThread {
                            context.showButtonConnection(false)
                        }
                    }
                }
            }
        }.start()
    }
    val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_USB_PERMISSION) {
                usbSerialDevice?.close()
                var usbManager: UsbManager? = null
                if (context is MainActivity) {
                    usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager?
                }
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                var tmp: String
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (context is MainActivity) {
                        context.showButtonConnection(true)
                    }
                    device?.apply {

                        connection = usbManager?.openDevice(device)
                        if (connection != null) {
                            flagRead = false
                            try {
                                usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection)
                                usbSerialDevice?.open()
                            } catch (e: IOException) {
                                Log.d("usbSerialDevice", e.message.toString())
                            }



                            val baudRate = 115200
                            val data = byteArrayOf(
                                (baudRate and 0xff).toByte(),
                                (baudRate shr 8 and 0xff).toByte(),
                                (baudRate shr 16 and 0xff).toByte(),
                                (baudRate shr 24 and 0xff).toByte()
                            )

                            var ret: Int = connection!!.controlTransfer(
                                REQTYPE_HOST_TO_DEVICE,
                                SILABSER_SET_BAUDRATE, 0, 0, data, data.size,
                                USB_WRITE_TIMEOUT_MILLIS
                            )
                            if (ret < 0) {
                                Log.e(TAG_APP, "Error setting baud rate")
                            }

                            ret = connection!!.controlTransfer(
                                REQTYPE_HOST_TO_DEVICE,
                                SILABSER_IFC_ENABLE_REQUEST_CODE,
                                UART_ENABLE, 0, null, 0,
                                USB_WRITE_TIMEOUT_MILLIS
                            )
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
                                    deviceConnect = device
                                    onSelectUumBit(ConstUsbSettings.numBit)
                                    onSerialSpeed(ConstUsbSettings.speedIndex)
                                    onSerialParity(ConstUsbSettings.parityIndex)
                                    onSerialStopBits(ConstUsbSettings.stopBit)
                                    flagRead = true
                                    val bytesToSend = "Подключение произошло успешно".toByteArray()
                                    connection!!.bulkTransfer(
                                        epOUT, bytesToSend, bytesToSend.size,
                                        USB_WRITE_TIMEOUT_MILLIS
                                    )
                                    (context as Activity).runOnUiThread {
                                        if (context is MainActivity) {
                                            context.showDeviceName(device.productName.toString())
                                        }
                                    }
                                    readToConnectToDevice(context)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}