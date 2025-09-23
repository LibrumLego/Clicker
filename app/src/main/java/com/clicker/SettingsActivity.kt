package com.clicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.widget.Toast

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // 사용방법 버튼
        val howToUseButton: MaterialButton = findViewById(R.id.button_how_to_use)
        howToUseButton.setOnClickListener {
            val intent = Intent(this, HowToUseActivity::class.java)
            startActivity(intent)
        }

        // 리뷰 버튼
        val reviewButton: MaterialButton = findViewById(R.id.button_review)
        reviewButton.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("market://details?id=$packageName")
                startActivity(intent)
            } catch (e: android.content.ActivityNotFoundException) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                startActivity(intent)
            }
        }

        // 배경음 버튼
        val soundButton: MaterialButton = findViewById(R.id.button_sound)
        soundButton.setOnClickListener {
            val soundOptions = arrayOf("배경음 1", "배경음 2", "배경음 3")
            var selectedIndex = 0

            val builder = AlertDialog.Builder(this)
            builder.setTitle("배경음 선택")
            builder.setSingleChoiceItems(soundOptions, selectedIndex) { _, which ->
                selectedIndex = which
            }
            builder.setPositiveButton("확인") { dialog, _ ->
                val selectedSound = soundOptions[selectedIndex]
                Toast.makeText(this, "선택: $selectedSound", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            builder.setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }
    }
}