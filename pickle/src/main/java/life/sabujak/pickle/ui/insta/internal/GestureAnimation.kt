package life.sabujak.pickle.ui.insta.internal

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.view.GestureDetectorCompat
import life.sabujak.pickle.util.Logger

/**
 * According to the gesture detected on [trackPad], this will notify [actionListener].
 */
internal class GestureAnimation(
    private val trackPad: View,
    private val actionListener: ActionListener
) {
    val logger = Logger.getLogger(this::class.java.simpleName)

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onShowPress(e: MotionEvent) = Unit

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return true
        }

        override fun onScroll(
            initialEvent: MotionEvent, currentEvent: MotionEvent, dx: Float, dy: Float
        ): Boolean {
            actionListener.onMoved(-dx, -dy)
            return true
        }

        override fun onLongPress(e: MotionEvent) = Unit

    }

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return super.onScaleBegin(detector)
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)
            actionListener.onScaleEnded()
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            actionListener.onScaled(detector.scaleFactor)
            return true
        }
    }

    private val gestureDetector = GestureDetectorCompat(trackPad.context, gestureListener)
    private val scaleDetector = ScaleGestureDetector(trackPad.context, scaleListener)

    fun start() {
        trackPad.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            scaleDetector.onTouchEvent(event)
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    actionListener.onMoveEnded()
                }
            }
            true
        }
    }

    fun stop() {
        trackPad.setOnTouchListener(null)
        actionListener.cancel()
    }
}