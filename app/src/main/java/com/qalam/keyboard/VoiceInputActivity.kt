package com.qalam.keyboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast

class VoiceInputActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lang = intent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE) ?: "ar-EG"
        try {
            val speech = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث الآن")
            }
            startActivityForResult(speech, 41)
        } catch (_: Exception) {
            Toast.makeText(this, "الإملاء الصوتي يحتاج إلى خدمة تعرّف متوافقة على الجهاز", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    @Deprecated("Deprecated Android API retained for broad speech-recognizer compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 41 && resultCode == RESULT_OK) {
            val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                sendBroadcast(Intent(ACTION_INSERT_SPEECH).apply {
                    setPackage(packageName)
                    putExtra(EXTRA_SPOKEN_TEXT, spoken)
                })
            }
        }
        finish()
    }

    companion object {
        const val ACTION_INSERT_SPEECH = "com.qalam.keyboard.INSERT_SPEECH"
        const val EXTRA_SPOKEN_TEXT = "spoken_text"
    }
}
