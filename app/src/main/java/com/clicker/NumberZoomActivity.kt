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

// 🔑 광고 import
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class NumberZoomActivity : AppCompatActivity() {

    private lateinit var viewModel: CounterViewModel
    private var itemId: String? = null // ID 기반으로 변경됨

    // ✅ 광고 뷰
    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_number_zoom)

        // ✅ 광고 초기화 및 로드
        MobileAds.initialize(this) {}
        adView = findViewById(R.id.adView_zoom)  // XML에서 추가한 AdView ID
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

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

        // ➖ 감소 버튼
        btnMinus.setOnClickListener {
            triggerVibration()
            val item = viewModel.counters.value?.find { it.id == itemId } ?: return@setOnClickListener
            val newValue = item.value - item.decrementStep
            viewModel.updateValueById(item.id, newValue)
        }

        // ➕ 증가 버튼
        btnPlus.setOnClickListener {
            triggerVibration()
            val item = viewModel.counters.value?.find { it.id == itemId } ?: return@setOnClickListener
            val newValue = item.value + item.incrementStep
            viewModel.updateValueById(item.id, newValue)
        }

        // 초기화 버튼
        btnReset.setOnClickListener {
            val item = viewModel.counters.value?.find { it.id == itemId } ?: return@setOnClickListener
            viewModel.updateValueById(item.id, item.minValue)
        }

        // 삭제 버튼
        btnDelete.setOnClickListener {
            viewModel.removeCounterById(itemId!!)
            finish()
        }

        // 설정 버튼
        btnEdit.setOnClickListener {
            showEditSettingsDialog()
        }

        // ✅ 커스텀 버튼 → 진동 + 증가
        customButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                triggerVibration()
                val item = viewModel.counters.value?.find { it.id == itemId } ?: return@setOnClickListener
                val step = item.customSteps.getOrNull(index) ?: 0
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
    // ✅ 짧은 진동 함수
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
    // ✅ 설정 다이얼로그
    // ----------------------------------------------------------------------
    private fun showEditSettingsDialog() {
        // (기존 다이얼로그 코드 그대로 유지)
        // ...
    }
}
