package life.sabujak.pickle.ui.insta.internal

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Rect
import android.view.View
import android.view.View.TRANSLATION_Y
import androidx.annotation.VisibleForTesting
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import life.sabujak.pickle.ui.insta.internal.MoveAnimator.Companion.DAMPING_RATIO
import life.sabujak.pickle.ui.insta.internal.MoveAnimator.Companion.STIFFNESS

/**
 * VerticalAnimatorImpl is responsible for animating [targetView] vertically.
 */
internal class VerticalAnimatorImpl @VisibleForTesting constructor(
    private val targetView: View,
    private val topBound: Float,
    private val bottomBound: Float,
    private val maxScale: Float,
    private val spring: SpringAnimation,
    private val animator: ObjectAnimator,
    private val springEndListener: DynamicAnimation.OnAnimationEndListener,
    private val moveEndListener: Animator.AnimatorListener
) : MoveAnimator {

    constructor(
        targetView: View,
        topBound: Float,
        bottomBound: Float,
        maxScale: Float,
        springEndListener: DynamicAnimation.OnAnimationEndListener,
        moveEndListener: Animator.AnimatorListener
    ) : this(
        targetView = targetView,
        topBound = topBound,
        bottomBound = bottomBound,
        maxScale = maxScale,
        spring = SpringAnimation(
            targetView,
            HORIZONTAL_PROPERTY
        ).setSpring(SPRING_FORCE),
        animator = ANIMATOR,
        springEndListener = springEndListener,
        moveEndListener = moveEndListener
    )

    init {
        animator.target = targetView
        animator.addListener(moveEndListener)
        spring.addEndListener(springEndListener)
    }

    override fun move(delta: Float) {
        cancel()
        animator.setFloatValues(targetView.translationY + delta)
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
            maxScale < targetView.scaleY -> {
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
            targetView.scaleY < 1f -> {
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
        return topBound < rect.top || rect.bottom < bottomBound
    }

    private fun adjustToBounds(rect: Rect, velocity: Float = 0f) {
        val scale = when {
            maxScale < targetView.scaleX -> maxScale
            targetView.scaleX < 1f -> 1f
            else -> targetView.scaleX
        }
        val diff = (targetView.height * scale - targetView.height) / 2
        if (topBound < rect.top) {
            cancel()
            val finalPosition = topBound + diff
            spring.setStartVelocity(velocity).animateToFinalPosition(finalPosition)
        } else if (rect.bottom < bottomBound) {
            cancel()
            val finalPosition = bottomBound - targetView.height.toFloat() - diff
            spring.setStartVelocity(velocity).animateToFinalPosition(finalPosition)
        }
    }

    override fun cancel() {
        animator.cancel()
        spring.cancel()
    }

    companion object {

        private val ANIMATOR = ObjectAnimator().apply {
            setProperty(TRANSLATION_Y)
            interpolator = null
            duration = 0
        }

        private val HORIZONTAL_PROPERTY = object : FloatPropertyCompat<View>("Y") {
            override fun getValue(view: View): Float {
                return view.y
            }

            override fun setValue(view: View, value: Float) {
                view.y = value
            }
        }

        private val SPRING_FORCE =
            SpringForce().setStiffness(STIFFNESS).setDampingRatio(DAMPING_RATIO)
    }
}
