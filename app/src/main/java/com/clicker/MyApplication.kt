package com.clicker

import android.app.Application

class MyApplication : Application() {
    val counterViewModel = CounterViewModel()
}

