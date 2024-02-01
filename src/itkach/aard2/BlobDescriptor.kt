package itkach.aard2

import android.net.Uri

import java.util.UUID

data class BlobDescriptor(
    @JvmField var slobId: String? = null,
    @JvmField var slobUri: String? = null,
    @JvmField var blobId: String? = null,
    @JvmField var key: String? = null,
    @JvmField var fragment: String? = null
) : BaseDescriptor() {

    companion object {
        @JvmStatic
        fun fromUri(uri: Uri): BlobDescriptor? {
            val bd = BlobDescriptor()
            bd.id = UUID.randomUUID().toString()
            bd.createdAt = System.currentTimeMillis()
            bd.lastAccess = bd.createdAt
            val pathSegments = uri.pathSegments
            val segmentCount = pathSegments.size
            if (segmentCount < 3) {
                return null
            }
            bd.slobId = pathSegments[1]
            val key = StringBuilder()
            for (i in 2 until segmentCount) {
                if (key.isNotEmpty()) {
                    key.append("/")
                }
                key.append(pathSegments[i])
            }
            bd.key = key.toString()
            bd.blobId = uri.getQueryParameter("blob")
            bd.fragment = uri.fragment
            return bd
        }
    }
}
