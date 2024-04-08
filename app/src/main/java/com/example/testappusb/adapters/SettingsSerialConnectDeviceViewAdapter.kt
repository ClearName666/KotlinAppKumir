package com.example.testappusb.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.example.testappusb.R
import com.example.testappusb.UsbActivityInterface
import com.example.testappusb.model.SettingsSerialConnectDeviceView


class SettingsSerialConnectDeviceViewAdapter(
    private val context: Context,
    private val list: ArrayList<SettingsSerialConnectDeviceView>
) : RecyclerView.Adapter<SettingsSerialConnectDeviceViewAdapter.SettingsSerialConnectDeviceViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsSerialConnectDeviceViewHolder {
        val itemView: View = LayoutInflater.from(context).inflate(R.layout.recycler_settings_serial,
            parent, false)
        return SettingsSerialConnectDeviceViewHolder(itemView)
    }

    override fun getItemCount():Int = list.size

    override fun onBindViewHolder(holder: SettingsSerialConnectDeviceViewHolder, position: Int) {
        val currentItem = list[position]
        currentItem.list?.let { list ->
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, list.map {"${currentItem.firstText} $it"}.toArrayList())
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            holder.spinner.adapter = adapter
            holder.spinner.setSelection(currentItem.selectedPosition)

            if (context is UsbActivityInterface) {

                val positionElemItems: Int = position

                holder.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        currentItem.selectedPosition = position
                        when (positionElemItems) {
                            0 -> {
                                when (position) {
                                    0 -> context.usb.onSelectUumBit(true)
                                    1 -> context.usb.onSelectUumBit(false)
                                    else -> {}
                                }
                            }
                            1 -> context.usb.onSerialSpeed(position)
                            2 -> context.usb.onSerialParity(position)
                            3 -> context.usb.onSerialStopBits(position)
                            4 -> context.usb.onSerialLineFeed(position)
                            5 -> context.usb.onSerialLineFeedRead(position)
                            6 -> context.usb.onSerialDTR(position)
                            7 -> context.usb.onSerialRTS(position)

                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {

                    }
                }
            }
        }
    }

    class SettingsSerialConnectDeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val spinner: Spinner = itemView.findViewById(R.id.itemSpinnerSetting)
    }

    private fun <T> List<T>.toArrayList(): ArrayList<T> {
        return ArrayList(this)
    }
}

