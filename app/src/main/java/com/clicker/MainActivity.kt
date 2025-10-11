package com.clicker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.graphics.drawable.LayerDrawable
import android.graphics.Color
import android.text.TextWatcher
import android.text.Editable

// 🔑 AdMob import
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError

class MainActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var viewModel: CounterViewModel
    private val MAX_ITEMS = 10

    // ✅ 배너 광고
    private lateinit var adView: AdView

    // ✅ 전면 광고
    private var mInterstitialAd: InterstitialAd? = null

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-3940256099942544/1033173712", // ✅ 테스트용 전면 광고 ID
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    mInterstitialAd = ad
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }
            }
        )
    }

    private fun maybeShowAd(probability: Int) {
        // 🎲 확률(%)로 광고 띄우기
        if ((1..100).random() <= probability && mInterstitialAd != null) {
            mInterstitialAd?.show(this)
            mInterstitialAd = null
            loadInterstitialAd() // 다음 광고 준비
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ 상태바 완전 투명
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        // ✅ UI 초기화
        container = findViewById(R.id.container)
        val addButton: ImageButton = findViewById(R.id.addButton)
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)

        // ✅ 광고 초기화
        MobileAds.initialize(this) {}
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // ✅ 전면 광고 로드
        loadInterstitialAd()

        // ✅ ViewModel 연결
        viewModel = (application as MyApplication).counterViewModel

        // LiveData 관찰 → UI 자동 갱신
        viewModel.counters.observe(this) { list ->
            container.removeAllViews()
            list.forEachIndexed { index, counterItem ->
                addCounterItemToLayout(counterItem, index)
            }
        }

        // ➕ 아이템 추가 버튼
        addButton.setOnClickListener {
            if (viewModel.counters.value?.size ?: 0 >= MAX_ITEMS) {
                Toast.makeText(this, "최대 ${MAX_ITEMS}개까지만 추가 가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                showAddItemDialog()
                maybeShowAd(30) // ✅ 카운터 생성 시 30% 확률로 전면 광고
            }
        }

        // ⚙️ 설정 버튼
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    /**
     * ✅ 짧은 진동
     */
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

    /**
     * ✅ 새 아이템 추가 다이얼로그
     */
    private fun showAddItemDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)
        val editName = dialogView.findViewById<EditText>(R.id.editName)
        val btnConfirm = dialogView.findViewById<View>(R.id.btnConfirm)

        val colorRed = dialogView.findViewById<ImageView>(R.id.colorRed)
        val colorBlue = dialogView.findViewById<ImageView>(R.id.colorBlue)
        val colorGreen = dialogView.findViewById<ImageView>(R.id.colorGreen)
        val colorYellow = dialogView.findViewById<ImageView>(R.id.colorYellow)
        val colorPurple = dialogView.findViewById<ImageView>(R.id.colorPurple)

        val colorViews = listOf(colorRed, colorBlue, colorGreen, colorYellow, colorPurple)
        var selectedColorRes: Int? = null

        // ✅ 색상 선택
        colorViews.forEach { v ->
            v.setOnClickListener {
                colorViews.forEach { it.isSelected = false }
                v.isSelected = true
                selectedColorRes = when (v.id) {
                    R.id.colorRed -> R.drawable.bg_button_purple_blue
                    R.id.colorBlue -> R.drawable.bg_button_pink_yellow
                    R.id.colorGreen -> R.drawable.bg_button_blue_mint
                    R.id.colorYellow -> R.drawable.bg_button_emerald_gold
                    R.id.colorPurple -> R.drawable.bg_button_purple_pink
                    else -> null
                }
            }
        }

        val dialog = android.app.AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        // ✅ 글자 수 제한 (자음/모음 포함)
        editName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    val text = it.toString()

                    // 자음(ㄱ~ㅎ), 모음(ㅏ~ㅣ), 완성형(가~힣)
                    val koreanCount = text.count { ch ->
                        (ch in '가'..'힣') || (ch in 'ㄱ'..'ㅎ') || (ch in 'ㅏ'..'ㅣ')
                    }
                    val englishCount = text.count { ch -> ch in 'A'..'Z' || ch in 'a'..'z' }
                    val totalCount = text.length

                    // ✅ 한글만 입력
                    if (koreanCount == totalCount && koreanCount > 10) {
                        Toast.makeText(
                            dialogView.context,
                            "한글은 최대 10자까지 입력 가능합니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        editName.setText(text.dropLast(1))
                        editName.setSelection(editName.text.length)
                        return
                    }

                    // ✅ 영어만 입력
                    if (englishCount == totalCount && englishCount > 20) {
                        Toast.makeText(
                            dialogView.context,
                            "영문은 최대 20자까지 입력 가능합니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        editName.setText(text.dropLast(1))
                        editName.setSelection(editName.text.length)
                        return
                    }

                    // ✅ 혼합 입력 (한글 + 영어)
                    if (koreanCount > 0 && englishCount > 0 && totalCount > 12) {
                        Toast.makeText(
                            dialogView.context,
                            "한글+영문 혼합 시 최대 12자까지 입력 가능합니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        editName.setText(text.dropLast(1))
                        editName.setSelection(editName.text.length)
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // ✅ 확인 버튼
        btnConfirm.setOnClickListener {
            val name = editName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedColorRes == null) {
                Toast.makeText(this, "색상을 골라주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addCounter(CounterItem(name = name, colorRes = selectedColorRes!!))
            dialog.dismiss()
        }
    }

    /**
     * ✅ 카운터 아이템 UI 생성
     */
    private fun addCounterItemToLayout(item: CounterItem, index: Int) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_counter, container, false)

        val itemName = itemView.findViewById<TextView>(R.id.itemName)
        val btnMinus = itemView.findViewById<TextView>(R.id.btnMinus)
        val btnPlus = itemView.findViewById<TextView>(R.id.btnPlus)
        val itemValue = itemView.findViewById<TextView>(R.id.itemValue)

        itemName.text = item.name
        btnMinus.setBackgroundResource(item.colorRes)
        btnPlus.setBackgroundResource(item.colorRes)

        val outerDrawable = resources.getDrawable(item.colorRes, theme).mutate()
        val innerDrawable = resources.getDrawable(R.drawable.bg_edittext_border, theme).mutate()
        val layerDrawable = LayerDrawable(arrayOf(outerDrawable, innerDrawable))
        layerDrawable.setLayerInset(1, 4, 4, 4, 4)
        itemValue.background = layerDrawable

        itemValue.text = item.value.toString()

        // ➖ 버튼
        btnMinus.setOnClickListener {
            triggerVibration()
            val newValue = item.value - item.decrementStep
            viewModel.updateValueById(item.id, newValue)
        }

        // ➕ 버튼
        btnPlus.setOnClickListener {
            triggerVibration()
            val newValue = item.value + item.incrementStep
            viewModel.updateValueById(item.id, newValue)
        }

        // 숫자 클릭 → 확대 화면 이동 + 랜덤 광고
        itemValue.setOnClickListener {
            val intent = Intent(this, NumberZoomActivity::class.java)
            intent.putExtra("itemId", item.id)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            maybeShowAd(20) // ✅ 숫자 클릭 시 20% 확률로 전면 광고
        }

        container.addView(itemView)
    }
}
