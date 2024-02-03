package itkach.aard2

import android.database.DataSetObserver
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView

internal class BlobDescriptorListAdapter(val list: BlobDescriptorList) : BaseAdapter() {
    var isSelectionMode = false
        set(selectionMode) {
            field = selectionMode
            notifyDataSetChanged()
        }

    init {
        val observer: DataSetObserver = object : DataSetObserver() {
            override fun onChanged() {
                notifyDataSetChanged()
            }

            override fun onInvalidated() {
                notifyDataSetInvalidated()
            }
        }
        list.registerDataSetObserver(observer)
    }

    override fun getCount(): Int {
        synchronized(list) { return list.size }
    }

    override fun getItem(position: Int): Any {
        synchronized(list) { return list[position] }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        val item = list[position]
        val timestamp = DateUtils.getRelativeTimeSpanString(item.createdAt)
        val view: View = convertView
        val titleView = view.findViewById<TextView>(R.id.blob_descriptor_key)
        titleView.text = item.key
        val sourceView = view.findViewById<TextView>(R.id.blob_descriptor_source)
        val slob = list.resolveOwner(item)
        sourceView.text = if (slob == null) "???" else slob.tags["label"]
        val timestampView = view.findViewById<TextView>(R.id.blob_descriptor_timestamp)
        timestampView.text = timestamp
        val cb = view.findViewById<CheckBox>(R.id.blob_descriptor_checkbox)
        cb.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        return view
    }
}
