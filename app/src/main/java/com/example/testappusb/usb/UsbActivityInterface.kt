package com.example.testappusb.usb

import android.hardware.usb.UsbDevice

// интерфейс для работы с классом usb от него надо наследоваться в Activity
interface UsbActivityInterface {
    val usb: Usb
    fun showDeviceName(deviceName: String) // отображения имени подключенного девайса
    fun showButtonConnection(con: Boolean) // функция для отображения статуса подключения к девайсу на кнопки
    fun connectToUsbDevice(device: UsbDevice) // подключения и регистрация широковещятельного приемника
    fun withdrawalsShow(msg: String) // вывод ошибок при работе с девасом
    fun printData(data: String) // вывод полученых данных из серийного порта
    fun printDSR_CTS(dsr: Boolean, cts: Boolean)

}