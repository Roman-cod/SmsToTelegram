package com.example.sms2tg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BlockedListActivity : AppCompatActivity() {

    private lateinit var etPattern: EditText
    private lateinit var btnAdd: Button
    private lateinit var rvList: RecyclerView
    private lateinit var adapter: BlockedAdapter
    private val items: MutableList<BlockedSender> = mutableListOf()

    private val db by lazy { AppDatabase.get(this) }
    private val dao by lazy { db.blockedSenderDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked_list)

        etPattern = findViewById(R.id.etPattern)
        btnAdd = findViewById(R.id.btnAdd)
        rvList = findViewById(R.id.rvBlocked)

        adapter = BlockedAdapter(
            onLongClick = { item: BlockedSender ->
                AlertDialog.Builder(this)
                    .setTitle("Удалить отправителя?")
                    .setMessage("Вы уверены, что хотите удалить '${item.pattern}' из списка?")
                    .setPositiveButton("Удалить") { _, _ ->
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                dao.delete(item)
                            }
                            Toast.makeText(this@BlockedListActivity, "Удалено", Toast.LENGTH_SHORT).show()
                            loadData()
                        }
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }
        )

        rvList.layoutManager = LinearLayoutManager(this)
        rvList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rvList.adapter = adapter

        btnAdd.setOnClickListener {
            val pattern = etPattern.text.toString().trim()
            if (pattern.isEmpty()) {
                Toast.makeText(this, "Введите шаблон отправителя", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    dao.insert(BlockedSender(pattern = pattern))
                }
                Toast.makeText(this@BlockedListActivity, "Добавлено", Toast.LENGTH_SHORT).show()
                etPattern.setText("")
                loadData()
            }
        }

        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            val list: List<BlockedSender> = withContext(Dispatchers.IO) { dao.getAll() }
            items.clear()
            items.addAll(list)
            adapter.submit(items)
        }
    }

    private class BlockedAdapter(
        private val onLongClick: (BlockedSender) -> Unit
    ) : RecyclerView.Adapter<BlockedVH>() {

        private val data: MutableList<BlockedSender> = mutableListOf()

        fun submit(newItems: List<BlockedSender>) {
            data.clear()
            data.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedVH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_blocked_sender, parent, false)
            return BlockedVH(view, onLongClick)
        }

        override fun onBindViewHolder(holder: BlockedVH, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int = data.size
    }
}
