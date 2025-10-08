package com.clicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {

    private lateinit var vibrationSwitch: Switch
    private val PREFS_NAME = "AppSettings"
    private val VIBRATION_KEY = "vibration_enabled"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // ✅ SharedPreferences (앱 전체 공용 설정 저장소)
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // ✅ 사용방법 버튼
        val howToUseButton: MaterialButton = findViewById(R.id.button_how_to_use)
        howToUseButton.setOnClickListener {
            startActivity(Intent(this, HowToUseActivity::class.java))
        }

        // ✅ 리뷰 버튼
        val reviewButton: MaterialButton = findViewById(R.id.button_review)
        reviewButton.setOnClickListener {
            try {
                // Play 스토어 앱으로 이동
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                startActivity(intent)
            } catch (e: android.content.ActivityNotFoundException) {
                // Play 스토어 앱이 없을 경우 웹 브라우저로 이동
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
                startActivity(intent)
            }
        }

        // ✅ 진동 스위치
        vibrationSwitch = findViewById(R.id.switch_vibration)

        // 저장된 진동 설정값 불러오기 (기본값 true)
        vibrationSwitch.isChecked = prefs.getBoolean(VIBRATION_KEY, true)

        // 스위치 상태 변경 시 SharedPreferences에 즉시 반영
        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(VIBRATION_KEY, isChecked).apply()

            if (isChecked) {
                // ✅ 진동 기능 활성화됨
                // (MainActivity 등에서 triggerVibration() 호출 시 작동)
            } else {
                // 🚫 진동 기능 비활성화됨
                // (버튼 눌러도 진동 안 울림)
            }
        }
    }
}
