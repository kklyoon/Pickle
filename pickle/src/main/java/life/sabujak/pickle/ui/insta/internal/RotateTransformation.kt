package life.sabujak.pickle.ui.insta.internal

import android.graphics.Bitmap
import android.graphics.Matrix
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.util.Util
import life.sabujak.pickle.util.Logger
import java.nio.charset.Charset
import java.security.MessageDigest

class RotateTransformation(private val cropData: CropData): BitmapTransformation(){
    val logger = Logger.getLogger(this::class.java.simpleName)

    private val ID = this::class.java.simpleName
    private val ID_BYTES = ID.toByteArray(Charset.forName("UTF-8"))

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val matrix = Matrix()
        val orientation = cropData.orientation
        matrix.postRotate(orientation.toFloat())

        return Bitmap.createBitmap(toTransform, 0, 0, toTransform.width, toTransform.height, matrix, true)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    override fun hashCode(): Int {
        return Util.hashCode(ID.hashCode())
    }

    override fun equals(other: Any?): Boolean {
        return other is RotateTransformation && other.cropData == cropData
    }

}