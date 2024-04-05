package com.example.testappusb

import android.hardware.usb.UsbDevice

interface UsbActivityInterface {
    val usb: Usb
    fun showDeviceName(deviceName: String)
    fun showButtonConnection(con: Boolean)
    fun connectToUsbDevice(device: UsbDevice)
    fun withdrawalsShow(msg: String)
    fun printData(data: String)
}