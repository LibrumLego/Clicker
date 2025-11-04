package com.woodangtanglabclicker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import androidx.core.net.toUri
import androidx.core.content.edit
import androidx.appcompat.widget.SwitchCompat


// AdMob
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

// ----------------------------------------------------------------------
// SettingsActivity : 앱 설정 화면
// ----------------------------------------------------------------------
class SettingsActivity : AppCompatActivity() {

    private lateinit var vibrationSwitch: SwitchCompat
    private lateinit var adView: AdView

    private val prefsName = "AppSettings"
    private val vibrationKey = "vibration_enabled"

    // ------------------------------------------------------------------
    // onCreate
    // ------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // SharedPreferences (앱 설정 저장)
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)

        // 🔘 사용 방법 버튼
        val howToUseButton: MaterialButton = findViewById(R.id.button_how_to_use)
        howToUseButton.setOnClickListener {
            startActivity(Intent(this, HowToUseActivity::class.java))
        }

        // 리뷰 버튼 (Play 스토어 이동)
        val reviewButton: MaterialButton = findViewById(R.id.button_review)
        reviewButton.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
                startActivity(intent)
            } catch (_: android.content.ActivityNotFoundException) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=$packageName".toUri()
                )
                startActivity(intent)
            }
        }

        // ✅ 개인정보처리방침 버튼
        val privacyButton: MaterialButton = findViewById(R.id.button_privacy_policy)
        privacyButton.setOnClickListener {
            val url = "https://cute-burst-24b.notion.site/Clicker-2a0fb9e35b7880fdb094d1b71be1a443"
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        }

        // 진동 설정 스위치
        vibrationSwitch = findViewById(R.id.switch_vibration)

        // 저장된 진동 설정 불러오기 (기본값 true)
        vibrationSwitch.isChecked = prefs.getBoolean(vibrationKey, true)

        // 상태 변경 시 즉시 저장
        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit {
                putBoolean(vibrationKey, isChecked)
            }
        }

        // 광고 초기화 및 로드
        MobileAds.initialize(this)
        adView = findViewById(R.id.adView_settings)
        adView.loadAd(AdRequest.Builder().build())
    }
}