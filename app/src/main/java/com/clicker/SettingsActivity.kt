package com.clicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // ✅ 1️⃣ SharedPreferences 객체 생성 (앱 전체에서 설정 저장용)
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)

        // ✅ 2️⃣ 사용방법 버튼
        val howToUseButton: MaterialButton = findViewById(R.id.button_how_to_use)
        howToUseButton.setOnClickListener {
            // "사용방법" 화면으로 이동
            val intent = Intent(this, HowToUseActivity::class.java)
            startActivity(intent)
        }

        // ✅ 3️⃣ 리뷰 버튼 (Play 스토어 연결)
        val reviewButton: MaterialButton = findViewById(R.id.button_review)
        reviewButton.setOnClickListener {
            try {
                // Play 스토어 앱으로 연결 시도
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                startActivity(intent)
            } catch (e: android.content.ActivityNotFoundException) {
                // Play 스토어 앱이 없을 경우, 웹 브라우저로 연결
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
                startActivity(intent)
            }
        }

        // ✅ 4️⃣ 진동 스위치
        val vibrationSwitch: Switch = findViewById(R.id.switch_vibration)

        // 저장된 진동 설정 값 불러오기 (기본값: true)
        vibrationSwitch.isChecked = prefs.getBoolean("vibration_enabled", true)

        // 스위치 상태 변경 시 SharedPreferences에 저장
        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            // 설정값을 즉시 저장 (commit 대신 apply → 비동기 처리)
            prefs.edit().putBoolean("vibration_enabled", isChecked).apply()

            if (isChecked) {
                // ✅ 진동 기능 활성화됨
                // (MainActivity에서 버튼 클릭 시 진동 발생)
            } else {
                // 🚫 진동 기능 비활성화됨
                // (MainActivity에서 버튼 클릭 시 진동 없음)
            }
        }
    }
}
