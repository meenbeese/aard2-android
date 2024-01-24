package itkach.aard2

import android.net.Uri
import android.util.Log

import java.util.*

object Utility {
    private const val TAG = "Utility"

    fun compare(l1: Long, l2: Long): Int {
        return l1.compareTo(l2)
    }

    fun <T : Comparable<T>> sort(list: MutableList<T>) {
        try {
            list.sort()
        } catch (e: Exception) {
            Log.w(TAG, "Error while sorting:", e)
        }
    }

    fun <T> sort(list: MutableList<T>, comparator: Comparator<in T>) {
        try {
            list.sortWith(comparator)
        } catch (e: Exception) {
            Log.w(TAG, "Error while sorting:", e)
        }
    }

    fun isBlank(value: String?): Boolean {
        return value == null || value.trim() == ""
    }

    fun wikipediaToSlobUri(uri: Uri): String? {
        val host = uri.host
        if (isBlank(host)) {
            return null
        }
        var normalizedHost = host
        val parts = host?.split("\\.".toRegex())?.toTypedArray()
        // If mobile host like en.m.wikipedia.opr get rid of m
        if (parts?.size == 4) {
            normalizedHost = String.format("%s.%s.%s", parts[0], parts[2], parts[3])
        }
        return "http://$normalizedHost"
    }
}
