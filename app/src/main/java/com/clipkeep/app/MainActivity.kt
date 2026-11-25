package com.clipkeep.app

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    private lateinit var db: Db
    private lateinit var list: ListView
    private var items: List<Clip> = emptyList()
    private var query = ""
    private val fmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        db = Db(this)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 24, 24, 24) }

        val row = LinearLayout(this)
        fun btn(t: String, a: () -> Unit) = Button(this).apply {
            text = t; textSize = 12f; setOnClickListener { a() }
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        row.addView(btn("Save clipboard") {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val t = cm.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()
            if (t.isNullOrBlank()) Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show()
            else { db.add(t); refresh(); Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show() }
        })
        row.addView(btn("Delete all") {
            AlertDialog.Builder(this).setMessage("Delete all saved clips?")
                .setPositiveButton("Delete") { _, _ -> db.clear(); refresh() }
                .setNegativeButton("Cancel", null).show()
        })
        root.addView(row)

        val search = EditText(this).apply { hint = "Search" }
        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { query = s.toString(); refresh() }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        })
        root.addView(search)

        list = ListView(this)
        list.setOnItemClickListener { _, _, i, _ ->
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("clip", items[i].text))
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
        }
        list.setOnItemLongClickListener { _, _, i, _ ->
            AlertDialog.Builder(this).setMessage("Delete this clip?")
                .setPositiveButton("Delete") { _, _ -> db.delete(items[i].id); refresh() }
                .setNegativeButton("Cancel", null).show()
            true
        }
        root.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        items = db.all(query)
        list.adapter = object : BaseAdapter() {
            override fun getCount() = items.size
            override fun getItem(p: Int) = items[p]
            override fun getItemId(p: Int) = items[p].id
            override fun getView(p: Int, v: View?, parent: ViewGroup): View {
                val c = items[p]
                val tv = (v as? TextView) ?: TextView(this@MainActivity).apply { setPadding(16, 20, 16, 20) }
                tv.text = "${c.text.take(300)}\n${fmt.format(Date(c.ts))}"
                return tv
            }
        }
    }
}
