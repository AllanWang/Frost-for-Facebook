/*
 * Copyright 2018 Allan Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pitchedapps.frost.contracts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import ca.allanwang.kau.permissions.PERMISSION_WRITE_EXTERNAL_STORAGE
import ca.allanwang.kau.permissions.kauRequestPermissions
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.L

/**
 * Created by Allan Wang on 2017-07-04.
 */
const val MEDIA_CHOOSER_RESULT = 67

interface FileChooserActivityContract {
    fun openFileChooser(
        filePathCallback: ValueCallback<Array<Uri>?>,
        fileChooserParams: WebChromeClient.FileChooserParams
    )
}

interface FileChooserContract {
    var filePathCallback: ValueCallback<Array<Uri>?>?
    fun Activity.openMediaPicker(
        filePathCallback: ValueCallback<Array<Uri>?>,
        fileChooserParams: WebChromeClient.FileChooserParams
    )

    fun Activity.onActivityResultWeb(requestCode: Int, resultCode: Int, intent: Intent?): Boolean
}

class FileChooserDelegate : FileChooserContract {

    override var filePathCallback: ValueCallback<Array<Uri>?>? = null

    override fun Activity.openMediaPicker(
        filePathCallback: ValueCallback<Array<Uri>?>,
        fileChooserParams: WebChromeClient.FileChooserParams
    ) {
        kauRequestPermissions(PERMISSION_WRITE_EXTERNAL_STORAGE) { granted, _ ->
            if (!granted) {
                filePathCallback.onReceiveValue(null)
                return@kauRequestPermissions
            }
            this@FileChooserDelegate.filePathCallback = filePathCallback
            val intent = Intent()
            intent.type = fileChooserParams.acceptTypes.firstOrNull()
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, string(R.string.pick_image)),
                MEDIA_CHOOSER_RESULT
            )
        }
    }

    override fun Activity.onActivityResultWeb(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?
    ): Boolean {
        L.d { "FileChooser On activity results web $requestCode" }
        if (requestCode != MEDIA_CHOOSER_RESULT) return false
        val data = intent?.data
        filePathCallback?.onReceiveValue(if (data != null) arrayOf(data) else null)
        filePathCallback = null
        return true
    }
}
