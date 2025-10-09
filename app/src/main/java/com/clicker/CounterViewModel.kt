package com.clicker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.UUID

// ----------------------------------------------------------------------
// CounterItem 데이터 모델 (1단계 반영)
// ----------------------------------------------------------------------
data class CounterItem(
    // ID: 데이터 안정성을 위한 고유 ID (생성 시 자동 부여)
    var id: String = UUID.randomUUID().toString(),

    var name: String,
    var value: Int = 0,
    var colorRes: Int,

    // 설정 필드 (3단계 설정 반영)
    var decrementStep: Int = 1, // 감소량
    var incrementStep: Int = 1, // 증가량
    var minValue: Int = 0,      // 최솟값
    var maxValue: Int = 99999999,    // 최댓값

    // 커스텀 추가값 버튼 필드
    val customSteps: MutableList<Int> = mutableListOf(10, 50, 100, 1000)
)

// ----------------------------------------------------------------------
// CounterViewModel 클래스
// ----------------------------------------------------------------------
class CounterViewModel : ViewModel() { // 표준 ViewModel 상속

    private val _counters = MutableLiveData<MutableList<CounterItem>>(mutableListOf())
    val counters: LiveData<MutableList<CounterItem>> = _counters

    // LiveData 옵저버에게 변경을 알리는 헬퍼 함수
    private fun <T> MutableLiveData<T>.notifyObserver() {
        // LiveData 값을 다시 설정하여 옵저버가 변경을 감지하도록 함
        this.value = this.value //T 타입에 대해 안전하게 쓰기 작업 수행 가능
    }

    // 카운터 추가 (addCounter)
    fun addCounter(item: CounterItem) {
        _counters.value?.add(item)
        _counters.notifyObserver()
    }

    // ----------------------------------------------------------------------
    // 6단계: ID 기반 값 갱신 및 유효성 검사 (핵심 안전 로직)
    // ----------------------------------------------------------------------
    /**
     * 특정 카운터 아이템의 값을 ID를 사용하여 갱신하고 최솟값/최댓값 범위 내로 제한합니다.
     * @param id 갱신할 CounterItem의 고유 ID
     * @param newValue 새로운 값
     */
    fun updateValueById(id: String, newValue: Int) {
        // ID를 사용하여 아이템을 찾고, 없으면 함수 종료
        val item = _counters.value?.find { it.id == id } ?: return

        // 💡 coerceIn을 사용하여 item에 설정된 범위 내로 값을 제한 (유효성 검사)
        item.value = newValue.coerceIn(item.minValue, item.maxValue)

        _counters.notifyObserver()
    }

    // ----------------------------------------------------------------------
    // 6단계: ID 기반 삭제
    // ----------------------------------------------------------------------
    /**
     * 특정 카운터 아이템을 ID를 사용하여 삭제합니다.
     * @param id 삭제할 CounterItem의 고유 ID
     */
    fun removeCounterById(id: String) {
        val list = _counters.value
        val itemToRemove = list?.find { it.id == id } // ID로 객체를 찾음

        if (itemToRemove != null) {
            list.remove(itemToRemove) // 찾은 객체를 리스트에서 삭제
            _counters.notifyObserver()
        }
    }

    // ----------------------------------------------------------------------
    // 3단계: 카운터 설정 전체 갱신 (updateCounterSettings)
    // ----------------------------------------------------------------------
    /**
     * 특정 카운터 아이템의 모든 설정 값(이름, 색상, 증감량, 범위, 커스텀 값)을 갱신합니다.
     */
    fun updateCounterSettings(
        id: String,
        newName: String,
        newColorRes: Int,
        newDecrementStep: Int,
        newIncrementStep: Int,
        newMinValue: Int,
        newMaxValue: Int,
        newCustomSteps: List<Int> // 4개의 커스텀 값이 들어있는 리스트
    ) {
        val itemToUpdate = _counters.value?.find { it.id == id }

        itemToUpdate?.apply {
            // 새 설정 값들을 아이템에 반영
            name = newName
            colorRes = newColorRes
            decrementStep = newDecrementStep
            incrementStep = newIncrementStep
            minValue = newMinValue
            maxValue = newMaxValue

            // 커스텀 버튼 값 갱신
            customSteps.clear()
            customSteps.addAll(newCustomSteps)

            // ✅ 설정 변경 후, 현재 값(value)이 새로운 범위 내에 있는지 확인하고 조정
            value = value.coerceIn(minValue, maxValue)
        }

        _counters.notifyObserver()
    }
}

