package com.clicker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var isClickable = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val addButton: ImageButton = findViewById(R.id.addButton)
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)

        addButton.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)

            val editName = dialogView.findViewById<EditText>(R.id.editName)
            val btnConfirm = dialogView.findViewById<View>(R.id.btnConfirm)

            val colorRed   = dialogView.findViewById<ImageView>(R.id.colorRed)
            val colorBlue  = dialogView.findViewById<ImageView>(R.id.colorBlue)
            val colorGreen = dialogView.findViewById<ImageView>(R.id.colorGreen)
            val colorYellow= dialogView.findViewById<ImageView>(R.id.colorYellow)
            val colorPurple= dialogView.findViewById<ImageView>(R.id.colorPurple)

            val colorViews = listOf(colorRed, colorBlue, colorGreen, colorYellow, colorPurple)
            var selectedColor: String? = null

            colorViews.forEach { v ->
                v.setOnClickListener {
                    colorViews.forEach { it.isSelected = false }
                    v.isSelected = true
                    selectedColor = when (v.id) {
                        R.id.colorRed    -> "#F44336"
                        R.id.colorBlue   -> "#2196F3"
                        R.id.colorGreen  -> "#4CAF50"
                        R.id.colorYellow -> "#FFEB3B"
                        R.id.colorPurple -> "#9C27B0"
                        else -> null
                    }
                }
            }

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

            btnConfirm.setOnClickListener {
                val name = editName.text.toString()
                val color = selectedColor ?: ""
                Toast.makeText(this, "이름: $name / 색상: $color", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            dialog.show()
        }

        settingsButton.setOnClickListener {
            if (isClickable) {
                isClickable = false
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isClickable = true
    }
}
