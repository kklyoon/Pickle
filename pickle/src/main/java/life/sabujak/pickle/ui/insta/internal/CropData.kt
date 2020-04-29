package life.sabujak.pickle.ui.insta.internal

data class CropData(
    val resultX: Int,
    val resultY: Int,
    val resultWidth: Int,
    val resultHeight: Int,
    val sourceWidth: Int,
    val sourceHeight: Int,
    val orientation: Int = 0,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f
)