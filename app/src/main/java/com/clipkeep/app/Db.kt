package com.clipkeep.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Clip(val id: Long, val text: String, val ts: Long)

class Db(ctx: Context) : SQLiteOpenHelper(ctx, "clips.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE clips(id INTEGER PRIMARY KEY AUTOINCREMENT, text TEXT NOT NULL, ts INTEGER NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, o: Int, n: Int) {}

    @Synchronized
    fun add(text: String) {
        if (text.isBlank()) return
        val db = writableDatabase
        db.rawQuery("SELECT text FROM clips ORDER BY id DESC LIMIT 1", null).use {
            if (it.moveToFirst() && it.getString(0) == text) return
        }
        db.insert("clips", null, ContentValues().apply {
            put("text", text); put("ts", System.currentTimeMillis())
        })
    }

    fun all(q: String): List<Clip> {
        val out = ArrayList<Clip>()
        val c = if (q.isBlank())
            readableDatabase.rawQuery("SELECT id,text,ts FROM clips ORDER BY id DESC", null)
        else
            readableDatabase.rawQuery("SELECT id,text,ts FROM clips WHERE text LIKE ? ORDER BY id DESC", arrayOf("%$q%"))
        c.use { while (it.moveToNext()) out.add(Clip(it.getLong(0), it.getString(1), it.getLong(2))) }
        return out
    }

    fun delete(id: Long) { writableDatabase.delete("clips", "id=?", arrayOf(id.toString())) }
    fun clear() { writableDatabase.delete("clips", null, null) }
}
