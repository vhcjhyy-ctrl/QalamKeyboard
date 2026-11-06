package com.qalam.keyboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast

class VoiceInputActivity : Activity() {
    companion object {
        const val ACTION_INSERT_TEXT = "com.qalam.keyboard.action.INSERT_RECOGNIZED_TEXT"
        const val EXTRA_TRANSCRIBED_TEXT = "recognized_text"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lang = intent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE) ?: "ar-EG"
        try {
            val speech = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث الآن")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            startActivityForResult(speech, 41)
        } catch (_: Exception) {
            Toast.makeText(this, "خدمة الإملاء الصوتي غير متاحة على هذا الجهاز", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    @Deprecated("Deprecated in Android API; retained for compatibility with speech recognizers")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 41 && resultCode == RESULT_OK) {
            val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                sendBroadcast(Intent(ACTION_INSERT_TEXT).apply {
                    setPackage(packageName)
                    putExtra(EXTRA_TRANSCRIBED_TEXT, spoken)
                })
            }
        }
        finish()
    }
}
