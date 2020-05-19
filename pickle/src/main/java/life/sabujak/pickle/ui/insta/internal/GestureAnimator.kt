package life.sabujak.pickle.ui.insta.internal

import android.animation.Animator
import android.graphics.RectF
import android.view.View
import androidx.dynamicanimation.animation.DynamicAnimation

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

    override fun onMoveEnded() {
        horizontalAnimator.adjust()
        verticalAnimator.adjust()
    }

    override fun cancel() {
        horizontalAnimator.cancel()
        verticalAnimator.cancel()
    }

    companion object {

        fun of(target: View, frame: RectF, scale: Float, springEndListener: DynamicAnimation.OnAnimationEndListener, moveEndListener: Animator.AnimatorListener): GestureAnimator {
            val horizontalAnimator = HorizontalAnimatorImpl(
                targetView = target,
                leftBound = frame.left,
                rightBound = frame.right,
                maxScale = scale,
                springEndListener = springEndListener,
                moveEndListener = moveEndListener
            )
            val verticalAnimator = VerticalAnimatorImpl(
                targetView = target,
                topBound = frame.top,
                bottomBound = frame.bottom,
                maxScale = scale,
                springEndListener = springEndListener,
                moveEndListener = moveEndListener
            )
            val scaleAnimator = ScaleAnimatorImpl(
                targetView = target,
                maxScale = scale,
                scaleEndListener = moveEndListener
            )
            return GestureAnimator(
                horizontalAnimator,
                verticalAnimator,
                scaleAnimator
            )
        }
    }
}