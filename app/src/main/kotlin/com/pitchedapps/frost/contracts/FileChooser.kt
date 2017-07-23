package com.pitchedapps.frost.contracts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import ca.allanwang.kau.permissions.PERMISSION_READ_EXTERNAL_STORAGE
import ca.allanwang.kau.permissions.kauRequestPermissions
import com.pitchedapps.frost.utils.L

/**
 * Created by Allan Wang on 2017-07-04.
 */
const val FILE_CHOOSER_REQUEST = 67

interface FileChooserActivityContract {
    fun openFileChooser(filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams)
}

interface FileChooserContract {
    var filePathCallback: ValueCallback<Array<Uri>>?
    fun openFileChooser(activity: Activity, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams)
    fun onActivityResultWeb(requestCode: Int, resultCode: Int, intent: Intent?): Boolean
}

class FileChooserDelegate : FileChooserContract {

    override var filePathCallback: ValueCallback<Array<Uri>>? = null

    override fun openFileChooser(activity: Activity, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams) {
        activity.kauRequestPermissions(PERMISSION_READ_EXTERNAL_STORAGE) {
            granted, _ ->
            if (!granted) return@kauRequestPermissions
            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
            contentSelectionIntent.type = fileChooserParams.acceptTypes?.joinToString(separator = "|") ?: "*/*"
            activity.startActivityForResult(contentSelectionIntent, FILE_CHOOSER_REQUEST)
            this.filePathCallback?.onReceiveValue(null)
            this.filePathCallback = filePathCallback
        }
    }

    override fun onActivityResultWeb(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        L.d("On activity results web $requestCode")
        if (requestCode != FILE_CHOOSER_REQUEST) return false
        var results: Uri? = null

        if (resultCode == Activity.RESULT_OK && intent != null) results = Uri.parse(intent.dataString)
        L.d("Callback received; ${filePathCallback != null} $results")
        filePathCallback?.onReceiveValue(if (results == null) null else arrayOf(results))
        filePathCallback = null
        return true
    }

}