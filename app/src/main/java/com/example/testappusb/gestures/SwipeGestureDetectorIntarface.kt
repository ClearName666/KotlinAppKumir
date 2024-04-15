package com.example.testappusb.gestures

// интерфейс для обработки жеста в активности
interface SwipeGestureDetectorIntarface {
    fun onSwipeAction(flagDirection: Boolean) // false лево свайп true право свайп
}