package com.pitchedapps.frost.contracts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import ca.allanwang.kau.imagepicker.kauLaunchImagePicker
import ca.allanwang.kau.imagepicker.kauOnImagePickerResult
import com.pitchedapps.frost.activities.MediaPickerActivity
import com.pitchedapps.frost.utils.L

/**
 * Created by Allan Wang on 2017-07-04.
 */
const val IMAGE_CHOOSER_REQUEST = 67

interface FileChooserActivityContract {
    fun openFileChooser(filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams)
}

interface FileChooserContract {
    var filePathCallback: ValueCallback<Array<Uri>>?
    fun Activity.openImagePicker(filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams)
    fun Activity.onActivityResultWeb(requestCode: Int, resultCode: Int, intent: Intent?): Boolean
}

class FileChooserDelegate : FileChooserContract {

    override var filePathCallback: ValueCallback<Array<Uri>>? = null

    override fun Activity.openImagePicker(filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams) {
        this@FileChooserDelegate.filePathCallback = filePathCallback
        kauLaunchImagePicker(MediaPickerActivity::class.java, IMAGE_CHOOSER_REQUEST)
    }

    override fun Activity.onActivityResultWeb(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        L.d("FileChooser On activity results web $requestCode")
        if (requestCode != IMAGE_CHOOSER_REQUEST) return false
        val results = kauOnImagePickerResult(resultCode, intent).map { it.uri }.toTypedArray()
        L.d("FileChooser result ${results.contentToString()}")
        //proper path content://com.android.providers.media.documents/document/image%3A36341
        L.d("FileChooser Callback received; ${filePathCallback != null}")
        filePathCallback?.onReceiveValue(results)
        filePathCallback = null
        return true
    }

}