package com.clicker

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

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

        // 커스텀 버튼 초기화
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

        // ViewModel 관찰 → UI 갱신
        viewModel.counters.observe(this) { list ->
            val item = list.find { it.id == itemId }

            if (item == null) {
                finish()
                return@observe
            }

            zoomNumber.text = item.value.toString()
            btnMinus.setBackgroundResource(item.colorRes)
            btnPlus.setBackgroundResource(item.colorRes)

            item.customSteps.forEachIndexed { index, step ->
                customButtons.getOrNull(index)?.text = step.toString()
            }
        }

        // ➖ 감소 버튼 → 진동 + 값 변경
        btnMinus.setOnClickListener {
            triggerVibration()
            val item =
                viewModel.counters.value?.find { it.id == itemId } ?: return@setOnClickListener
            val newValue = item.value - item.decrementStep
            viewModel.updateValueById(item.id, newValue)
        }

        // ➕ 증가 버튼 → 진동 + 값 변경
        btnPlus.setOnClickListener {
            triggerVibration()
            val item =
                viewModel.counters.value?.find { it.id == itemId } ?: return@setOnClickListener
            val newValue = item.value + item.incrementStep
            viewModel.updateValueById(item.id, newValue)
        }

        // 초기화 버튼 (진동 없음)
        btnReset.setOnClickListener {
            val item =
                viewModel.counters.value?.find { it.id == itemId } ?: return@setOnClickListener
            viewModel.updateValueById(item.id, item.minValue)
        }

        // 삭제 버튼 (진동 없음)
        btnDelete.setOnClickListener {
            viewModel.removeCounterById(itemId!!)
            finish()
        }

        // 설정 버튼 (진동 없음)
        btnEdit.setOnClickListener {
            showEditSettingsDialog()
        }

        // ✅ 커스텀 버튼 → 진동 + 증가
        customButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                triggerVibration()
                val item =
                    viewModel.counters.value?.find { it.id == itemId } ?: return@setOnClickListener
                val step = item.customSteps.getOrNull(index) ?: 0
                val newValue = item.value + step
                viewModel.updateValueById(item.id, newValue)
            }
        }

        // 숫자 클릭 시 종료 (진동 없음)
        zoomNumber.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    // ----------------------------------------------------------------------
    // ✅ 짧은 진동 함수 (➕ / ➖ / 커스텀 버튼용)
    // ----------------------------------------------------------------------
    private fun triggerVibration() {
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val vibrationEnabled = prefs.getBoolean("vibration_enabled", true)

        if (!vibrationEnabled) return // 진동 설정 OFF면 바로 종료

        val vibrator: Vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (!vibrator.hasVibrator()) return

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(40)
        }
    }

    // ----------------------------------------------------------------------
    // 기존 다이얼로그 함수 (유지)
    // ----------------------------------------------------------------------
// ----------------------------------------------------------------------
    // 설정 다이얼로그 함수 (최종 수정: 최댓값 8자리 제한 추가)
    // ----------------------------------------------------------------------
    private fun showEditSettingsDialog() {
        val currentItem = viewModel.counters.value?.find { it.id == itemId } ?: return

        // dialog_edit_settings.xml 레이아웃 사용
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_settings, null)

        // 뷰 초기화
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

        // 색상 뷰 초기화
        val colorRed = dialogView.findViewById<ImageView>(R.id.colorRed)
        val colorBlue = dialogView.findViewById<ImageView>(R.id.colorBlue)
        val colorGreen = dialogView.findViewById<ImageView>(R.id.colorGreen)
        val colorYellow = dialogView.findViewById<ImageView>(R.id.colorYellow)
        val colorPurple = dialogView.findViewById<ImageView>(R.id.colorPurple)
        val colorViews = listOf(colorRed, colorBlue, colorGreen, colorYellow, colorPurple)

        var selectedColorRes = currentItem.colorRes // 현재 색상으로 초기값 설정

        // 1. 기존 데이터 채우기 (최솟값/최댓값 조건부 표시)
        editName.setText(currentItem.name)
        editDecStep.setText(currentItem.decrementStep.toString())
        editIncStep.setText(currentItem.incrementStep.toString())

        // 최솟값: 저장된 값이 기본값(0)이 아닐 때만 필드에 채움
        if (currentItem.minValue != 0) {
            editMinVal.setText(currentItem.minValue.toString())
        }

        // 최댓값: 저장된 값이 기본값(99999999)이 아닐 때만 필드에 채움
        if (currentItem.maxValue != 99999999) {
            editMaxVal.setText(currentItem.maxValue.toString())
        }

        editCustom1.setText(currentItem.customSteps.getOrNull(0)?.toString() ?: "")
        editCustom2.setText(currentItem.customSteps.getOrNull(1)?.toString() ?: "")
        editCustom3.setText(currentItem.customSteps.getOrNull(2)?.toString() ?: "")
        editCustom4.setText(currentItem.customSteps.getOrNull(3)?.toString() ?: "")

        // 2. 색상 선택 UI 구현 및 클릭 리스너 추가
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

        // 3. 다이얼로그 생성 및 표시
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        // 4. '확인' 버튼 클릭 리스너 (저장)
        btnConfirm.setOnClickListener {
            // 입력 값 유효성 검사 및 파싱
            val name = editName.text.toString().trim()

            // 🚨 최댓값 자리수 검사 (8자리 초과 금지)
            val maxValInputString = editMaxVal.text.toString().trim()
            if (maxValInputString.isNotEmpty() && maxValInputString.length > 8) {
                Toast.makeText(this, "최댓값은 8자리를 초과할 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 💡 입력 값 (Input)과 대체 값 (Final Value) 분리
            val decStepInput = editDecStep.text.toString().toIntOrNull()
            val incStepInput = editIncStep.text.toString().toIntOrNull()
            val minValInput = editMinVal.text.toString().toIntOrNull()
            // 8자리 검사를 통과한 문자열을 Int로 파싱
            val maxValInput = maxValInputString.toIntOrNull()

            // 🚨 파싱: 입력 값이 없거나 숫자가 아니면 currentItem의 기존 값(현재 저장된 값)을 사용
            val decStep = decStepInput ?: currentItem.decrementStep
            val incStep = incStepInput ?: currentItem.incrementStep
            val minVal = minValInput ?: currentItem.minValue
            val maxVal = maxValInput ?: currentItem.maxValue

            // 커스텀 값 리스트 파싱 (입력 없으면 기존 값 사용)
            val customSteps = listOf(
                editCustom1.text.toString().toIntOrNull() ?: currentItem.customSteps.getOrNull(0) ?: 0,
                editCustom2.text.toString().toIntOrNull() ?: currentItem.customSteps.getOrNull(1) ?: 0,
                editCustom3.text.toString().toIntOrNull() ?: currentItem.customSteps.getOrNull(2) ?: 0,
                editCustom4.text.toString().toIntOrNull() ?: currentItem.customSteps.getOrNull(3) ?: 0
            )


            // 5. 유효성 검사 (나머지 검사)
            if (name.isEmpty()) {
                Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 💡 최솟값 음수 검사
            if (minValInput != null && minValInput < 0) {
                Toast.makeText(this, "최솟값은 음수가 될 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 💡 감소량 1 미만 검사
            if (decStepInput != null && decStepInput < 1) {
                Toast.makeText(this, "감소량은 1 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 💡 증가량 1 미만 검사
            if (incStepInput != null && incStepInput < 1) {
                Toast.makeText(this, "증가량은 1 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 최솟값이 최댓값보다 클 수 없음 (최종 값으로 검사)
            if (minVal > maxVal) {
                Toast.makeText(this, "최솟값은 최댓값보다 클 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 커스텀 스텝 음수 검사
            if (customSteps.any { it < 0 }) {
                Toast.makeText(this, "커스텀 값은 음수가 될 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // ViewModel의 설정 저장 함수 호출 (ID 사용)
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