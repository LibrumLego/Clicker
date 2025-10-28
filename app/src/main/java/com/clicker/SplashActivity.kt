package com.clicker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

// ----------------------------------------------------------------------
// SplashActivity : 앱 실행 시 로고 화면 (2초 후 메인으로 이동)
// ----------------------------------------------------------------------
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 상태바 투명 처리
        window.statusBarColor = Color.TRANSPARENT

        // 2초 뒤 메인 화면으로 전환
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }
}
