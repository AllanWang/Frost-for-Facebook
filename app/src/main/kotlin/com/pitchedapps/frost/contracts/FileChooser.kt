package com.pitchedapps.frost.contracts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import ca.allanwang.kau.imagepicker.kauLaunchImagePicker
import ca.allanwang.kau.imagepicker.kauOnImagePickerResult
import com.pitchedapps.frost.activities.ImagePickerActivity
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
        kauLaunchImagePicker(ImagePickerActivity::class.java, IMAGE_CHOOSER_REQUEST)
    }

    override fun Activity.onActivityResultWeb(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        L.d("On activity results web $requestCode")
        if (requestCode != IMAGE_CHOOSER_REQUEST) return false
        val results = kauOnImagePickerResult(resultCode, intent).map { Uri.parse(it.data) }
        L.d("Callback received; ${filePathCallback != null}")
        filePathCallback?.onReceiveValue(results.toTypedArray())
        filePathCallback = null
        return true
    }

}