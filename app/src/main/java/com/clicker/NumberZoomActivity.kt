package com.clicker

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NumberZoomActivity : AppCompatActivity() {

    private lateinit var viewModel: CounterViewModel
    private var index = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_number_zoom)

        // 뷰 초기화
        val zoomNumber = findViewById<TextView>(R.id.zoomNumber)
        val btnMinus = findViewById<TextView>(R.id.btnMinus)
        val btnPlus = findViewById<TextView>(R.id.btnPlus)
        val btnDelete = findViewById<ImageButton>(R.id.btnBack) // 삭제 버튼으로 사용
        val btnReset = findViewById<ImageButton>(R.id.btnReset)

        // intent에서 index 가져오기
        index = intent.getIntExtra("index", -1)
        if (index == -1) {
            finish()
            return
        }

        // ViewModel 초기화
        viewModel = (application as MyApplication).counterViewModel

        // ViewModel 관찰
        viewModel.counters.observe(this) { list ->
            // 삭제 후 index 유효성 체크
            if (index !in list.indices) return@observe

            val item = list[index]
            zoomNumber.text = item.value.toString()
            btnMinus.setBackgroundResource(item.colorRes)
            btnPlus.setBackgroundResource(item.colorRes)
        }

        // 숫자 감소
        btnMinus.setOnClickListener {
            val current = viewModel.counters.value?.getOrNull(index)?.value ?: 0
            if (current > 0) {
                viewModel.updateValue(index, current - 1)
                triggerVibration() // ✅ 진동
            }
        }

        // 숫자 증가
        btnPlus.setOnClickListener {
            val current = viewModel.counters.value?.getOrNull(index)?.value ?: 0
            viewModel.updateValue(index, current + 1)
            triggerVibration() // ✅ 진동
        }

        // 숫자 초기화
        btnReset.setOnClickListener {
            viewModel.updateValue(index, 0)
            triggerVibration() // ✅ 진동
        }

        // 삭제 버튼 클릭 -> 카운터 삭제 후 종료
        btnDelete.setOnClickListener {
            viewModel.removeCounter(index)
            triggerVibration() // ✅ 진동
            finish()
        }

        // 숫자 클릭 시 종료 (fade 효과)
        zoomNumber.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    // ✅ 진동 함수 (SettingsActivity의 스위치 설정 반영)
    private fun triggerVibration() {
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val vibrationEnabled = prefs.getBoolean("vibration_enabled", true)

        if (!vibrationEnabled) return  // 스위치 꺼져 있으면 진동 안 함

        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(40)
        }
    }
}
