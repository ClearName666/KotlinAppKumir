package com.example.testappusb.gestures

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs


// класс для обработки свайпов
class SwipeGestureDetector(val context: Context) : GestureDetector.SimpleOnGestureListener() {
    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100

    override fun onDown(e: MotionEvent): Boolean {
        return true // Всегда возвращаем true для обработки всех жестов
    }

    // обработка свайпов
    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (e1 != null) {
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x

            if (abs(diffX) > abs(diffY) &&
                abs(diffX) > swipeThreshold &&
                abs(velocityX) > swipeVelocityThreshold) {

                // отправка данных о свайпе в активность
                if (context is SwipeGestureDetectorIntarface) {
                    context.onSwipeAction(diffX > 0)
                }
                return true
            }
        }
        return false
    }
}
