package com.clicker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var viewModel: CounterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.container)
        val addButton: ImageButton = findViewById(R.id.addButton)
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)

        // ViewModel 가져오기
        viewModel = (application as MyApplication).counterViewModel

        // LiveData 관찰
        viewModel.counters.observe(this) { list ->
            container.removeAllViews()
            list.forEachIndexed { index, counterItem ->
                addCounterItemToLayout(counterItem, index)
            }
        }

        // ➕ 아이템 추가 버튼
        addButton.setOnClickListener { showAddItemDialog() }

        // ⚙️ 설정 버튼
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun showAddItemDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)

        val editName = dialogView.findViewById<EditText>(R.id.editName)
        val btnConfirm = dialogView.findViewById<View>(R.id.btnConfirm)

        val colorRed = dialogView.findViewById<ImageView>(R.id.colorRed)
        val colorBlue = dialogView.findViewById<ImageView>(R.id.colorBlue)
        val colorGreen = dialogView.findViewById<ImageView>(R.id.colorGreen)
        val colorYellow = dialogView.findViewById<ImageView>(R.id.colorYellow)
        val colorPurple = dialogView.findViewById<ImageView>(R.id.colorPurple)
        val colorViews = listOf(colorRed, colorBlue, colorGreen, colorYellow, colorPurple)
        var selectedColorRes: Int? = null

        colorViews.forEach { v ->
            v.setOnClickListener {
                colorViews.forEach { it.isSelected = false }
                v.isSelected = true
                selectedColorRes = when (v.id) {
                    R.id.colorRed -> R.drawable.bg_button_purple_blue
                    R.id.colorBlue -> R.drawable.bg_button_pink_yellow
                    R.id.colorGreen -> R.drawable.bg_button_blue_mint
                    R.id.colorYellow -> R.drawable.bg_button_emerald_gold
                    R.id.colorPurple -> R.drawable.bg_button_purple_pink
                    else -> null
                }
            }
        }

        val dialog = android.app.AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        btnConfirm.setOnClickListener {
            val name = editName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedColorRes == null) {
                Toast.makeText(this, "색상을 골라주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addCounter(CounterItem(name, 0, selectedColorRes!!))
            dialog.dismiss()
        }
    }

    // ✅ 최종: 아이템 뷰 추가
    private fun addCounterItemToLayout(item: CounterItem, index: Int) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_counter, container, false)

        val itemName = itemView.findViewById<TextView>(R.id.itemName)
        val btnMinus = itemView.findViewById<TextView>(R.id.btnMinus)
        val btnPlus = itemView.findViewById<TextView>(R.id.btnPlus)
        val itemValue = itemView.findViewById<TextView>(R.id.itemValue)

        itemName.text = item.name

        // 버튼 색상 적용
        btnMinus.setBackgroundResource(item.colorRes)
        btnPlus.setBackgroundResource(item.colorRes)

        // 중앙 숫자 배경: LayerDrawable (외곽 버튼 + 안쪽 흰색)
        val outerDrawable = resources.getDrawable(item.colorRes, theme).mutate()
        val innerDrawable = resources.getDrawable(R.drawable.bg_edittext_border, theme).mutate()
        val layerDrawable = android.graphics.drawable.LayerDrawable(arrayOf(outerDrawable, innerDrawable))
        layerDrawable.setLayerInset(0, 0, 0, 0, 0) // 외곽
        layerDrawable.setLayerInset(1, 4, 4, 4, 4) // 안쪽 흰색
        itemValue.background = layerDrawable

        // 현재 값 표시
        itemValue.text = item.value.toString()

        // ➖ 버튼 클릭
        btnMinus.setOnClickListener {
            if (item.value > 0) viewModel.updateValue(index, item.value - 1)
        }

        // ➕ 버튼 클릭
        btnPlus.setOnClickListener {
            viewModel.updateValue(index, item.value + 1)
        }

        // 중앙 숫자 클릭 시 NumberZoomActivity로 이동
        itemValue.setOnClickListener {
            val intent = Intent(this, NumberZoomActivity::class.java)
            intent.putExtra("index", index)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        container.addView(itemView)
    }
}

