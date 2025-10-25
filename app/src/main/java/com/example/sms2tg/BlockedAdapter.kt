package com.example.sms2tg

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sms2tg.databinding.ItemBlockedBinding

class BlockedAdapter :
    ListAdapter<BlockedSender, BlockedAdapter.BlockedViewHolder>(DiffCallback()) {

    inner class BlockedViewHolder(val binding: ItemBlockedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedViewHolder {
        val binding = ItemBlockedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BlockedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlockedViewHolder, position: Int) {
        val sender = getItem(position)
        holder.binding.tvPattern.text = sender.pattern
    }

    class DiffCallback : DiffUtil.ItemCallback<BlockedSender>() {
        override fun areItemsTheSame(oldItem: BlockedSender, newItem: BlockedSender) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BlockedSender, newItem: BlockedSender) = oldItem == newItem
    }
}
