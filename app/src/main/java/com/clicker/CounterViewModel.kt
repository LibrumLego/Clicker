package com.clicker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class CounterItem(
    val name: String,
    var value: Int = 0,
    val colorRes: Int
)

class CounterViewModel : ViewModel() {

    private val _counters = MutableLiveData<MutableList<CounterItem>>(mutableListOf())
    val counters: LiveData<MutableList<CounterItem>> = _counters

    fun addCounter(item: CounterItem) {
        _counters.value?.add(item)
        _counters.notifyObserver()
    }

    fun updateValue(index: Int, newValue: Int) {
        _counters.value?.get(index)?.value = newValue
        _counters.notifyObserver()
    }

    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }
}

