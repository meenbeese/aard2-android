package itkach.aard2

import android.os.Bundle
import android.support.v4.app.ListFragment
import android.text.method.LinkMovementMethod
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView

import itkach.aard2.IconMaker.actionBar
import itkach.aard2.IconMaker.emptyView

abstract class BaseListFragment : ListFragment() {
    protected lateinit var emptyView: View
    var actionMode: ActionMode? = null
    abstract val emptyIcon: Char
    abstract val emptyText: CharSequence?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        emptyView = inflater.inflate(R.layout.empty_view, container, false)
        val emptyText = emptyView.findViewById<TextView>(R.id.empty_text)
        emptyText.movementMethod = LinkMovementMethod.getInstance()
        emptyText.text = this.emptyText
        val emptyIcon = emptyView.findViewById<ImageView>(R.id.empty_icon)
        emptyIcon.setImageDrawable(emptyView(activity, this.emptyIcon))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    protected open fun setSelectionMode(selectionMode: Boolean) {}
    protected open val selectionMenuId: Int
        get() = 0

    protected open fun onSelectionActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return false
    }

    protected open fun supportsSelection(): Boolean {
        return true
    }

    fun finishActionMode(): Boolean {
        actionMode?.finish()
        return actionMode != null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = listView
        listView.emptyView = emptyView
        (listView.parent as ViewGroup).addView(emptyView, 0)
        if (supportsSelection()) {
            listView.itemsCanFocus = false
            listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
            listView.setMultiChoiceModeListener(object : MultiChoiceModeListener {
                override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                    actionMode = mode
                    val inflater = mode.menuInflater
                    inflater.inflate(selectionMenuId, menu)
                    val miDelete = menu.findItem(R.id.blob_descriptor_delete)
                    miDelete?.setIcon(actionBar(activity, IconMaker.IC_TRASH))
                    val miSelectAll = menu.findItem(R.id.blob_descriptor_select_all)
                    miSelectAll?.setIcon(actionBar(activity, IconMaker.IC_SELECT_ALL))
                    setSelectionMode(true)
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                    return false
                }

                override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                    return onSelectionActionItemClicked(mode, item)
                }

                override fun onDestroyActionMode(mode: ActionMode) {
                    setSelectionMode(false)
                    actionMode = null
                }

                override fun onItemCheckedStateChanged(
                    mode: ActionMode,
                    position: Int, id: Long, checked: Boolean
                ) {}
            })
        }
    }
}
