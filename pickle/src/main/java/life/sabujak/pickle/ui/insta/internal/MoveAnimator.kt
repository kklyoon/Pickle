package life.sabujak.pickle.ui.insta.internal

import androidx.dynamicanimation.animation.SpringForce

/**
 * Interface to move Image.
 */
internal interface MoveAnimator {

  /**
   * Move image
   *
   * @param delta distance of how much image moves
   */
  fun move(delta: Float)

  /**
   * adjust image when image is off of the frame
   */
  fun adjust()

  fun cancel()

  companion object {

    /**
     * stiffness when flinging or bouncing
     */
    const val STIFFNESS = SpringForce.STIFFNESS_MEDIUM

    /**
     * dumping ratio when flinging or bouncing
     */
    const val DAMPING_RATIO = SpringForce.DAMPING_RATIO_NO_BOUNCY

  }
}
