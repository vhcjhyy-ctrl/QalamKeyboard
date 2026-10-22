package com.qalam.keyboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 36, 28, 36)
        }
        layout.addView(TextView(this).apply {
            text = "لوحة قلم\nلوحة مفاتيح عربية وإنجليزية"
            textSize = 24f
            gravity = Gravity.CENTER
        })
        layout.addView(TextView(this).apply {
            text = "\n1) فعّل لوحة قلم من إعدادات لوحات المفاتيح.\n2) اخترها من مبدّل لوحة المفاتيح.\n3) جرّب الكتابة في أي حقل نصي.\n\nالتصحيح الأساسي يعمل محليًا على الجهاز."
            textSize = 16f
            setPadding(0, 16, 0, 20)
        })
        layout.addView(Button(this).apply {
            text = "فتح إعدادات لوحات المفاتيح"
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        })
        layout.addView(Button(this).apply {
            text = "اختيار لوحة المفاتيح الآن"
            setOnClickListener {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
            }
        })
        setContentView(layout)
    }
}
