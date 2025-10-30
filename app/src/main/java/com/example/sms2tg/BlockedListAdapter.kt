package com.example.sms2tg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class BlockedListAdapter(
    private val onDelete: (BlockedSender) -> Unit
) : ListAdapter<BlockedSender, BlockedListAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<BlockedSender>() {
            override fun areItemsTheSame(old: BlockedSender, new: BlockedSender) = old.id == new.id
            override fun areContentsTheSame(old: BlockedSender, new: BlockedSender) = old == new
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blocked_sender, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tv = view.findViewById<TextView>(R.id.tvPattern)
        private val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)

        fun bind(item: BlockedSender) {
            tv.text = item.pattern
            btnDelete.setOnClickListener { onDelete(item) }
        }
    }
}
