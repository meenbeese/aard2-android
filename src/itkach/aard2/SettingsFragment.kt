package itkach.aard2

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.support.v4.provider.DocumentFile
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.ListView
import android.widget.Toast

import itkach.aard2.Application.FileTooBigException

class SettingsFragment : ListFragment() {
    private var clearCacheConfirmationDialog: AlertDialog? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listAdapter = SettingsListAdapter(this)
        setListAdapter(listAdapter)
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        if (position == SettingsListAdapter.POS_CLEAR_CACHE) {
            val builder = AlertDialog.Builder(activity)
            builder.setMessage(R.string.confirm_clear_cached_content)
                .setPositiveButton(android.R.string.yes) { _: DialogInterface?, _: Int ->
                    val webView = WebView(activity)
                    webView.clearCache(true)
                }
                .setNegativeButton(android.R.string.no) { _: DialogInterface?, _: Int -> }
            clearCacheConfirmationDialog = builder.create() as AlertDialog
            clearCacheConfirmationDialog?.setOnDismissListener {
                clearCacheConfirmationDialog = null
            }
            clearCacheConfirmationDialog?.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode != SettingsListAdapter.CSS_SELECT_REQUEST) {
            Log.d(TAG, "Unknown request code: $requestCode")
            return
        }
        val dataUri = data.data
        Log.d(TAG,"req code $requestCode, result code: $resultCode, data: $dataUri")
        if (resultCode == Activity.RESULT_OK && dataUri != null) {
            try {
                val `is` = activity.contentResolver.openInputStream(dataUri)
                val documentFile = DocumentFile.fromSingleUri(context, dataUri)
                var fileName = documentFile.name
                var userCss = Application.readTextFile(`is`, 256 * 1024)
                Log.d(TAG, fileName)
                Log.d(TAG, userCss)
                val lastIndexOfDot = fileName.lastIndexOf(".")
                if (lastIndexOfDot > -1) {
                    fileName = fileName.substring(0, lastIndexOfDot)
                }
                if (fileName.isEmpty()) {
                    fileName = "???"
                }
                val prefs = activity.getSharedPreferences(
                    "userStyles", Activity.MODE_PRIVATE
                )
                userCss = userCss.replace("\r", "").replace("\n", "\\n")
                val editor = prefs.edit()
                editor.putString(fileName, userCss)
                val saved = editor.commit()
                if (!saved) {
                    Toast.makeText(activity, R.string.msg_failed_to_store_user_style, Toast.LENGTH_LONG).show()
                }
            } catch (e: FileTooBigException) {
                Log.d(TAG, "File is too big: $dataUri")
                Toast.makeText(activity, R.string.msg_file_too_big, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.d(TAG, "Failed to load: $dataUri", e)
                Toast.makeText(activity, R.string.msg_failed_to_read_file, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (clearCacheConfirmationDialog != null) {
            clearCacheConfirmationDialog!!.dismiss()
        }
    }

    companion object {
        private val TAG = SettingsFragment::class.java.simpleName
    }
}
