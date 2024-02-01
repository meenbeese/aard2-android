package itkach.aard2

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

import itkach.aard2.IconMaker.actionBar
import itkach.aard2.IconMaker.emptyView

class ArticleFragment : Fragment() {
    var webView: ArticleWebView? = null
        private set
    private var miBookmark: MenuItem? = null
    private var miFullscreen: MenuItem? = null
    private var icBookmark: Drawable? = null
    private var icBookmarkO: Drawable? = null
    private var icFullscreen: Drawable? = null
    private var url: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity: Activity = activity
        val context = activity.actionBar!!.themedContext
        icBookmark = actionBar(context, IconMaker.IC_BOOKMARK)
        icBookmarkO = actionBar(context, IconMaker.IC_BOOKMARK_O)
        icFullscreen = actionBar(context, IconMaker.IC_FULLSCREEN)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Looks like this may be called multiple times with the same menu
        // when activity is restored, so need to clear to avoid duplicates
        menu.clear()
        inflater.inflate(R.menu.article, menu)
        miBookmark = menu.findItem(R.id.action_bookmark_article)
        miFullscreen = menu.findItem(R.id.action_fullscreen)
    }

    private fun displayBookmarked(value: Boolean) {
        miBookmark?.apply {
            isChecked = value
            icon = if (value) icBookmark else icBookmarkO
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val app = activity.application as Application
        return when (item.itemId) {
            R.id.action_find_in_page -> {
                webView?.showFindDialog(null, true)
                true
            }
            R.id.action_bookmark_article -> {
                url?.let {
                    if (item.isChecked) {
                        app.removeBookmark(it)
                        displayBookmarked(false)
                    } else {
                        app.addBookmark(it)
                        displayBookmarked(true)
                    }
                }
                true
            }
            R.id.action_fullscreen -> {
                (activity as ArticleCollectionActivity).toggleFullScreen()
                true
            }
            R.id.action_zoom_in -> {
                webView?.textZoomIn()
                true
            }
            R.id.action_zoom_out -> {
                webView?.textZoomOut()
                true
            }
            R.id.action_zoom_reset -> {
                webView?.resetTextZoom()
                true
            }
            R.id.action_load_remote_content -> {
                webView?.apply {
                    forceLoadRemoteContent = true
                    reload()
                }
                true
            }
            R.id.action_select_style -> {
                val builder = AlertDialog.Builder(activity)
                val styleTitles = webView?.availableStyles
                builder.setTitle(R.string.select_style)
                    .setItems(styleTitles) { _: DialogInterface?, which: Int ->
                        val title = styleTitles?.get(which)
                        webView?.apply {
                            saveStylePref(title)
                            applyStylePref()
                        }
                    }
                val dialog = builder.create()
                dialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args = arguments
        url = args?.getString(ARG_URL)
        if (url == null) {
            val layout = inflater.inflate(R.layout.empty_view, container, false)
            val textView = layout.findViewById<TextView>(R.id.empty_text)
            textView.text = ""
            val icon = layout.findViewById<ImageView>(R.id.empty_icon)
            icon.setImageDrawable(
                emptyView(
                    activity,
                    IconMaker.IC_BAN
                )
            )
            setHasOptionsMenu(false)
            return layout
        }
        val layout = inflater.inflate(R.layout.article_view, container, false)
        val progressBar = layout.findViewById<ProgressBar>(R.id.webViewProgress)
        webView = layout.findViewById(R.id.webView)
        webView?.restoreState(savedInstanceState!!)
        webView?.loadUrl(url!!)
        webView?.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                val activity: Activity? = activity
                activity?.runOnUiThread {
                    progressBar.progress = newProgress
                    if (newProgress >= progressBar.max) {
                        progressBar.visibility = ViewGroup.GONE
                    }
                }
            }
        }
        return layout
    }

    override fun onResume() {
        super.onResume()
        applyTextZoomPref()
        applyStylePref()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (url == null) {
            miBookmark!!.setVisible(false)
        } else {
            val app = activity.application as Application
            try {
                val bookmarked = app.isBookmarked(url)
                displayBookmarked(bookmarked)
            } catch (ex: Exception) {
                miBookmark!!.setVisible(false)
            }
        }
        applyTextZoomPref()
        applyStylePref()
        miFullscreen!!.setIcon(icFullscreen)
    }

    fun applyTextZoomPref() {
        webView?.let { applyTextZoomPref() }
    }

    private fun applyStylePref() {
        webView?.let { applyStylePref() }
    }

    override fun onDestroy() {
        webView?.let {
            it.destroy()
            webView = null
        }
        miFullscreen = null
        miBookmark = null
        super.onDestroy()
    }

    companion object {
        const val ARG_URL = "articleUrl"
    }
}