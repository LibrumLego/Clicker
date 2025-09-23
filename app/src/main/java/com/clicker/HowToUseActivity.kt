package com.clicker

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HowToUseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_to_use)

        val closeButton: Button = findViewById(R.id.button_close)
        closeButton.setOnClickListener {
            finish()
        }
    }
}
