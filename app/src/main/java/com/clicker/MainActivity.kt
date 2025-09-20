package com.clicker

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val addButton: ImageButton = findViewById(R.id.addButton)

        addButton.setOnClickListener {
            // 다이얼로그 뷰 inflate
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

                // TODO: 입력값 처리 로직
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}
