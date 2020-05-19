package life.sabujak.pickle.ui.insta.internal

import android.graphics.Rect

data class CropData(
    val resultX: Int = 0,
    val resultY: Int = 0,
    val resultWidth: Int = 0,
    val resultHeight: Int = 0,
    val sourceWidth: Int = 0,
    val sourceHeight: Int = 0,
    val orientation: Int = 0,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val imageRect: Rect
)