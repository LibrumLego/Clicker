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
    private fun showEditSettingsDialog() {
        val currentItem = viewModel.counters.value?.find { it.id == itemId } ?: return
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_settings, null)

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

        val colorRed = dialogView.findViewById<ImageView>(R.id.colorRed)
        val colorBlue = dialogView.findViewById<ImageView>(R.id.colorBlue)
        val colorGreen = dialogView.findViewById<ImageView>(R.id.colorGreen)
        val colorYellow = dialogView.findViewById<ImageView>(R.id.colorYellow)
        val colorPurple = dialogView.findViewById<ImageView>(R.id.colorPurple)
        val colorViews = listOf(colorRed, colorBlue, colorGreen, colorYellow, colorPurple)

        var selectedColorRes = currentItem.colorRes

        editName.setText(currentItem.name)
        editDecStep.setText(currentItem.decrementStep.toString())
        editIncStep.setText(currentItem.incrementStep.toString())

        editCustom1.setText(currentItem.customSteps.getOrNull(0)?.toString() ?: "")
        editCustom2.setText(currentItem.customSteps.getOrNull(1)?.toString() ?: "")
        editCustom3.setText(currentItem.customSteps.getOrNull(2)?.toString() ?: "")
        editCustom4.setText(currentItem.customSteps.getOrNull(3)?.toString() ?: "")

        colorViews.forEach { v ->
            val colorMapping = when (v.id) {
                R.id.colorRed -> R.drawable.bg_button_purple_blue
                R.id.colorBlue -> R.drawable.bg_button_pink_yellow
                R.id.colorGreen -> R.drawable.bg_button_blue_mint
                R.id.colorYellow -> R.drawable.bg_button_emerald_gold
                R.id.colorPurple -> R.drawable.bg_button_purple_pink
                else -> 0
            }
            if (colorMapping == selectedColorRes) v.isSelected = true

            v.setOnClickListener {
                colorViews.forEach { it.isSelected = false }
                v.isSelected = true
                selectedColorRes = colorMapping
            }
        }

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        btnConfirm.setOnClickListener {
            val name = editName.text.toString().trim()
            val decStep = editDecStep.text.toString().toIntOrNull() ?: currentItem.decrementStep
            val incStep = editIncStep.text.toString().toIntOrNull() ?: currentItem.incrementStep
            val minVal = editMinVal.text.toString().toIntOrNull() ?: currentItem.minValue
            val maxVal = editMaxVal.text.toString().toIntOrNull() ?: currentItem.maxValue

            val customSteps = listOf(
                editCustom1.text.toString().toIntOrNull()
                    ?: currentItem.customSteps.getOrNull(0) ?: 0,
                editCustom2.text.toString().toIntOrNull()
                    ?: currentItem.customSteps.getOrNull(1) ?: 0,
                editCustom3.text.toString().toIntOrNull()
                    ?: currentItem.customSteps.getOrNull(2) ?: 0,
                editCustom4.text.toString().toIntOrNull()
                    ?: currentItem.customSteps.getOrNull(3) ?: 0
            )

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
