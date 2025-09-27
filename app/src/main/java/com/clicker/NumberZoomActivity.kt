package com.clicker

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NumberZoomActivity : AppCompatActivity() {

    private lateinit var viewModel: CounterViewModel
    private var index = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_number_zoom)

        val zoomNumber = findViewById<TextView>(R.id.zoomNumber)
        val btnMinus = findViewById<TextView>(R.id.btnMinus)
        val btnPlus = findViewById<TextView>(R.id.btnPlus)

        index = intent.getIntExtra("index", -1)
        if (index == -1) finish()

        viewModel = (application as MyApplication).counterViewModel

        viewModel.counters.observe(this) { list ->
            val item = list[index]
            zoomNumber.text = item.value.toString()
            btnMinus.setBackgroundResource(item.colorRes)
            btnPlus.setBackgroundResource(item.colorRes)
        }

        btnMinus.setOnClickListener {
            val current = viewModel.counters.value?.get(index)?.value ?: 0
            if (current > 0) viewModel.updateValue(index, current - 1)
        }
        btnPlus.setOnClickListener {
            val current = viewModel.counters.value?.get(index)?.value ?: 0
            viewModel.updateValue(index, current + 1)
        }

        zoomNumber.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}


