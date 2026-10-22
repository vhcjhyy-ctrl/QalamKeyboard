package com.qalam.keyboard

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.speech.RecognizerIntent
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class QalamInputMethodService : InputMethodService() {
    private var arabic = true
    private var symbols = false
    private lateinit var root: LinearLayout
    private lateinit var keysArea: LinearLayout

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4, 5, 4, 5)
            setBackgroundColor(Color.rgb(22, 25, 33))
        }
        val toolbar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        toolbar.addView(tool("عربي / EN", 1f) { arabic = !arabic; symbols = false; rebuildKeys() })
        toolbar.addView(tool("تصحيح", .8f) { correctCurrentWord() })
        toolbar.addView(tool("🎙", .5f) { launchVoiceInput() })
        toolbar.addView(tool("مسح", .5f) { currentInputConnection?.deleteSurroundingText(1000, 0) })
        root.addView(toolbar)
        keysArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(keysArea)
        rebuildKeys()
        return root
    }

    private fun rebuildKeys() {
        if (!::keysArea.isInitialized) return
        keysArea.removeAllViews()
        val rows = if (symbols) {
            listOf("1 2 3 4 5 6 7 8 9 0".split(" "),
                "@ # \\$ % & - + ( ) /".split(" "),
                listOf("=", "*", "\"", "'", ":", ";", "!", "?", "↵"))
        } else if (arabic) {
            listOf("ض ص ث ق ف غ ع ه خ ح ج".split(" "),
                "ش س ي ب ل ا ت ن م ك ة".split(" "),
                "ئ ء ؤ ر لا ى ظ".split(" "))
        } else {
            listOf("q w e r t y u i o p".split(" "),
                "a s d f g h j k l".split(" "),
                "z x c v b n m".split(" "))
        }
        rows.forEach { row ->
            val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row.forEach { key -> line.addView(keyButton(key) { typeKey(key) }, weightParams(1f)) }
            keysArea.addView(line)
        }
        val bottom = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        bottom.addView(keyButton("?123") { symbols = !symbols; rebuildKeys() }, weightParams(1.2f))
        bottom.addView(keyButton("⌫") { currentInputConnection?.deleteSurroundingText(1, 0) }, weightParams(1f))
        bottom.addView(keyButton(if (arabic) "،" else ",") { currentInputConnection?.commitText(if (arabic) "،" else ",", 1) }, weightParams(.8f))
        bottom.addView(keyButton("مسافة") {
            correctCurrentWord()
            currentInputConnection?.commitText(" ", 1)
        }, weightParams(2.6f))
        bottom.addView(keyButton(if (arabic) "؟" else ".") { currentInputConnection?.commitText(if (arabic) "؟" else ".", 1) }, weightParams(.8f))
        bottom.addView(keyButton("↵") { sendEnter() }, weightParams(1f))
        keysArea.addView(bottom)
    }

    private fun weightParams(weight: Float) = LinearLayout.LayoutParams(0, dp(44), weight).apply {
        setMargins(dp(2), dp(2), dp(2), dp(2))
    }

    private fun keyButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        textSize = if (label.length > 2) 12f else 18f
        isAllCaps = false
        setTextColor(Color.WHITE)
        typeface = Typeface.DEFAULT_BOLD
        background = GradientDrawable().apply { setColor(Color.rgb(53, 59, 73)); cornerRadius = dp(7).toFloat() }
        minWidth = 0; minimumWidth = 0
        setPadding(0, 0, 0, 0)
        setOnClickListener { action() }
    }

    private fun tool(label: String, weight: Float, action: () -> Unit) = TextView(this).apply {
        text = label; textSize = 13f; gravity = Gravity.CENTER
        setTextColor(Color.WHITE); setPadding(3, dp(10), 3, dp(10))
        layoutParams = LinearLayout.LayoutParams(0, dp(38), weight)
        setOnClickListener { action() }
    }

    private fun typeKey(key: String) {
        val ic = currentInputConnection ?: return
        ic.commitText(key, 1)
    }

    private fun correctCurrentWord() {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(80, 0)?.toString().orEmpty()
        val word = Regex("""[^\s]+$""").find(before)?.value ?: return
        val fixed = LocalCorrection.correct(word)
        if (fixed != word) {
            ic.deleteSurroundingText(word.length, 0)
            ic.commitText(fixed, 1)
        }
    }

    private fun sendEnter() {
        val ic = currentInputConnection ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    private fun launchVoiceInput() {
        try {
            val intent = Intent(this, VoiceInputActivity::class.java).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (arabic) "ar-EG" else Locale.US.toLanguageTag())
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Exception) { }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
