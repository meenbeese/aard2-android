package itkach.aard2

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

open class SearchableWebView : WebView {
    private var mLastFind: String? = null

    fun setLastFind(find: String?) {
        mLastFind = find
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    /**
     * Start an ActionMode for finding text in this WebView.  Only works if this
     * WebView is attached to the view system.
     *
     * @param text    If non-null, will be the initial text to search for.
     *                Otherwise, the last String searched for in this WebView will
     *                be used to start.
     * @param showIme If true, show the IME, assuming the user will begin typing.
     *                If false and text is non-null, perform a find all.
     * @return boolean True if the find dialog is shown, false otherwise.
     */
    @Deprecated("Deprecated")
    override fun showFindDialog(text: String?, showIme: Boolean): Boolean {
        val callback = FindActionModeCallback(context, this)
        if (parent == null || startActionMode(callback) == null) {
            // Could not start the action mode, so end Find on page
            return false
        }

        if (showIme) {
            callback.showSoftInput()
        } else if (text != null) {
            callback.setText(text)
            callback.findAll()
            return true
        }
        var text = text
        if (text == null) {
            text = mLastFind
        }
        if (text != null) {
            callback.setText(text)
            callback.findAll()
        }
        return true
    }
}
