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
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale

class QalamInputMethodService : InputMethodService() {
    private var arabic = true
    private var symbols = false
    private var developerMode = false
    private var autoCorrection = true
    private var keyHeightDp = 48
    private lateinit var root: LinearLayout
    private lateinit var keysArea: LinearLayout
    private lateinit var suggestionsBar: LinearLayout

    private val speechReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: Intent?) {
            if (intent?.action != VoiceInputActivity.ACTION_INSERT_SPEECH) return
            val text = intent.getStringExtra(VoiceInputActivity.EXTRA_SPOKEN_TEXT).orEmpty()
            if (text.isNotBlank()) currentInputConnection?.commitText(text, 1)
        }
    }
    private var receiverRegistered = false

    override fun onCreate() {
        super.onCreate()
        val filter = android.content.IntentFilter(VoiceInputActivity.ACTION_INSERT_SPEECH)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            registerReceiver(speechReceiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(speechReceiver, filter)
        }
        receiverRegistered = true
    }

    override fun onDestroy() {
        if (receiverRegistered) {
            unregisterReceiver(speechReceiver)
            receiverRegistered = false
        }
        super.onDestroy()
    }

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(5), dp(5), dp(5), dp(4))
            setBackgroundColor(Color.rgb(15, 20, 32))
        }
        val toolbar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        toolbar.addView(tool("قلم / Q", 1.1f) { arabic = !arabic; symbols = false; rebuildKeys() })
        toolbar.addView(tool("تصحيح ${if (autoCorrection) "✓" else "○"}", 1f) { autoCorrection = !autoCorrection; rebuildKeys() })
        toolbar.addView(tool("{} Dev", .9f) { developerMode = !developerMode; symbols = false; rebuildKeys() })
        toolbar.addView(tool("A+", .55f) { keyHeightDp = (keyHeightDp + 4).coerceAtMost(64); rebuildKeys() })
        toolbar.addView(tool("A−", .55f) { keyHeightDp = (keyHeightDp - 4).coerceAtLeast(40); rebuildKeys() })
        root.addView(toolbar)
        suggestionsBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(1), 0, dp(1))
        }
        root.addView(suggestionsBar, LinearLayout.LayoutParams(-1, dp(30)))
        keysArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply {
            isFillViewport = false
            addView(keysArea)
        }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        rebuildKeys()
        return root
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        if (::keysArea.isInitialized) rebuildKeys()
    }

    private fun rebuildKeys() {
        if (!::keysArea.isInitialized) return
        keysArea.removeAllViews()
        suggestionsBar.removeAllViews()
        suggestionsBar.addView(tool("↶", .5f) { undoLastWordCorrection() })
        suggestionsBar.addView(tool("قاموس", .85f) { showDictionaryTools() })
        suggestionsBar.addView(tool("🎙", .5f) { launchVoiceInput() })
        suggestionsBar.addView(tool("مسح الكل", .9f) { currentInputConnection?.deleteSurroundingText(1000, 0) })

        val rows: List<List<String>> = when {
            developerMode -> listOf(
                listOf("{", "}", "(", ")", "[", "]", "<", ">"),
                listOf("=", "==", "!=", "=>", "->", "&&", "||", "!"),
                listOf(";", ":", "\"", "'", "`", "_", ".", ","),
                listOf("/", "//", "/*", "*/", "#", "@", "\\", "|"),
                listOf("+", "-", "*", "%", "?", "??", "?.", "..."),
            )
            symbols -> listOf(
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
                listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/"),
                listOf("=", "*", "\"", "'", ":", ";", "!", "?", "_", "↵")
            )
            arabic -> listOf(
                listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج"),
                listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ة"),
                listOf("ئ", "ء", "ؤ", "ر", "لا", "ى", "ظ")
            )
            else -> listOf(
                listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
                listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
                listOf("z", "x", "c", "v", "b", "n", "m")
            )
        }
        rows.forEachIndexed { index, row ->
            val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row.forEach { key -> line.addView(keyButton(key) { typeKey(key) }, weightParams(1f, keyHeightDp)) }
            keysArea.addView(line)
        }
        val bottom = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        bottom.addView(keyButton(if (developerMode) "ABC" else "?123") {
            if (developerMode) developerMode = false else symbols = !symbols
            rebuildKeys()
        }, weightParams(1.05f, keyHeightDp))
        bottom.addView(keyButton("⇧") { toggleCase() }, weightParams(.85f, keyHeightDp))
        bottom.addView(keyButton("⌫") { currentInputConnection?.deleteSurroundingText(1, 0) }, weightParams(1f, keyHeightDp))
        bottom.addView(keyButton(if (arabic) "،" else ",") { currentInputConnection?.commitText(if (arabic) "،" else ",", 1) }, weightParams(.8f, keyHeightDp))
        bottom.addView(keyButton("مسافة") { insertSpaceWithCorrection() }, weightParams(2.1f, keyHeightDp))
        bottom.addView(keyButton(if (arabic) "؟" else ".") { currentInputConnection?.commitText(if (arabic) "؟" else ".", 1) }, weightParams(.8f, keyHeightDp))
        bottom.addView(keyButton("↵") { sendEnter() }, weightParams(1f, keyHeightDp))
        keysArea.addView(bottom)
    }

    private fun weightParams(weight: Float, height: Int = keyHeightDp) = LinearLayout.LayoutParams(0, dp(height), weight).apply {
        setMargins(dp(2), dp(2), dp(2), dp(2))
    }

    private fun keyButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        textSize = when {
            label.length >= 5 -> 12f
            label.length >= 3 -> 14f
            else -> 19f
        }
        isAllCaps = false
        setTextColor(Color.rgb(244, 247, 255))
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        background = GradientDrawable().apply {
            setColor(if (label in listOf("مسافة", "{} Dev", "قلم / Q")) Color.rgb(37, 99, 235) else Color.rgb(38, 48, 67))
            cornerRadius = dp(9).toFloat()
            setStroke(dp(1), Color.rgb(58, 74, 100))
        }
        minWidth = 0; minimumWidth = 0
        setPadding(dp(1), 0, dp(1), 0)
        contentDescription = label
        setOnClickListener { action() }
    }

    private fun tool(label: String, weight: Float, action: () -> Unit) = TextView(this).apply {
        text = label; textSize = 12f; gravity = Gravity.CENTER
        setTextColor(Color.rgb(210, 224, 255)); typeface = Typeface.DEFAULT_BOLD
        setPadding(dp(2), dp(8), dp(2), dp(8))
        layoutParams = LinearLayout.LayoutParams(0, dp(34), weight)
        setOnClickListener { action() }
    }

    private fun typeKey(key: String) {
        val ic = currentInputConnection ?: return
        ic.commitText(key, 1)
        if (autoCorrection && !developerMode && key.length == 1 && key[0].isLetter()) updateSuggestions()
        else suggestionsBar.removeAllViews()
    }

    private fun insertSpaceWithCorrection() {
        if (autoCorrection && !developerMode) correctCurrentWord()
        currentInputConnection?.commitText(" ", 1)
        updateSuggestions()
    }

    private fun updateSuggestions() {
        if (!::suggestionsBar.isInitialized) return
        suggestionsBar.removeAllViews()
        val before = currentInputConnection?.getTextBeforeCursor(60, 0)?.toString().orEmpty()
        val prefix = Regex("[^\\s]+$").find(before)?.value.orEmpty().trim { !it.isLetterOrDigit() && it !in "أإآءئ" }
        val suggestions = LocalCorrection.suggestions(prefix)
        if (suggestions.isEmpty()) {
            suggestionsBar.addView(tool("محلي · دون إنترنت", 1f) {})
            return
        }
        suggestions.forEach { suggestion -> suggestionsBar.addView(tool(suggestion, 1f) {
            val ic = currentInputConnection ?: return@tool
            val token = Regex("[^\\s]+$").find(ic.getTextBeforeCursor(80, 0)?.toString().orEmpty())?.value ?: return@tool
            ic.deleteSurroundingText(token.length, 0)
            ic.commitText(suggestion, 1)
        }) }
    }

    private fun correctCurrentWord() {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(100, 0)?.toString().orEmpty()
        val word = Regex("[^\\s]+$").find(before)?.value ?: return
        val fixed = LocalCorrection.correct(word)
        if (fixed != word) {
            ic.deleteSurroundingText(word.length, 0)
            ic.commitText(fixed, 1)
        }
    }

    private fun undoLastWordCorrection() {
        // The keyboard deliberately avoids keeping typed content in storage. This tool acts as a one-step local undo.
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    private fun showDictionaryTools() {
        suggestionsBar.removeAllViews()
        suggestionsBar.addView(tool("تصحيح محلي ${if (autoCorrection) "مفعّل" else "متوقف"}", 1.4f) { autoCorrection = !autoCorrection; rebuildKeys() })
        suggestionsBar.addView(tool("إن شاء الله", 1f) { currentInputConnection?.commitText("إن شاء الله", 1) })
        suggestionsBar.addView(tool("الحمد لله", 1f) { currentInputConnection?.commitText("الحمد لله", 1) })
    }

    private fun toggleCase() {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(1, 0)?.toString().orEmpty()
        if (before.isNotEmpty() && before[0].isLetter()) {
            ic.deleteSurroundingText(1, 0)
            ic.commitText(if (before[0].isUpperCase()) before.lowercase(Locale.ROOT) else before.uppercase(Locale.ROOT), 1)
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
