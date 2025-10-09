package com.clicker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.UUID

// ----------------------------------------------------------------------
// CounterItem 데이터 모델
// ----------------------------------------------------------------------
data class CounterItem(
    var id: String = UUID.randomUUID().toString(),
    var name: String,
    var value: Int = 0,
    var colorRes: Int,
    var decrementStep: Int = 1,
    var incrementStep: Int = 1,
    var minValue: Int = 0,      // 기본값 0 유지
    var maxValue: Int = 99999999,
    val customSteps: MutableList<Int> = mutableListOf(10, 50, 100, 1000)
)

// ----------------------------------------------------------------------
// CounterViewModel 클래스
// ----------------------------------------------------------------------
class CounterViewModel : ViewModel() {

    private val _counters = MutableLiveData<MutableList<CounterItem>>(mutableListOf())
    val counters: LiveData<MutableList<CounterItem>> = _counters

    // ✅ 유효성 검사 실패 메시지 LiveData (타입 명확)
    private val _validationMessage = MutableLiveData<String?>()
    val validationMessage: LiveData<String?> = _validationMessage // public으로 노출

    // LiveData 옵저버에게 변경을 알리는 헬퍼 함수 (타입 모호성 해결)
    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

    // ✅ Activity가 메시지를 소비했음을 알리는 함수 (오류 해결 핵심)
    fun clearValidationMessage() {
        _validationMessage.value = null
    }

    // 카운터 추가 (addCounter)
    fun addCounter(item: CounterItem) {
        _counters.value?.add(item)
        _counters.notifyObserver()
    }

    // ID 기반 값 갱신 및 유효성 검사
    fun updateValueById(id: String, newValue: Int) {
        val item = _counters.value?.find { it.id == id } ?: return
        item.value = newValue.coerceIn(item.minValue, item.maxValue)
        _counters.notifyObserver()
    }

    // ID 기반 삭제
    fun removeCounterById(id: String) {
        val list = _counters.value
        val itemToRemove = list?.find { it.id == id }

        if (itemToRemove != null) {
            list.remove(itemToRemove)
            _counters.notifyObserver()
        }
    }

    // 카운터 설정 전체 갱신 (updateCounterSettings)
    fun updateCounterSettings(
        id: String,
        newName: String,
        newColorRes: Int,
        newDecrementStep: Int,
        newIncrementStep: Int,
        newMinValue: Int,
        newMaxValue: Int,
        newCustomSteps: List<Int>
    ) {
        // 🚨 유효성 검사 실패 시 메시지 LiveData에 전달 (Activity가 Toast 표시)
        val hasNegativeStep = newCustomSteps.any { it < 0 }

        if (newDecrementStep < 1 || newIncrementStep < 1 || newMinValue < 0 || newMaxValue < 0 || hasNegativeStep) {
            _validationMessage.value = "설정 값은 음수일 수 없으며, 증감 값은 1 이상이어야 합니다."
            return
        }

        val itemToUpdate = _counters.value?.find { it.id == id }

        itemToUpdate?.apply {
            name = newName
            colorRes = newColorRes
            decrementStep = newDecrementStep
            incrementStep = newIncrementStep
            minValue = newMinValue
            maxValue = newMaxValue

            customSteps.clear()
            customSteps.addAll(newCustomSteps)

            value = value.coerceIn(minValue, maxValue)
        }

        _counters.notifyObserver()
    }
}

