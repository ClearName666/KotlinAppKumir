package com.example.testappusb.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.testappusb.ItemsButtonTextSet
import com.example.testappusb.R
import com.example.testappusb.model.SaveTextCommandView

class SaveTextCommandViewAdapter(
    private val context: Context,
    private val list: ArrayList<SaveTextCommandView>
) :  RecyclerView.Adapter<SaveTextCommandViewAdapter.SaveTextCommandViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaveTextCommandViewHolder {
        val itemView: View = LayoutInflater.from(context).inflate(
            R.layout.item_text_history_comand,
            parent, false)
        return SaveTextCommandViewHolder(itemView)
    }

    override fun getItemCount():Int = list.size

    override fun onBindViewHolder(holder: SaveTextCommandViewHolder, position: Int) {
        val currentItem = list[position]
        currentItem.text.let { text ->
            holder.buttonTextTerm.text = text
            holder.buttonTextTerm.setOnClickListener {
                if (context is ItemsButtonTextSet) {
                    context.setTextFromButton(holder.buttonTextTerm.text.toString())
                }
            }
        }
    }

    class SaveTextCommandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val buttonTextTerm: Button = itemView.findViewById(R.id.itemTextComandSave)
    }
}