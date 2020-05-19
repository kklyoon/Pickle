package life.sabujak.pickle.ui.insta

import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import life.sabujak.pickle.R
import life.sabujak.pickle.data.cursor.CursorType
import life.sabujak.pickle.ui.insta.internal.OnInstaResultListener

class InstaConfig private constructor(
    val onResultListener: OnInstaResultListener?,
    val cursorType: CursorType,
    @ColorRes val themeColorRes: Int,
    val title: CharSequence

) {
    // MaxCount
    // CropRatio
    //

    class Builder (private var onResultListener: OnInstaResultListener? = null) {
        private var cursorType: CursorType = CursorType.IMAGE
        @ColorRes
        private var themeColorRes: Int = R.color.GR500
        private var title: CharSequence = "Select images"

        fun setCursorType(cursorType: CursorType) = apply { this.cursorType = cursorType }
        fun setThemeColorRes(@ColorRes themeColorRes: Int) =
            apply { this.themeColorRes = themeColorRes }

        fun setTitle(title: CharSequence) = apply { this.title = title }

        fun build() = InstaConfig(
            onResultListener,
            cursorType,
            themeColorRes,
            title
        )
    }


}