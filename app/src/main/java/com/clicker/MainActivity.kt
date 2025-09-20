package com.clicker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
            val editColor = dialogView.findViewById<EditText>(R.id.editColor)
            val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()
            btnConfirm.setOnClickListener {
                val name = editName.text.toString()
                val color = editColor.text.toString()
                dialog.dismiss()
            }
            dialog.show()
        }

        settingsButton.setOnClickListener {
            // ✅ isClickable 플래그를 확인하여 여러 번 클릭되는 것을 방지합니다.
            if (isClickable) {
                // 한 번 클릭했으니 바로 플래그를 false로 바꿉니다.
                isClickable = false

                // SettingsActivity로 이동하는 Intent를 생성하고 시작합니다.
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
    }

    // ✅ 사용자가 MainActivity로 돌아왔을 때 플래그를 다시 true로 변경합니다.
    override fun onResume() {
        super.onResume()
        isClickable = true
    }
}
