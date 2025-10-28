package com.clicker

import android.app.Application

// ----------------------------------------------------------------------
// 앱 전체에서 공유되는 전역 ViewModel을 관리하는 Application 클래스
// ----------------------------------------------------------------------
class MyApplication : Application() {

    // 전역으로 하나의 CounterViewModel 인스턴스 유지
    val counterViewModel = CounterViewModel()
}