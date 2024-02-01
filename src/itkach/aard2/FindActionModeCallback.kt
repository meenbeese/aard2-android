package itkach.aard2

import android.content.Context
import android.text.Editable
import android.text.Selection
import android.text.Spannable
import android.text.TextWatcher
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnLongClickListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

internal class FindActionModeCallback(context: Context, private val webview: SearchableWebView) :
    ActionMode.Callback, TextWatcher, OnLongClickListener, View.OnClickListener {
    private val searchView: View
    private val editText: EditText
    private val imManager: InputMethodManager

    init {
        searchView = LayoutInflater.from(context).inflate(R.layout.webview_find, null)
        editText = searchView.findViewById(R.id.edit)
        editText.setOnLongClickListener(this)
        editText.setOnClickListener(this)
        editText.addTextChangedListener(this)
        imManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    /* Place text in the text field so it can be searched for. */
    fun setText(text: String?) {
        editText.setText(text)
        val span: Spannable = editText.text
        val length = span.length
        // Ideally, we would like to set the selection to the whole field,
        // but this brings up the Text selection CAB, which dismisses this one.
        Selection.setSelection(span, length, length)
        // Necessary each time we set the text, so that this will watch changes to it.
        span.setSpan(this, 0, length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }

    /**
     * Move the highlight to the next match.
     * @param next If true, find the next match further down in the document.
     * If false, find the previous match, up in the document.
     */
    private fun findNext(next: Boolean) {
        webview.findNext(next)
    }

    /*
     * Highlight all the instances of the string from editText in webview.
     */
    fun findAll() {
        val find = editText.text.toString()
        webview.findAllAsync(find)
    }

    fun showSoftInput() {
        // imManager.showSoftInputMethod does not work
        imManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    override fun onLongClick(v: View): Boolean {
        // Override long click so that select ActionMode is not opened, which
        // would exit find ActionMode.
        return true
    }

    override fun onClick(v: View) {
        findNext(true)
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.customView = searchView
        mode.menuInflater.inflate(R.menu.webview_find, menu)
        val edit = editText.text
        Selection.setSelection(edit, edit.length)
        editText.requestFocus()
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        webview.clearMatches()
        imManager.hideSoftInputFromWindow(webview.windowToken, 0)
        webview.setLastFind(editText.text.toString())
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        imManager.hideSoftInputFromWindow(webview.windowToken, 0)
        when (item.itemId) {
            R.id.find_prev -> findNext(false)
            R.id.find_next -> findNext(true)
            else -> {
                return false
            }
        }
        return true
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { findAll() }

    override fun afterTextChanged(s: Editable) {}
}