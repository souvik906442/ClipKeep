package com.clipkeep.app

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

/** Handles: text selection menu, Share menu, and Quick Settings tile (clipboard mode). */
class SaveActivity : Activity() {
    private var done = false

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        when (intent?.action) {
            Intent.ACTION_PROCESS_TEXT ->
                finishWith(intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString())
            Intent.ACTION_SEND ->
                finishWith(intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString())
            // else: clipboard mode, wait for window focus (Android only allows reading then)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus || done) return
        if (intent?.action == Intent.ACTION_PROCESS_TEXT || intent?.action == Intent.ACTION_SEND) return
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val t = try { cm.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString() } catch (_: Exception) { null }
        finishWith(t)
    }

    private fun finishWith(text: String?) {
        if (done) return
        done = true
        if (text.isNullOrBlank()) {
            Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show()
        } else {
            Db(this).add(text)
            Toast.makeText(this, "Saved to ClipKeep", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
