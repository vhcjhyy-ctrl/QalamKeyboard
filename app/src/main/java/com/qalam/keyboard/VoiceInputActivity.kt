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
                getSystemService(android.view.inputmethod.InputMethodManager::class.java)
                    ?.currentInputMethodSubtype
                // Return text to the IME through a package-scoped broadcast is intentionally avoided.
                // Insert by switching focus back is not guaranteed; see README limitations.
                Toast.makeText(this, "تم التعرف على: $spoken — الإدراج المباشر قيد الاستكمال", Toast.LENGTH_LONG).show()
            }
        }
        finish()
    }
}
