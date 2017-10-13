package com.pitchedapps.frost.contracts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.L

/**
 * Created by Allan Wang on 2017-07-04.
 */
const val MEDIA_CHOOSER_RESULT = 67

interface FileChooserActivityContract {
    fun openFileChooser(filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams)
}

interface FileChooserContract {
    var filePathCallback: ValueCallback<Array<Uri>>?
    fun Activity.openMediaPicker(filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams)
    fun Activity.onActivityResultWeb(requestCode: Int, resultCode: Int, intent: Intent?): Boolean
}

class FileChooserDelegate : FileChooserContract {

    override var filePathCallback: ValueCallback<Array<Uri>>? = null

    override fun Activity.openMediaPicker(filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams) {
        this@FileChooserDelegate.filePathCallback = filePathCallback
        val intent = Intent()
        intent.type = fileChooserParams.acceptTypes.firstOrNull()
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, string(R.string.pick_image)), MEDIA_CHOOSER_RESULT)
    }

    override fun Activity.onActivityResultWeb(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        L.d("FileChooser On activity results web $requestCode")
        if (requestCode != MEDIA_CHOOSER_RESULT) return false
        val data = intent?.data
        if (data != null)
            filePathCallback?.onReceiveValue(arrayOf(data))
        filePathCallback = null
        return true
    }

}