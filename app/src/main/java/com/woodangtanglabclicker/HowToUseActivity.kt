package com.woodangtanglabclicker

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

// ----------------------------------------------------------------------
// 앱의 사용 방법(도움말) 화면을 표시하는 Activity
// ----------------------------------------------------------------------
class HowToUseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_to_use)

        // 닫기 버튼 (화면 종료)
        val closeButton: Button = findViewById(R.id.button_close)
        closeButton.setOnClickListener {
            finish()
        }
    }
}
