package com.pitchedapps.frost.contracts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import ca.allanwang.kau.mediapicker.kauLaunchMediaPicker
import ca.allanwang.kau.mediapicker.kauOnMediaPickerResult
import com.pitchedapps.frost.activities.ImagePickerActivity
import com.pitchedapps.frost.activities.VideoPickerActivity
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
        val isVideo = fileChooserParams.acceptTypes.firstOrNull() == "video/*"
        kauLaunchMediaPicker(if (isVideo) VideoPickerActivity::class.java else ImagePickerActivity::class.java, MEDIA_CHOOSER_RESULT)
    }

    override fun Activity.onActivityResultWeb(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        L.d("FileChooser On activity results web $requestCode")
        if (requestCode != MEDIA_CHOOSER_RESULT) return false
        val results = kauOnMediaPickerResult(resultCode, intent).map { it.uri }.toTypedArray()
        L.i("FileChooser result ${results.contentToString()}")
        //proper path content://com.android.providers.media.documents/document/image%3A36341
        L.d("FileChooser Callback received; ${filePathCallback != null}")
        filePathCallback?.onReceiveValue(results)
        filePathCallback = null
        return true
    }

}