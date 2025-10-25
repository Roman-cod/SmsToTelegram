package com.example.sms2tg

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BlockedVH(
    itemView: View,
    private val onLongClick: (BlockedSender) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val tv: TextView = itemView.findViewById(R.id.tvPattern)

    fun bind(item: BlockedSender) {
        tv.text = item.pattern
        itemView.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }
}