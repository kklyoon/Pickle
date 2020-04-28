package life.sabujak.pickle.data.entity

import android.net.Uri

abstract class Media(
    val id: Long,
    val uri: Uri,
    val bucketId: String,
    val dateAdded: Long,
    val fileSize: Long,
    val mediaType: Int,
    val mimeType: String,
    val orientation: Int = 0
) {
    override fun toString(): String {
        return "id = $id, bucketId = $bucketId, uri = $uri dateAdded = $dateAdded, fileSize = $fileSize, mediaType = $mediaType, mimeType = $mimeType, orientation = $orientation"
    }
}
