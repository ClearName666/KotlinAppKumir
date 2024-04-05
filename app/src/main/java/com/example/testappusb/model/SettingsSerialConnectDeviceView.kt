package com.example.testappusb.model

class SettingsSerialConnectDeviceView {
    var id:Int? = null
    var list:ArrayList<String>? = null
    var selectedPosition: Int = 0


    constructor(id: Int?, list: ArrayList<String>?) {
        this.id = id
        this.list = list
    }
}