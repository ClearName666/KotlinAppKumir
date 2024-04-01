package com.example.testappusb.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.example.testappusb.MainActivity
import com.example.testappusb.R
import com.example.testappusb.model.SettingsSerialConnectDeviceView


class SettingsSerialConnectDeviceViewAdapter(
    private val context: Context,
    private val list: ArrayList<SettingsSerialConnectDeviceView>
) : RecyclerView.Adapter<SettingsSerialConnectDeviceViewAdapter.SettingsSerialConnectDeviceViewHolder>(){

    //public var listSpinner: ArrayList<Spinner> = arrayListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SettingsSerialConnectDeviceViewHolder {
        val itemView: View = LayoutInflater.from(context).inflate(R.layout.recycler_settings_serial, parent, false)
        return SettingsSerialConnectDeviceViewHolder(itemView)
    }

    override fun getItemCount():Int = list.size

    override fun onBindViewHolder(holder: SettingsSerialConnectDeviceViewHolder, position: Int) {
        val currentItem = list[position]
        currentItem.list?.let { list ->
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, list)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            holder.spinner.adapter = adapter
            //listSpinner.add(holder.spinner)
            if (context is MainActivity) {
                if (position == 0) {
                    holder.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                            if (position == 0) {
                                context.usb.onSelectUumBit(true)
                                //Log.d("SetCon", "число бит 8")
                            } else {
                                context.usb.onSelectUumBit(false)
                                //Log.d("SetCon", "число бит 7")
                            }
                        }
                        override fun onNothingSelected(parent: AdapterView<*>) {

                        }
                    }
                } else if (position == 1) {
                    holder.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                            context.usb.onSerialSpeed(position)
                        }
                        override fun onNothingSelected(parent: AdapterView<*>) {

                        }
                    }
                } else if (position == 2) {
                    holder.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                            context.usb.onSerialParity(position)
                        }
                        override fun onNothingSelected(parent: AdapterView<*>) {

                        }
                    }
                } else if (position == 3) {
                    holder.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                            context.usb.onSerialStopBits(position)
                        }
                        override fun onNothingSelected(parent: AdapterView<*>) {

                        }
                    }
                }
            }
        }
    }

    class SettingsSerialConnectDeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val spinner: Spinner = itemView.findViewById(R.id.itemSpinnerSetting)
    }
}

