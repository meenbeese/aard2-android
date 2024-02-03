package itkach.aard2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.SearchView
import android.widget.TextView

import itkach.aard2.Clipboard.take

import java.util.Timer
import java.util.TimerTask

class LookupFragment : BaseListFragment(), LookupListener {
    private var timer: Timer? = null
    private var searchView: SearchView? = null
    private var app: Application? = null
    private var queryTextListener: SearchView.OnQueryTextListener? = null
    private var closeListener: SearchView.OnCloseListener? = null
    override val emptyIcon: Char
        get() = IconMaker.IC_SEARCH
    override val emptyText: CharSequence
        get() = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity.application as Application
        app!!.addLookupListener(this)
    }

    override fun supportsSelection(): Boolean {
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBusy(false)
        val listView = listView
        listView.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                Log.i("--", "Item clicked: $position")
                val intent = Intent(activity, ArticleCollectionActivity::class.java)
                intent.putExtra("position", position)
                startActivity(intent)
            }
        val app = activity.application as Application
        getListView().adapter = app.lastResult
        closeListener = SearchView.OnCloseListener { true }
        queryTextListener = object : SearchView.OnQueryTextListener {
            var scheduledLookup: TimerTask? = null
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.d(TAG, "query text submit: $query")
                onQueryTextChange(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.d(TAG, "new query text: $newText")
                val doLookup: TimerTask = object : TimerTask() {
                    override fun run() {
                        val query = searchView!!.query.toString()
                        if (app.lookupQuery == query) {
                            return
                        }
                        activity.runOnUiThread { app.lookup(query) }
                        scheduledLookup = null
                    }
                }
                val query = searchView!!.query.toString()
                if (app.lookupQuery != query) {
                    if (scheduledLookup != null) {
                        scheduledLookup!!.cancel()
                    }
                    scheduledLookup = doLookup
                    timer!!.schedule(doLookup, 600)
                }
                return true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        timer = Timer()
        inflater.inflate(R.menu.lookup, menu)
        val miFilter = menu.findItem(R.id.action_lookup)
        val filterActionView = miFilter.actionView!!
        searchView = filterActionView.findViewById(R.id.fldLookup)
        searchView?.queryHint = miFilter.title
        searchView?.isIconified = false
        searchView?.setOnQueryTextListener(queryTextListener)
        searchView?.setOnCloseListener(closeListener)
        searchView?.isSubmitButtonEnabled = false
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (app!!.autoPaste()) {
            val clipboard = take(this.activity)
            if (clipboard != null) {
                app!!.lookup(clipboard.toString(), false)
            }
        }
        val query: CharSequence = app!!.lookupQuery
        searchView!!.setQuery(query, true)
        if (app!!.lastResult.count > 0) {
            searchView!!.clearFocus()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (searchView != null) {
            val query = searchView!!.query.toString()
            outState.putString("lookupQuery", query)
        }
    }

    private fun setBusy(busy: Boolean) {
        setListShown(!busy)
        if (!busy) {
            val emptyText = emptyView.findViewById<TextView>(R.id.empty_text)
            var msg: String? = ""
            val query = app!!.lookupQuery
            if (query != null && query != "") {
                msg = getString(R.string.lookup_nothing_found)
            }
            emptyText.text = msg
        }
    }

    override fun onDestroy() {
        if (timer != null) {
            timer!!.cancel()
        }
        val app = activity.application as Application
        app.removeLookupListener(this)
        super.onDestroy()
    }

    override fun onLookupStarted(query: String?) {
        setBusy(true)
    }

    override fun onLookupFinished(query: String?) {
        setBusy(false)
    }

    override fun onLookupCanceled(query: String?) {
        setBusy(false)
    }

    companion object {
        private val TAG = LookupFragment::class.java.simpleName
    }
}
