package itkach.aard2

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import android.widget.RelativeLayout

class CheckableRelativeLayout : RelativeLayout, Checkable {
    private var isChecked = false
    private val checkableViews = ArrayList<Checkable>()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun setChecked(checked: Boolean) {
        isChecked = checked
        for (c in checkableViews) {
            // Pass the information to all the child Checkable widgets
            c.isChecked = isChecked
        }
    }

    override fun isChecked(): Boolean {
        return isChecked
    }

    override fun toggle() {
        isChecked = !isChecked
        for (c in checkableViews) {
            // Pass the information to all the child Checkable widgets
            c.toggle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val childCount = childCount
        for (i in 0 until childCount) {
            findCheckableChildren(getChildAt(i))
        }
    }

    /*
     * Add to our checkable list all the children of the view that implement the
     * interface Checkable
     */
    private fun findCheckableChildren(v: View) {
        if (v is Checkable) {
            checkableViews.add(v)
        }
        if (v is ViewGroup) {
            val childCount = v.childCount
            for (i in 0 until childCount) {
                findCheckableChildren(v.getChildAt(i))
            }
        }
    }
}
