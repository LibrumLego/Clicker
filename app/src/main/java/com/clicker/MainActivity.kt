package com.clicker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.clicker.R

class MainActivity : AppCompatActivity() {

    private var isClickable = true
    private lateinit var container: LinearLayout // 아이템이 추가될 컨테이너

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.container)

        val addButton: ImageButton = findViewById(R.id.addButton)
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)

        // ➕ 추가 버튼
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
            var selectedColorRes: Int? = null // 기본값: 선택 안 한 경우 체크용

            // 색상 선택 이벤트
            colorViews.forEach { v ->
                v.setOnClickListener {
                    colorViews.forEach { it.isSelected = false }
                    v.isSelected = true
                    selectedColorRes = when (v.id) {
                        R.id.colorRed    -> R.drawable.bg_button_purple_blue
                        R.id.colorBlue   -> R.drawable.bg_button_pink_yellow
                        R.id.colorGreen  -> R.drawable.bg_button_blue_mint
                        R.id.colorYellow -> R.drawable.bg_button_emerald_gold
                        R.id.colorPurple -> R.drawable.bg_button_purple_pink
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
                val name = editName.text.toString().trim()

                // 이름이 비어있으면
                if (name.isEmpty()) {
                    Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // 색상 선택 안 했으면
                if (selectedColorRes == null) {
                    Toast.makeText(this, "색상을 골라주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // 둘 다 통과했을 때만 생성
                addItem(name, selectedColorRes!!)
                dialog.dismiss()
            }

            dialog.show()
        }

        // ⚙️ 설정 버튼
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

    // ✅ 아이템 추가 함수
    private fun addItem(name: String, colorRes: Int) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_counter, container, false)

        val itemName = itemView.findViewById<TextView>(R.id.itemName)
        val btnMinus = itemView.findViewById<TextView>(R.id.btnMinus)
        val btnPlus = itemView.findViewById<TextView>(R.id.btnPlus)
        val itemValue = itemView.findViewById<EditText>(R.id.itemValue)

        itemName.text = name
        btnMinus.setBackgroundResource(colorRes)
        btnPlus.setBackgroundResource(colorRes)

        // ✅ 버튼 배경(GradientDrawable) 가져오기
        val buttonDrawable = resources.getDrawable(colorRes, null).mutate()

        // ✅ 숫자칸 안쪽 흰 배경 drawable
        val innerDrawable = resources.getDrawable(R.drawable.bg_edittext_border, null).mutate()

        // ✅ LayerDrawable: [0] 버튼 그라데이션(바깥 테두리), [1] 안쪽 흰 배경
        val layerDrawable = android.graphics.drawable.LayerDrawable(arrayOf(buttonDrawable, innerDrawable))
        layerDrawable.setLayerInset(0, 0, 0, 0, 0)   // 바깥 레이어 (그라데이션)
        layerDrawable.setLayerInset(1, 4, 4, 4, 4)   // 안쪽 레이어 (흰색)

        // 숫자칸 배경으로 적용
        itemValue.background = layerDrawable

        // ➖ 버튼 클릭
        btnMinus.setOnClickListener {
            val value = itemValue.text.toString().toIntOrNull() ?: 0
            if (value > 0) itemValue.setText((value - 1).toString())
        }

        // ➕ 버튼 클릭
        btnPlus.setOnClickListener {
            val value = itemValue.text.toString().toIntOrNull() ?: 0
            itemValue.setText((value + 1).toString())
        }

        container.addView(itemView)
    }
}
