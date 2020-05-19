package life.sabujak.pickle.data.entity

import android.graphics.Bitmap
import android.net.Uri
import life.sabujak.pickle.ui.insta.internal.CropData

data class CropItem(val originalUri: Uri, val resultBitmap: Bitmap, val error: Exception, val cropData: CropData, val sampleSize: Int)