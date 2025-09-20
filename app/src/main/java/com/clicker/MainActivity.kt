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

        // ➕ 추가 버튼 클릭 시 다이얼로그 열기
        addButton.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)

            val editName = dialogView.findViewById<EditText>(R.id.editName)

            // ✅ 버튼은 PNG 이미지 → 클릭 영역만 View
            val btnConfirmArea = dialogView.findViewById<View>(R.id.btnConfirmArea)

            // 색상 동그라미 뷰
            val colorRed   = dialogView.findViewById<ImageView>(R.id.colorRed)
            val colorBlue  = dialogView.findViewById<ImageView>(R.id.colorBlue)
            val colorGreen = dialogView.findViewById<ImageView>(R.id.colorGreen)
            val colorYellow= dialogView.findViewById<ImageView>(R.id.colorYellow)
            val colorPurple= dialogView.findViewById<ImageView>(R.id.colorPurple)

            val colorViews = listOf(colorRed, colorBlue, colorGreen, colorYellow, colorPurple)
            var selectedColor: String? = null

            // 색상 클릭 이벤트 처리
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

            // ✅ 뒷배경 어두워짐 제거
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

            // 확인 버튼 클릭 (투명 View에 이벤트 연결)
            btnConfirmArea.setOnClickListener {
                val name = editName.text.toString()
                val color = selectedColor ?: ""
                Toast.makeText(this, "이름: $name / 색상: $color", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            dialog.show()
        }

        // ⚙️ 설정 버튼 클릭 시 이동
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
