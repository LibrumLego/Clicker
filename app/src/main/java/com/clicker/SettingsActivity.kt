package com.clicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

// AdMob
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

// ----------------------------------------------------------------------
// SettingsActivity : 앱 설정 화면
// ----------------------------------------------------------------------
class SettingsActivity : AppCompatActivity() {

    private lateinit var vibrationSwitch: Switch
    private lateinit var adView: AdView

    private val PREFS_NAME = "AppSettings"
    private val VIBRATION_KEY = "vibration_enabled"

    // ------------------------------------------------------------------
    // onCreate
    // ------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // SharedPreferences (앱 설정 저장)
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // 🔘 사용 방법 버튼
        val howToUseButton: MaterialButton = findViewById(R.id.button_how_to_use)
        howToUseButton.setOnClickListener {
            startActivity(Intent(this, HowToUseActivity::class.java))
        }

        // 리뷰 버튼 (Play 스토어 이동)
        val reviewButton: MaterialButton = findViewById(R.id.button_review)
        reviewButton.setOnClickListener {
            try {
                // Play 스토어 앱으로 이동
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                startActivity(intent)
            } catch (e: android.content.ActivityNotFoundException) {
                // Play 스토어 앱이 없을 경우 → 웹 브라우저로 열기
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
                startActivity(intent)
            }
        }

        // 진동 설정 스위치
        vibrationSwitch = findViewById(R.id.switch_vibration)

        // 저장된 진동 설정 불러오기 (기본값 true)
        vibrationSwitch.isChecked = prefs.getBoolean(VIBRATION_KEY, true)

        // 상태 변경 시 즉시 저장
        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(VIBRATION_KEY, isChecked).apply()
        }

        // 광고 초기화 및 로드
        MobileAds.initialize(this)
        adView = findViewById(R.id.adView_settings)
        adView.loadAd(AdRequest.Builder().build())
    }
}