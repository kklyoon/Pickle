package life.sabujak.pickle.ui.insta.internal

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Rect
import android.view.View
import android.view.View.TRANSLATION_X
import androidx.annotation.VisibleForTesting
import androidx.dynamicanimation.animation.*
import androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener
import life.sabujak.pickle.ui.insta.internal.MoveAnimator.Companion.DAMPING_RATIO
import life.sabujak.pickle.ui.insta.internal.MoveAnimator.Companion.STIFFNESS
import life.sabujak.pickle.util.Logger

/**
 * HorizontalAnimatorImpl is responsible for animating [targetView] horizontally.
 */
internal class HorizontalAnimatorImpl @VisibleForTesting constructor(
    private val targetView: View,
    private val leftBound: Float,
    private val rightBound: Float,
    private val maxScale: Float,
    private val spring: SpringAnimation,
    private val animator: ObjectAnimator,
    private val springEndListener: OnAnimationEndListener,
    private val moveEndListener: Animator.AnimatorListener
) : MoveAnimator {

    constructor(
        targetView: View,
        leftBound: Float,
        rightBound: Float,
        maxScale: Float,
        springEndListener: OnAnimationEndListener,
        moveEndListener: Animator.AnimatorListener
    ) : this(
        targetView = targetView,
        leftBound = leftBound,
        rightBound = rightBound,
        maxScale = maxScale,
        spring = SpringAnimation(
            targetView,
            VERTICAL_PROPERTY
        ).setSpring(SPRING_FORCE),
        animator = ANIMATOR,
        springEndListener = springEndListener,
        moveEndListener = moveEndListener
    )

    val logger = Logger.getLogger(this::class.java.simpleName)

    init {
        animator.target = targetView
        animator.addListener(moveEndListener)
        spring.addEndListener(springEndListener)
    }

    override fun move(delta: Float) {
        cancel()
        animator.setFloatValues(targetView.translationX + delta)
        animator.start()
    }

    override fun adjust() {
        val expectedRect = expectRect()
        if (outOfBounds(expectedRect)) {
            adjustToBounds(expectedRect)
        }
    }

    private fun expectRect(): Rect {
        val targetRect = Rect()
        targetView.getHitRect(targetRect)
        return when {
            maxScale < targetView.scaleX -> {
                val heightDiff =
                    ((targetRect.height() - targetRect.height() * (maxScale / targetView.scaleY)) / 2).toInt()
                val widthDiff =
                    ((targetRect.width() - targetRect.width() * (maxScale / targetView.scaleY)) / 2).toInt()
                Rect(
                    targetRect.left + widthDiff,
                    targetRect.top + heightDiff,
                    targetRect.right - widthDiff,
                    targetRect.bottom - heightDiff
                )
            }
            targetView.scaleX < 1f -> {
                val heightDiff = (targetView.height - targetRect.height()) / 2
                val widthDiff = (targetView.width - targetRect.width()) / 2
                Rect(
                    targetRect.left + widthDiff,
                    targetRect.top + heightDiff,
                    targetRect.right - widthDiff,
                    targetRect.bottom - heightDiff
                )
            }
            else -> targetRect
        }
    }

    private fun outOfBounds(rect: Rect): Boolean {
        return leftBound < rect.left || rect.right < rightBound
    }

    private fun adjustToBounds(rect: Rect, velocity: Float = 0f) {
        val scale = when {
            maxScale < targetView.scaleX -> maxScale
            targetView.scaleX < 1f -> 1f
            else -> targetView.scaleX
        }
        val diff = (targetView.width * scale - targetView.width) / 2

        if (leftBound < rect.left) {
            cancel()
            val finalPosition = leftBound + diff
            spring.setStartVelocity(velocity).animateToFinalPosition(finalPosition)
        } else if (rect.right < rightBound) {
            cancel()
            val finalPosition = rightBound - targetView.width.toFloat() - diff
            spring.setStartVelocity(velocity).animateToFinalPosition(finalPosition)
        }
    }

    override fun cancel() {
        animator.cancel()
        spring.cancel()
    }

    companion object {

        private val ANIMATOR = ObjectAnimator().apply {
            setProperty(TRANSLATION_X)
            interpolator = null
            duration = 0
        }

        private val VERTICAL_PROPERTY = object : FloatPropertyCompat<View>("X") {
            override fun getValue(view: View): Float {
                return view.x
            }

            override fun setValue(view: View, value: Float) {
                view.x = value
            }
        }

        private val SPRING_FORCE =
            SpringForce().setStiffness(STIFFNESS).setDampingRatio(DAMPING_RATIO)
    }
}
