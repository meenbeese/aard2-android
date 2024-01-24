package itkach.aard2

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

import java.util.LinkedList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import itkach.slob.Slob

class BlobListAdapter @JvmOverloads constructor(
    context: Context,
    private val chunkSize: Int = 20,
    private val loadMoreThreshold: Int = 10
) : BaseAdapter() {
    private var mainHandler: Handler
    var list: MutableList<Slob.Blob>?
    private var iter: Iterator<Slob.Blob>? = null
    private var executor: ExecutorService
    private var maxSize = 10000

    init {
        mainHandler = Handler(context.mainLooper)
        executor = Executors.newSingleThreadExecutor()
        list = ArrayList(chunkSize)
    }

    fun setData(lookupResultsIter: Iterator<Slob.Blob>?) {
        mainHandler.post {
            list!!.clear()
            notifyDataSetChanged()
        }
        iter = lookupResultsIter
        loadChunkSync()
    }

    private fun loadChunkSync() {
        val t0 = System.currentTimeMillis()
        var count = 0
        val chunkList: MutableList<Slob.Blob> = LinkedList()
        while (iter!!.hasNext() && count < chunkSize && list!!.size <= maxSize) {
            count++
            val b = iter!!.next()
            chunkList.add(b)
        }
        mainHandler.post {
            list!!.addAll(chunkList)
            notifyDataSetChanged()
        }
        Log.d(
            TAG, String.format(
                "Loaded chunk of %d (adapter size %d) in %d ms",
                count, list!!.size, System.currentTimeMillis() - t0
            )
        )
    }

    private fun loadChunk() {
        if (!iter!!.hasNext()) {
            return
        }
        executor.execute { loadChunkSync() }
    }

    override fun getCount(): Int {
        return if (list == null) 0 else list!!.size
    }

    override fun getItem(position: Int): Any {
        val result: Any = list!![position]
        maybeLoadMore(position)
        return result
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun maybeLoadMore(position: Int) {
        if (position >= list!!.size - loadMoreThreshold) {
            loadChunk()
        }
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        val item = list!![position]
        val slob = item.owner
        maybeLoadMore(position)
        val view: View = convertView
        val titleView = view.findViewById<View>(R.id.blob_descriptor_key) as TextView
        titleView.text = item.key
        val sourceView = view.findViewById<View>(R.id.blob_descriptor_source) as TextView
        sourceView.text = if (slob == null) "???" else slob.tags["label"]
        val timestampView = view.findViewById<View>(R.id.blob_descriptor_timestamp) as TextView
        timestampView.text = ""
        timestampView.visibility = View.GONE
        return view
    }

    companion object {
        private val TAG = BlobListAdapter::class.java.simpleName
    }
}
