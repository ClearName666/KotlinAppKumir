package com.example.testappusb.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testappusb.R
import com.example.testappusb.model.HintCommandsSetView


class HintCommandsSetViewAdapter(private val context: Context,
                                 private val list: List<HintCommandsSetView>,
                                 private var lastSelectedPosition: Int  = -1
) :  RecyclerView.Adapter<HintCommandsSetViewAdapter.HintCommandsSetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HintCommandsSetViewHolder {
        val itemView: View = LayoutInflater.from(context).inflate(
            R.layout.item_save_commands, parent, false)

        return HintCommandsSetViewHolder(itemView)
    }

    override fun getItemCount():Int = list.size
    override fun onBindViewHolder(holder: HintCommandsSetViewHolder, position: Int) {
        val currentItem = list[position]
        currentItem.text.let { text ->
            holder.text.text = text
            holder.radioButton.isChecked = lastSelectedPosition == position

            holder.radioButton.setOnClickListener { _ ->
                val previousSelectedPosition = lastSelectedPosition
                lastSelectedPosition = holder.adapterPosition

                if (previousSelectedPosition >= 0) {
                    notifyItemChanged(previousSelectedPosition)
                }

                notifyItemChanged(lastSelectedPosition)
            }
        }
    }

    class HintCommandsSetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.textItem)
        val radioButton: RadioButton = itemView.findViewById(R.id.radioButton)
    }
}