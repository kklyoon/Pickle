package life.sabujak.pickle.ui.insta.internal

import android.graphics.RectF
import android.view.View

/**
 * Animator to move a view horizontally and vertically, and scale a view.
 */
internal class GestureAnimator(
    private val horizontalAnimator: MoveAnimator,
    private val verticalAnimator: MoveAnimator,
    private val scaleAnimator: ScaleAnimator
) : ActionListener {

    override fun onScaled(scale: Float) {
        scaleAnimator.scale(scale)
    }

    override fun onScaleEnded() {
        scaleAnimator.adjust()
    }

    override fun onMoved(dx: Float, dy: Float) {
        horizontalAnimator.move(dx)
        verticalAnimator.move(dy)
    }

    override fun onFlinged(velocityX: Float, velocityY: Float) {
        horizontalAnimator.fling(velocityX)
        verticalAnimator.fling(velocityY)
    }

    override fun onMoveEnded() {
        horizontalAnimator.adjust()
        verticalAnimator.adjust()
    }

    override fun cancel() {
        horizontalAnimator.cancel()
        verticalAnimator.cancel()
    }

    companion object {

        fun of(target: View, frame: RectF, scale: Float, endListener: CropDataListener): GestureAnimator {
            val horizontalAnimator = HorizontalAnimatorImpl(
                targetView = target,
                leftBound = frame.left,
                rightBound = frame.right,
                maxScale = scale,
                aniEndListener = endListener
            )
            val verticalAnimator = VerticalAnimatorImpl(
                targetView = target,
                topBound = frame.top,
                bottomBound = frame.bottom,
                maxScale = scale,
                aniEndListener = endListener
            )
            val scaleAnimator = ScaleAnimatorImpl(
                targetView = target,
                maxScale = scale,
                aniEndListener = endListener
            )
            return GestureAnimator(
                horizontalAnimator,
                verticalAnimator,
                scaleAnimator
            )
        }
    }
}