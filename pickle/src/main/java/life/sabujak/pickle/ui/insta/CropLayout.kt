package life.sabujak.pickle.ui.insta

import android.animation.Animator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.dynamicanimation.animation.DynamicAnimation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.*
import life.sabujak.pickle.R
import life.sabujak.pickle.data.entity.PickleItem
import life.sabujak.pickle.ui.insta.internal.*
import life.sabujak.pickle.ui.insta.internal.GestureAnimation
import life.sabujak.pickle.ui.insta.internal.GestureAnimator
import life.sabujak.pickle.util.Logger
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Layout to show Image and Frame.
 *
 * This will be the parent view that holds [CropImageView]
 * This is based on https://github.com/TakuSemba/CropMe
 */
class CropLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), DynamicAnimation.OnAnimationEndListener, Animator.AnimatorListener {
    val logger = Logger.getLogger(this::class.java.simpleName)
    private lateinit var frame: RectF
    private var scale = DEFAULT_MAX_SCALE
    private var percentWidth = DEFAULT_PERCENT_WIDTH
    private var percentHeight = DEFAULT_PERCENT_HEIGHT

    private lateinit var animator: GestureAnimator
    private lateinit var animation: GestureAnimation

    private var cropImageView: CropImageView
    private var selectItem: PickleItem? = null

    private val cropOverlay: RectangleCropOverlay by lazy {
        RectangleCropOverlay(context, null, 0, attrs)
    }
    private lateinit var frameCache: RectF
    private val listeners = CopyOnWriteArrayList<OnCropListener>()

    private lateinit var selectionManager: InstaSelectionManager

    init {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.CropLayout, 0, 0)
        cropImageView = CropImageView(context, null, 0)
        cropOverlay.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT, Gravity.CENTER)
        cropOverlay.visibility = View.GONE
        addView(cropImageView, 0)
        addView(cropOverlay, 1)

        try {
            scale = attr.getFloat(R.styleable.CropLayout_max_scale, DEFAULT_MAX_SCALE)

            percentWidth = attr.getFraction(
                R.styleable.CropLayout_frame_width_percent,
                DEFAULT_BASE,
                DEFAULT_PBASE,
                DEFAULT_PERCENT_WIDTH
            )
            percentHeight = attr.getFraction(
                R.styleable.CropLayout_frame_height_percent,
                DEFAULT_BASE,
                DEFAULT_PBASE,
                DEFAULT_PERCENT_HEIGHT
            )
        } finally {
            attr.recycle()
        }

        val vto = viewTreeObserver
        vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                logger.d("addOnPreDrawListener")
                val totalWidth = measuredWidth.toFloat()
                val totalHeight = measuredHeight.toFloat()
                val frameWidth = measuredWidth * percentWidth
                val frameHeight = measuredHeight * percentHeight
                frame = RectF(
                    (totalWidth - frameWidth) / 2f,
                    (totalHeight - frameHeight) / 2f,
                    (totalWidth + frameWidth) / 2f,
                    (totalHeight + frameHeight) / 2f
                )
                cropImageView.setFrame(frame)
                cropOverlay.setFrame(frame)
                cropOverlay.requestLayout()
                frameCache = frame

                when {
                    vto.isAlive -> vto.removeOnPreDrawListener(this)
                    else -> cropOverlay.viewTreeObserver.removeOnPreDrawListener(this)
                }
                return true
            }
        })
    }

    fun addOnCropListener(listener: OnCropListener) {
        listeners.addIfAbsent(listener)
    }

    private fun removeCropListener() {
        listeners.clear()
    }

    suspend fun crop() {
        val cropData = getCropData()
        if (selectionManager.isMultiSelect.value == true)
            getBitmapFromUri(selectItem!!.mediaUri)?.let { bitmap ->
                multiTransForm(bitmap, cropData)
            } else {
            val source = cropImageView.drawable.toBitmap()
            try {
                Glide.with(context).asBitmap().load(source)
                    .transform(CropTransformation(cropData))
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            result: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            for (listener in listeners) {
                                listener.onSuccess(result)
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
            } catch (e: Exception) {
                for (listener in listeners) {
                    listener.onFailure(e)
                }
            }
        }
    }

    fun getCropData(): CropData {
        val targetRect = Rect().apply { cropImageView.getHitRect(this) }
        val imageViewRect = Rect().apply {
            this.left = cropImageView.left
            this.top = cropImageView.top
            this.right = cropImageView.right
            this.bottom = cropImageView.bottom
        }
        val leftOffset = (frameCache.left - targetRect.left).toInt()
        val topOffset = (frameCache.top - targetRect.top).toInt()
        val width = frameCache.width().toInt()
        val height = frameCache.height().toInt()
        val orientation = selectItem?.media?.orientation
        val scaleX = cropImageView.scaleX
        val scaleY = cropImageView.scaleY
        val translationX = cropImageView.x - cropImageView.left
        val translationY = cropImageView.y - cropImageView.top
//        imageViewLog("getCropData")
//        targetViewLog("getCropData")
        orientation?.let {
            return CropData(
                leftOffset,
                topOffset,
                width,
                height,
                targetRect.width(),
                targetRect.height(),
                it,
                scaleX,
                scaleY,
                translationX,
                translationY,
                imageViewRect
            )
        }
        return CropData(
            leftOffset,
            topOffset,
            width,
            height,
            targetRect.width(),
            targetRect.height(),
            scaleX = scaleX,
            scaleY = scaleY,
            translationX = translationX,
            translationY = translationY,
            imageRect = imageViewRect
        )
    }

    fun setCropScale() {
//        val cropInfoListener = CropImageLayoutChangeListener(selectItem)
        cropOverlay.visibility = View.VISIBLE
        cropImageView.layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER)
        cropImageView.scaleType = ImageView.ScaleType.CENTER_CROP
        cropImageView.adjustViewBounds = true

        selectItem?.let { it ->
            val cropData = selectionManager.getCropData(it)
            cropData?.let { data ->
                cropImageView.setCropData(data)
            }?: run {
                val rect = Rect(left, top, right, bottom)
                val data = CropData(imageRect = rect)
                cropImageView.setCropData(data)
            }
        }
        cropImageView.requestLayout()

        animator = GestureAnimator.of(cropImageView, frame, scale, this, this)
        animation = GestureAnimation(cropOverlay, animator)
        animation.start()
//        imageViewLog("setCropScale")
//        targetViewLog("setCropScale")
//        val position = IntArray(2).apply { cropImageView.getLocationOnScreen(this) }
//        logger.d(
//            "Item : ${selectItem?.getId()} setCropScale() : cropImageView ${cropImageView.left}, ${cropImageView.top}, ${cropImageView.right}, ${cropImageView.bottom}, ${cropImageView.x}, ${cropImageView.y}" +
//                    " scaleX : ${cropImageView.scaleX} "
//        )
//        logger.d("setCropScale() : cropImageView realposition ${position[0]}, ${position[1]}")
    }

    fun setAspectRatio() {
        if (::animation.isInitialized) animation.stop()
        removeCropListener()
        cropOverlay.visibility = View.GONE
        cropImageView.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT, Gravity.CENTER)
        cropImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        cropImageView.top = top
        cropImageView.left = left
        cropImageView.right = right
        cropImageView.bottom = bottom
        cropImageView.x = left.toFloat()
        cropImageView.y = top.toFloat()
        cropImageView.maxWidth = width
        cropImageView.maxHeight = height
        cropImageView.scaleX = 1f
        cropImageView.scaleY = 1f
        cropImageView.requestLayout()
//        val position = IntArray(2).apply { cropImageView.getLocationOnScreen(this) }
//        logger.d("setAspectRatio() : cropImageView ${cropImageView.left}, ${cropImageView.top}, ${cropImageView.right}, ${cropImageView.bottom}, ${cropImageView.x}, ${cropImageView.y}" +
//                " scaleX : ${cropImageView.scaleX} ")
//        logger.d("setAspectRatio() : cropImageView realposition ${position[0]}, ${position[1]}")
    }

    fun isEmpty(): Boolean {
        this.selectItem?.let { return false }
        return true
    }

    fun setPickleMedia(item: PickleItem, isAspectRatio: Boolean?) {
        this.selectItem = item
        Glide.with(this.context).load(this.selectItem?.mediaUri)
            .transition(DrawableTransitionOptions.withCrossFade())
            .listener(CropLayoutRequestListener(isAspectRatio)).into(cropImageView)
    }

    fun setSelectionManager(selectionManager: InstaSelectionManager) {
        this.selectionManager = selectionManager
    }

    private suspend fun getBitmapFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < 29) {
            return@withContext MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                selectItem?.mediaUri
            )
        } else {
            val source =
                ImageDecoder.createSource(context.contentResolver, selectItem?.mediaUri!!)
            return@withContext ImageDecoder.decodeBitmap(source)
        }
    }

    private suspend fun multiTransForm(bitmap: Bitmap, cropData: CropData) =
        withContext(Dispatchers.Default) {
            logger.d("glideTransFrom $cropData")
            try {
                val multiTransformation = MultiTransformation(
                    RotateTransformation(cropData),
                    CropTransformation(cropData)
                )
                Glide.with(context).asBitmap().load(bitmap)
                    .transform(multiTransformation).into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            result: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            for (listener in listeners) {
                                listener.onSuccess(result)
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
            } catch (e: Exception) {
                for (listener in listeners) {
                    listener.onFailure(e)
                }
            }
        }

    fun clear() {
        cropImageView.setImageResource(android.R.color.transparent)
    }

    private fun saveCropData(){
        val cropData = getCropData()
        val selectedMedia = selectItem
//        logger.d("saveCropData() ${cropData}")
        selectedMedia?.let {
            selectionManager.updateCropData(it, cropData)
        }
    }

    override fun onAnimationEnd(animation: Animator?) {
        saveCropData()
    }

    override fun onAnimationEnd(
        animation: DynamicAnimation<out DynamicAnimation<*>>?,
        canceled: Boolean,
        value: Float,
        velocity: Float
    ) {
        saveCropData()
    }

    override fun onAnimationCancel(animation: Animator?) {}
    override fun onAnimationRepeat(animation: Animator?) {}
    override fun onAnimationStart(animation: Animator?) {}

    companion object {
        private const val DEFAULT_MAX_SCALE = 2f

        private const val DEFAULT_BASE = 1
        private const val DEFAULT_PBASE = 1

        private const val DEFAULT_PERCENT_WIDTH = 0.8f
        private const val DEFAULT_PERCENT_HEIGHT = 0.8f
    }

    inner class CropLayoutRequestListener(val isAspect: Boolean?) : RequestListener<Drawable> {
        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            isAspect?.let {
                if (it) setAspectRatio()
                else setCropScale()
            }
            return false
        }

        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            logger.e("Image load Failed ${e.toString()}")
            return false
        }
    }

    // belows for debug
    private fun imageViewLog(funName: String) {
        logger.d(
            "$funName : ID: ${selectItem?.getId()}, cropImageView ${cropImageView.left}, ${cropImageView.top}, ${cropImageView.right}, ${cropImageView.bottom}, (${cropImageView.x}, ${cropImageView.y})" +
                    " ${cropImageView.width}, ${cropImageView.height} scaleX : ${cropImageView.scaleX} "
        )
    }

    private fun targetViewLog(funName: String) {
        val targetRect = Rect().apply { cropImageView.getHitRect(this) }        // move
        logger.d("$funName : targetRect ${targetRect} ")
    }

    private fun drawableLog(dr: Drawable?) {
        logger.d("Drawble width ${dr?.intrinsicWidth}, height ${dr?.intrinsicHeight}")
    }

    private fun cropdataLog(funName: String, cropData: CropData) {
        logger.d("$funName : ${selectItem?.getId()} $cropData")
    }

    private fun getTranslation(funName: String) {
        logger.d("$funName : ${cropImageView.translationX}, ${cropImageView.translationY}")
    }

    private fun realpositionLog(funName: String) {
        val position = IntArray(2).apply { cropImageView.getLocationOnScreen(this) }
        logger.d("$funName : cropImageView realposition ${position[0]}, ${position[1]}")
    }
}
