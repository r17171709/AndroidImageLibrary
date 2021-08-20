package com.stfalcon.imageviewer.common.gestures.direction

internal enum class SwipeDirection {
    NOT_DETECTED,
    UP,
    DOWN,
    LEFT,
    RIGHT;

    companion object {
        fun fromAngle(angle: Double): SwipeDirection {
            return when (angle) {
                in 0.0..45.0 -> RIGHT
                in 45.0..135.0 -> UP
                in 135.0..225.0 -> LEFT
                in 225.0..315.0 -> DOWN
                in 315.0..360.0 -> RIGHT
                else -> NOT_DETECTED
            }
        }
    }
}