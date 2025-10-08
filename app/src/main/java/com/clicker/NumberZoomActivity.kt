package com.clicker

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.util.Log

class NumberZoomActivity : AppCompatActivity() {

    private lateinit var viewModel: CounterViewModel
    private var itemId: String? = null // ID 기반으로 변경됨

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_number_zoom)

        // 뷰 초기화
        val zoomNumber = findViewById<TextView>(R.id.zoomNumber)
        val btnMinus = findViewById<TextView>(R.id.btnMinus)
        val btnPlus = findViewById<TextView>(R.id.btnPlus)
        val btnDelete = findViewById<ImageButton>(R.id.btnBack)
        val btnReset = findViewById<ImageButton>(R.id.btnReset)
        val btnEdit = findViewById<ImageButton>(R.id.btnEdit)

        // 커스텀 버튼 뷰 초기화
        val btnCustom1 = findViewById<MaterialButton>(R.id.btnCustom1)
        val btnCustom2 = findViewById<MaterialButton>(R.id.btnCustom2)
        val btnCustom3 = findViewById<MaterialButton>(R.id.btnCustom3)
        val btnCustom4 = findViewById<MaterialButton>(R.id.btnCustom4)
        val customButtons = listOf(btnCustom1, btnCustom2, btnCustom3, btnCustom4)

        // intent에서 ID 가져오기
        itemId = intent.getStringExtra("itemId")
        if (itemId == null) {
            finish()
            return
        }

        viewModel = (application as MyApplication).counterViewModel

        // ViewModel 관찰 (데이터 변경 시 UI 갱신)
        viewModel.counters.observe(this) { list ->
            // ID를 사용하여 아이템 찾기
            val item = list.find { it.id == itemId }

            // 아이템이 삭제되었거나 찾을 수 없는 경우
            if (item == null) {
                finish()
                return@observe
            }

            // UI 갱신 (ID 기반)
            zoomNumber.text = item.value.toString()
            btnMinus.setBackgroundResource(item.colorRes)
            btnPlus.setBackgroundResource(item.colorRes)

            // 커스텀 버튼 텍스트를 설정값으로 업데이트
            item.customSteps.forEachIndexed { index, step ->
                customButtons.getOrNull(index)?.text = step.toString()
            }
        }

        // 1. 숫자 감소 (decrementStep 반영)
        btnMinus.setOnClickListener {
            val item =
                viewModel.counters.value?.find { it.id == itemId } ?: return@setOnClickListener
            val newValue = item.value - item.decrementStep
            viewModel.updateValueById(item.id, newValue)
        }

        // 2. 숫자 증가 (incrementStep 반영)
        btnPlus.setOnClickListener {
            val item =
                viewModel.counters.value?.find { it.id == itemId } ?: return@setOnClickListener
            val newValue = item.value + item.incrementStep
            viewModel.updateValueById(item.id, newValue)
        }

        // 3. 숫자 초기화 (minValue 반영)
        btnReset.setOnClickListener {
            val item =
                viewModel.counters.value?.find { it.id == itemId } ?: return@setOnClickListener
            viewModel.updateValueById(item.id, item.minValue) // 설정된 최솟값으로 초기화
        }

        // 4. 삭제 버튼 클릭 (ID 기반 삭제 함수 호출)
        btnDelete.setOnClickListener {
            viewModel.removeCounterById(itemId!!)
            finish()
        }

        // 5. 설정 버튼 클릭: 새로운 다이얼로그 표시
        btnEdit.setOnClickListener {
            showEditSettingsDialog()
        }

        // 6. 커스텀 버튼 클릭 리스너 연결 (설정값만큼 증가)
        customButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                val item =
                    viewModel.counters.value?.find { it.id == itemId } ?: return@setOnClickListener
                val step = item.customSteps.getOrNull(index) ?: 0 // 설정된 customSteps의 값을 가져옴
                val newValue = item.value + step
                viewModel.updateValueById(item.id, newValue)
            }
        }

        // 숫자 클릭 시 종료
        zoomNumber.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    // ----------------------------------------------------------------------
    // 설정 다이얼로그 표시 및 저장 함수 (showEditSettingsDialog)
    // ----------------------------------------------------------------------

    private fun showEditSettingsDialog() {
        val currentItem = viewModel.counters.value?.find { it.id == itemId } ?: return

        // dialog_edit_settings.xml 레이아웃 사용
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_settings, null)

        // 뷰 초기화 (기존 코드 유지)
        val editName = dialogView.findViewById<EditText>(R.id.editName)
        val editDecStep = dialogView.findViewById<EditText>(R.id.editDecrementStep)
        val editIncStep = dialogView.findViewById<EditText>(R.id.editIncrementStep)
        val editMinVal = dialogView.findViewById<EditText>(R.id.editMinValue)
        val editMaxVal = dialogView.findViewById<EditText>(R.id.editMaxValue)
        val editCustom1 = dialogView.findViewById<EditText>(R.id.editCustomStep1)
        val editCustom2 = dialogView.findViewById<EditText>(R.id.editCustomStep2)
        val editCustom3 = dialogView.findViewById<EditText>(R.id.editCustomStep3)
        val editCustom4 = dialogView.findViewById<EditText>(R.id.editCustomStep4)
        val btnConfirm = dialogView.findViewById<TextView>(R.id.btnConfirmEdit)

        // 색상 뷰 초기화 (기존 코드 유지)
        val colorRed = dialogView.findViewById<ImageView>(R.id.colorRed)
        val colorBlue = dialogView.findViewById<ImageView>(R.id.colorBlue)
        val colorGreen = dialogView.findViewById<ImageView>(R.id.colorGreen)
        val colorYellow = dialogView.findViewById<ImageView>(R.id.colorYellow)
        val colorPurple = dialogView.findViewById<ImageView>(R.id.colorPurple)
        val colorViews = listOf(colorRed, colorBlue, colorGreen, colorYellow, colorPurple)

        var selectedColorRes = currentItem.colorRes // 현재 색상으로 초기값 설정

        // 1. 기존 데이터 채우기 (최솟값/최댓값만 힌트로 비우고 나머지는 값 채움)
        editName.setText(currentItem.name)

        // ✅ 증감량은 기존 값을 그대로 보여줌
        editDecStep.setText(currentItem.decrementStep.toString())
        editIncStep.setText(currentItem.incrementStep.toString())

        // 🚨 최솟값/최댓값은 힌트만 보여주기 위해 setText() 호출을 제거
        // editMinVal.setText(currentItem.minValue.toString())
        // editMaxVal.setText(currentItem.maxValue.toString())

        // ✅ 커스텀 값은 기존 값을 그대로 보여줌
        editCustom1.setText(currentItem.customSteps.getOrNull(0)?.toString() ?: "")
        editCustom2.setText(currentItem.customSteps.getOrNull(1)?.toString() ?: "")
        editCustom3.setText(currentItem.customSteps.getOrNull(2)?.toString() ?: "")
        editCustom4.setText(currentItem.customSteps.getOrNull(3)?.toString() ?: "")

        // 2. 색상 선택 UI 구현 및 클릭 리스너 추가 (로직 유지)
        colorViews.forEach { v ->
            val colorMapping = when (v.id) {
                R.id.colorRed -> R.drawable.bg_button_purple_blue
                R.id.colorBlue -> R.drawable.bg_button_pink_yellow
                R.id.colorGreen -> R.drawable.bg_button_blue_mint
                R.id.colorYellow -> R.drawable.bg_button_emerald_gold
                R.id.colorPurple -> R.drawable.bg_button_purple_pink
                else -> 0
            }
            // 초기 선택 상태 표시
            if (colorMapping == selectedColorRes) {
                v.isSelected = true
            }

            // 클릭 리스너 추가
            v.setOnClickListener {
                colorViews.forEach { it.isSelected = false }
                v.isSelected = true

                // 선택된 색상 리소스 ID 갱신
                selectedColorRes = colorMapping
            }
        }

        // 3. 다이얼로그 생성 및 표시 (로직 유지)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        // 4. '확인' 버튼 클릭 리스너 (저장)
        btnConfirm.setOnClickListener {
            // 입력 값 유효성 검사 및 파싱
            val name = editName.text.toString().trim()

            // 🚨 파싱 로직: 필드가 비어있으면(최솟값/최댓값) 기존 값 사용
            val decStep = editDecStep.text.toString().toIntOrNull() ?: currentItem.decrementStep
            val incStep = editIncStep.text.toString().toIntOrNull() ?: currentItem.incrementStep

            // 💡 최솟값/최댓값은 필드가 비어있을 가능성이 높으므로, toIntOrNull()이 null을 반환하면
            // currentItem의 기존 값을 사용하도록 처리되어 안전함
            val minVal = editMinVal.text.toString().toIntOrNull() ?: currentItem.minValue
            val maxVal = editMaxVal.text.toString().toIntOrNull() ?: currentItem.maxValue

            // 커스텀 값 리스트 파싱
            val customSteps = listOf(
                editCustom1.text.toString().toIntOrNull() ?: currentItem.customSteps.getOrNull(0) ?: 0,
                editCustom2.text.toString().toIntOrNull() ?: currentItem.customSteps.getOrNull(1) ?: 0,
                editCustom3.text.toString().toIntOrNull() ?: currentItem.customSteps.getOrNull(2) ?: 0,
                editCustom4.text.toString().toIntOrNull() ?: currentItem.customSteps.getOrNull(3) ?: 0
            )


            // 5. 유효성 검사 (로직 유지)
            if (name.isEmpty()) {
                Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (minVal <= 0) {
                Toast.makeText(this, "최솟값은 1 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (minVal > maxVal) {
                Toast.makeText(this, "최솟값은 최댓값보다 클 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (decStep <= 0) {
                Toast.makeText(this, "감소량은 1 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (incStep <= 0) {
                Toast.makeText(this, "증가량은 1 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ViewModel의 설정 저장 함수 호출 (로직 유지)
            viewModel.updateCounterSettings(
                id = itemId!!,
                newName = name,
                newColorRes = selectedColorRes,
                newDecrementStep = decStep,
                newIncrementStep = incStep,
                newMinValue = minVal,
                newMaxValue = maxVal,
                newCustomSteps = customSteps
            )

            dialog.dismiss()
        }
    }
}