package com.clicker

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import androidx.lifecycle.MutableLiveData // LiveData 캐스팅을 위해 필요

class NumberZoomActivity : AppCompatActivity() {

    private lateinit var viewModel: CounterViewModel
    private var itemId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_number_zoom)

        // 뷰 초기화 (생략)
        val zoomNumber = findViewById<TextView>(R.id.zoomNumber)
        val btnMinus = findViewById<TextView>(R.id.btnMinus)
        val btnPlus = findViewById<TextView>(R.id.btnPlus)
        val btnDelete = findViewById<ImageButton>(R.id.btnBack)
        val btnReset = findViewById<ImageButton>(R.id.btnReset)
        val btnEdit = findViewById<ImageButton>(R.id.btnEdit)
        val btnCustom1 = findViewById<MaterialButton>(R.id.btnCustom1)
        val btnCustom2 = findViewById<MaterialButton>(R.id.btnCustom2)
        val btnCustom3 = findViewById<MaterialButton>(R.id.btnCustom3)
        val btnCustom4 = findViewById<MaterialButton>(R.id.btnCustom4)
        val customButtons = listOf(btnCustom1, btnCustom2, btnCustom3, btnCustom4)

        // intent에서 ID 가져오기 (생략)
        itemId = intent.getStringExtra("itemId")
        if (itemId == null) {
            finish()
            return
        }

        viewModel = (application as MyApplication).counterViewModel

        // ViewModel.counters 관찰 → UI 갱신 (오류 없음)
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

        // ----------------------------------------------------------------------
        // ✅ ViewModel의 유효성 검사 메시지 관찰 및 Toast 띄우기 (오류 해결)
        // ----------------------------------------------------------------------
        viewModel.validationMessage.observe(this) { message: String? -> // 타입 명시
            if (!message.isNullOrEmpty()) {
                // Toast 오버로드 모호성 해결을 위해 CharSequence로 캐스팅
                Toast.makeText(this, message as CharSequence, Toast.LENGTH_LONG).show()

                // ViewModel에 추가한 함수를 호출하여 메시지 초기화
                viewModel.clearValidationMessage()
            }
        }

        // 1. 숫자 감소 (함수명 수정 완료)
        btnMinus.setOnClickListener {
            triggerVibration()
            val item = viewModel.counters.value?.find { it.id == itemId } ?: return@setOnClickListener
            val newValue = item.value - item.decrementStep
            viewModel.updateValueById(item.id, newValue)
        }

        // 2. 숫자 증가 (함수명 수정 완료)
        btnPlus.setOnClickListener {
            triggerVibration()
            val item = viewModel.counters.value?.find { it.id == itemId } ?: return@setOnClickListener
            val newValue = item.value + item.incrementStep
            viewModel.updateValueById(item.id, newValue)
        }

        // 3. 숫자 초기화 (함수명 수정 완료)
        btnReset.setOnClickListener {
            val item = viewModel.counters.value?.find { it.id == itemId } ?: return@setOnClickListener
            viewModel.updateValueById(item.id, item.minValue)
        }

        // 4. 삭제 버튼 클릭 (함수명 수정 완료)
        btnDelete.setOnClickListener {
            viewModel.removeCounterById(itemId!!)
            finish()
        }

        // 5. 설정 버튼 클릭
        btnEdit.setOnClickListener {
            showEditSettingsDialog()
        }

        // 6. 커스텀 버튼 클릭 리스너 (함수명 수정 완료)
        customButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                triggerVibration()
                val item = viewModel.counters.value?.find { it.id == itemId } ?: return@setOnClickListener
                val step = item.customSteps.getOrNull(index) ?: 0
                val newValue = item.value + step
                viewModel.updateValueById(item.id, newValue)
            }
        }

        // 숫자 클릭 시 종료 (생략)
        zoomNumber.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    // ----------------------------------------------------------------------
    // 짧은 진동 함수 (유지)
    // ----------------------------------------------------------------------
    private fun triggerVibration() {
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val vibrationEnabled = prefs.getBoolean("vibration_enabled", true)
        if (!vibrationEnabled) return

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
    // 설정 다이얼로그 함수 (UI 오류 표시 로직 적용)
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
        val textError = dialogView.findViewById<TextView>(R.id.textError) // 오류 메시지 뷰 초기화

        // 색상 뷰 초기화 (생략)
        val colorRed = dialogView.findViewById<ImageView>(R.id.colorRed)
        val colorBlue = dialogView.findViewById<ImageView>(R.id.colorBlue)
        val colorGreen = dialogView.findViewById<ImageView>(R.id.colorGreen)
        val colorYellow = dialogView.findViewById<ImageView>(R.id.colorYellow)
        val colorPurple = dialogView.findViewById<ImageView>(R.id.colorPurple)
        val colorViews = listOf(colorRed, colorBlue, colorGreen, colorYellow, colorPurple)

        var selectedColorRes = currentItem.colorRes

        // 1. 기존 데이터 채우기 (최솟값/최댓값 조건부 표시)
        editName.setText(currentItem.name)
        editDecStep.setText(currentItem.decrementStep.toString())
        editIncStep.setText(currentItem.incrementStep.toString())

        if (currentItem.minValue != 0) {
            editMinVal.setText(currentItem.minValue.toString())
        }
        if (currentItem.maxValue != 99999999) {
            editMaxVal.setText(currentItem.maxValue.toString())
        }

        editCustom1.setText(currentItem.customSteps.getOrNull(0)?.toString() ?: "")
        editCustom2.setText(currentItem.customSteps.getOrNull(1)?.toString() ?: "")
        editCustom3.setText(currentItem.customSteps.getOrNull(2)?.toString() ?: "")
        editCustom4.setText(currentItem.customSteps.getOrNull(3)?.toString() ?: "")

        // 2. 색상 선택 UI 구현 및 클릭 리스너 추가
        (colorViews as Iterable<ImageView>).forEach { v -> // 🚨 forEach 타입 모호성 해결
            val colorMapping = when (v.id) {
                R.id.colorRed -> R.drawable.bg_button_purple_blue
                R.id.colorBlue -> R.drawable.bg_button_pink_yellow
                R.id.colorGreen -> R.drawable.bg_button_blue_mint
                R.id.colorYellow -> R.drawable.bg_button_emerald_gold
                R.id.colorPurple -> R.drawable.bg_button_purple_pink
                else -> 0
            }
            if (colorMapping == selectedColorRes) {
                v.isSelected = true
            }

            v.setOnClickListener {
                colorViews.forEach { it.isSelected = false }
                v.isSelected = true
                selectedColorRes = colorMapping
            }
        }

        // 3. 다이얼로그 생성 및 표시
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        // 4. '확인' 버튼 클릭 리스너 (저장)
        btnConfirm.setOnClickListener {
            // 💡 오류 메시지를 초기화하고 숨김
            textError.visibility = View.GONE
            textError.text = ""

            // 💡 헬퍼 함수 정의: 다이얼로그 내 오류 표시
            fun showError(message: String) {
                textError.visibility = View.VISIBLE
                textError.text = message
            }

            // 입력 값 유효성 검사 및 파싱
            val name = editName.text.toString().trim()
            val maxValInputString = editMaxVal.text.toString().trim()

            val decStepInput = editDecStep.text.toString().toIntOrNull()
            val incStepInput = editIncStep.text.toString().toIntOrNull()
            val minValInput = editMinVal.text.toString().toIntOrNull()
            val maxValInput = maxValInputString.toIntOrNull()

            val decStep = decStepInput ?: currentItem.decrementStep
            val incStep = incStepInput ?: currentItem.incrementStep
            val minVal = minValInput ?: currentItem.minValue
            val maxVal = maxValInput ?: currentItem.maxValue

            val customSteps = listOf(
                editCustom1.text.toString().toIntOrNull() ?: currentItem.customSteps.getOrNull(0) ?: 0,
                editCustom2.text.toString().toIntOrNull() ?: currentItem.customSteps.getOrNull(1) ?: 0,
                editCustom3.text.toString().toIntOrNull() ?: currentItem.customSteps.getOrNull(2) ?: 0,
                editCustom4.text.toString().toIntOrNull() ?: currentItem.customSteps.getOrNull(3) ?: 0
            )


            // 5. 유효성 검사 (Toast 대신 showError 사용)
            if (name.isEmpty()) {
                showError("이름을 입력해주세요")
                return@setOnClickListener
            }
            if (maxValInputString.isNotEmpty() && maxValInputString.length > 8) {
                showError("최댓값은 8자리를 초과할 수 없습니다.")
                return@setOnClickListener
            }
            if (minValInput != null && minValInput < 0) {
                showError("최솟값은 음수가 될 수 없습니다.")
                return@setOnClickListener
            }
            if (decStepInput != null && decStepInput < 1) {
                showError("감소량은 1 이상이어야 합니다.")
                return@setOnClickListener
            }
            if (incStepInput != null && incStepInput < 1) {
                showError("증가량은 1 이상이어야 합니다.")
                return@setOnClickListener
            }
            if (minVal > maxVal) {
                showError("최솟값은 최댓값보다 클 수 없습니다.")
                return@setOnClickListener
            }
            if (customSteps.any { it < 0 }) {
                showError("커스텀 값은 음수가 될 수 없습니다.")
                return@setOnClickListener
            }


            // ViewModel의 설정 저장 함수 호출 (오류가 없으면 저장)
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