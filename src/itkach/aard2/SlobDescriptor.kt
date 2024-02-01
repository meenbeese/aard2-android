package itkach.aard2

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log

import itkach.slob.Slob

import java.io.FileInputStream

class SlobDescriptor : BaseDescriptor() {
    @JvmField
    var path: String? = null
    @JvmField
    var tags: Map<String, String> = HashMap()
    @JvmField
    var active = true
    @JvmField
    var priority: Long = 0
    @JvmField
    var blobCount: Long = 0
    @JvmField
    var error: String? = null
    @JvmField
    var expandDetail = false

    @Transient
    private var fileDescriptor: ParcelFileDescriptor? = null
    private fun update(s: Slob) {
        id = s.id.toString()
        path = s.fileURI
        tags = s.tags
        blobCount = s.blobCount
        error = null
    }

    fun load(context: Context): Slob? {
        var slob: Slob? = null
        try {
            val uri = Uri.parse(path)
            // Must hold on to ParcelFileDescriptor,
            // Otherwise it gets garbage collected and trashes underlying file descriptor
            fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            val fileInputStream = FileInputStream(fileDescriptor!!.fileDescriptor)
            slob = Slob(fileInputStream.channel, path)
            update(slob)
        } catch (e: Exception) {
            Log.e(TAG, "Error while opening $path", e)
            error = e.message
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Error while opening $path", e)
            }
            expandDetail = true
            active = false
        }
        return slob
    }

    val label: String
        get() {
            var label = tags["label"]
            if (label == null || label.trim { it <= ' ' }.isEmpty()) {
                label = "???"
            }
            return label
        }

    companion object {
        private val TAG = SlobDescriptor::class.java.simpleName
        @JvmStatic
        fun fromUri(context: Context, uri: String?): SlobDescriptor {
            val s = SlobDescriptor()
            s.path = uri
            s.load(context)
            return s
        }
    }
}
