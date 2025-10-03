package com.clicker

import android.os.Bundle
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
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
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
            val item = list[index]
            zoomNumber.text = item.value.toString()
            btnMinus.setBackgroundResource(item.colorRes)
            btnPlus.setBackgroundResource(item.colorRes)
        }

        // 숫자 감소
        btnMinus.setOnClickListener {
            val current = viewModel.counters.value?.get(index)?.value ?: 0
            if (current > 0) viewModel.updateValue(index, current - 1)
        }

        // 숫자 증가
        btnPlus.setOnClickListener {
            val current = viewModel.counters.value?.get(index)?.value ?: 0
            viewModel.updateValue(index, current + 1)
        }

        // 숫자 초기화
        btnReset.setOnClickListener {
            viewModel.updateValue(index, 0)
        }

        // 뒤로가기 버튼
        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        // 숫자 클릭 시 종료 (fade 효과)
        zoomNumber.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}



