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
import com.pitchedapps.frost.injectors.ThemeProvider
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostSnackbar
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

/**
 * Created by Allan Wang on 2017-07-04.
 */
private const val MEDIA_CHOOSER_RESULT = 67

interface WebFileChooser {
    fun openMediaPicker(
        filePathCallback: ValueCallback<Array<Uri>?>,
        fileChooserParams: WebChromeClient.FileChooserParams
    )

    fun onActivityResultWeb(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?
    ): Boolean
}

class WebFileChooserImpl @Inject internal constructor(
    private val activity: Activity,
    private val themeProvider: ThemeProvider
) : WebFileChooser {
    private var filePathCallback: ValueCallback<Array<Uri>?>? = null

    override fun openMediaPicker(
        filePathCallback: ValueCallback<Array<Uri>?>,
        fileChooserParams: WebChromeClient.FileChooserParams
    ) {
        activity.kauRequestPermissions(PERMISSION_WRITE_EXTERNAL_STORAGE) { granted, _ ->
            if (!granted) {
                L.d { "Failed to get write permissions" }
                activity.frostSnackbar(R.string.file_chooser_not_found, themeProvider)
                filePathCallback.onReceiveValue(null)
                return@kauRequestPermissions
            }
            this.filePathCallback = filePathCallback
            val intent = Intent()
            intent.type = fileChooserParams.acceptTypes.firstOrNull()
            intent.action = Intent.ACTION_GET_CONTENT
            activity.startActivityForResult(
                Intent.createChooser(intent, activity.string(R.string.pick_image)),
                MEDIA_CHOOSER_RESULT
            )
        }
    }

    override fun onActivityResultWeb(
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

@Module
@InstallIn(ActivityComponent::class)
interface WebFileChooserModule {
    @Binds
    @ActivityScoped
    fun webFileChooser(to: WebFileChooserImpl): WebFileChooser
}
