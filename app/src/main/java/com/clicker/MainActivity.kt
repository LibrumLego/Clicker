package com.clicker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.graphics.drawable.LayerDrawable
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var viewModel: CounterViewModel

    private val MAX_ITEMS = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.container)
        val addButton: ImageButton = findViewById(R.id.addButton)
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)

        // ViewModel 가져오기
        viewModel = (application as MyApplication).counterViewModel

        // LiveData 관찰 -> 리스트가 바뀔 때마다 UI 갱신
        viewModel.counters.observe(this) { list ->
            container.removeAllViews()
            list.forEachIndexed { index, counterItem ->
                // index는 목록 순서를 위한 용도로만 사용
                addCounterItemToLayout(counterItem, index)
            }
        }

        // ➕ 아이템 추가 버튼
        addButton.setOnClickListener {
            if (viewModel.counters.value?.size ?: 0 >= MAX_ITEMS) {
                Toast.makeText(this, "최대 ${MAX_ITEMS}개까지만 추가 가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                showAddItemDialog()
            }
        }

        // ⚙️ 설정 버튼
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    // 아이템 추가 다이얼로그 (로직은 이전과 동일)
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

            viewModel.addCounter(CounterItem(name = name, colorRes = selectedColorRes!!))
            dialog.dismiss()
        }
    }

    // ----------------------------------------------------------------------------------
    // ✅ 아이템 뷰 추가 (ID 기반 업데이트 및 설정값 반영 로직)
    // ----------------------------------------------------------------------------------
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

        // 중앙 숫자 배경 (LayerDrawable 로직)
        val outerDrawable = resources.getDrawable(item.colorRes, theme).mutate()
        val innerDrawable = resources.getDrawable(R.drawable.bg_edittext_border, theme).mutate()
        val layerDrawable = LayerDrawable(arrayOf(outerDrawable, innerDrawable))
        layerDrawable.setLayerInset(0, 0, 0, 0, 0)
        layerDrawable.setLayerInset(1, 4, 4, 4, 4)
        itemValue.background = layerDrawable

        // 현재 값 표시
        itemValue.text = item.value.toString()

        // ➖ 버튼 클릭 (ID 기반 호출 및 decrementStep 반영)
        btnMinus.setOnClickListener {
            // item.decrementStep에 저장된 값만큼 감소
            val newValue = item.value - item.decrementStep
            viewModel.updateValueById(item.id, newValue) // 🚨 ID 기반 업데이트
        }

        // ➕ 버튼 클릭 (ID 기반 호출 및 incrementStep 반영)
        btnPlus.setOnClickListener {
            // item.incrementStep에 저장된 값만큼 증가
            val newValue = item.value + item.incrementStep
            viewModel.updateValueById(item.id, newValue) // 🚨 ID 기반 업데이트
        }

        // 중앙 숫자 클릭 -> 확대 액티비티 이동 (5단계에서 ID 기반으로 변경 완료)
        itemValue.setOnClickListener {
            val intent = Intent(this, NumberZoomActivity::class.java)
            intent.putExtra("itemId", item.id) // 🚨 고유 ID 전달
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        container.addView(itemView)
    }
}