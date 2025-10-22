package com.example.sms2tg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class LogAdapter : ListAdapter<LogEntity, LogAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<LogEntity>() {
            override fun areItemsTheSame(oldItem: LogEntity, newItem: LogEntity) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: LogEntity, newItem: LogEntity) = oldItem == newItem
        }
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvSender: TextView = view.findViewById(R.id.tvSender)
        val tvBody: TextView = view.findViewById(R.id.tvBody)
        val tvTime: TextView = view.findViewById(R.id.tvTime)

        fun bind(item: LogEntity) {
            tvSender.text = item.sender
            tvBody.text = item.body        // ✅ заменено message → body
            tvTime.text = java.text.SimpleDateFormat.getDateTimeInstance()
                .format(java.util.Date(item.timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}
