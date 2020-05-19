package life.sabujak.pickle.ui.insta

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import life.sabujak.pickle.ui.insta.internal.CropData
import life.sabujak.pickle.util.Logger

/**
 * ImageView to show a image for cropping.
 */
class CropImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    val logger = Logger.getLogger(this::class.java.simpleName)
    private var frame: RectF? = null
    private var cropData: CropData? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = measuredHeight.toFloat()
        val width = measuredWidth.toFloat()
        val frameRect = this.frame
        val widthScale = if (frameRect != null) frameRect.width() / width else 1f
        val heightScale = if (frameRect != null) frameRect.height() / height else 1f
        val scale = maxOf(widthScale, heightScale)
        setMeasuredDimension((width * scale).toInt(), (height * scale).toInt())
    }

    fun setFrame(frame: RectF) {
        this.frame = frame
    }

    fun getFrame() = this.frame

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cropData?.let {
            if (w == it.imageRect.width() &&
                h == it.imageRect.height()
            ) {
                logger.d("onSizeChanged : $cropData")
                scaleX = it.scaleX
                scaleY = it.scaleY
                translationX = it.translationX
                translationY = it.translationY
                requestLayout()

            }

        }

    }

    fun setCropData(cropData: CropData) {
        this.cropData = cropData
        left = cropData.imageRect.left
        top = cropData.imageRect.top
        right = cropData.imageRect.right
        bottom = cropData.imageRect.bottom
    }
}
