package com.example.testappusb.adapters.AdaptersMainActivity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.testappusb.ItemsButtonTextSet
import com.example.testappusb.R
import com.example.testappusb.model.recyclerModelForMainActivity.SaveTextCommandView


// адаптер для добавления текстов подсказок
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
            // установка текста
            holder.buttonTextTerm.text = text
            if (text.isEmpty()) {
                holder.buttonTextTerm.visibility = View.GONE
            }
            // установка собятия на клик что бы заполниь поле ввода текстом
            holder.buttonTextTerm.setOnClickListener {
                if (context is ItemsButtonTextSet) {
                    // в активити нужно отнаследоваться от ItemsButtonTextSet
                    context.setTextFromButton(holder.buttonTextTerm.text.toString())
                }
            }
        }
    }

    class SaveTextCommandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val buttonTextTerm: Button = itemView.findViewById(R.id.itemTextComandSave)
    }
}