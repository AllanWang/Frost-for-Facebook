package com.pitchedapps.frost

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import ca.allanwang.kau.utils.darken
import ca.allanwang.kau.utils.navigationBarColor
import ca.allanwang.kau.utils.statusBarColor
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.materialDialogThemed
import com.pitchedapps.frost.utils.setFrostTheme

/**
 * Created by Allan Wang on 2017-06-12.
 */
open class BaseActivity : AppCompatActivity() {
    override fun onBackPressed() {
        if (isTaskRoot && Prefs.exitConfirmation) {
            materialDialogThemed {
                title(R.string.exit)
                content(R.string.exit_confirmation)
                positiveText(android.R.string.yes)
                negativeText(android.R.string.no)
                onPositive { _, _ -> super.onBackPressed() }
                checkBoxPromptRes(R.string.do_not_show_again, false, { _, b -> Prefs.exitConfirmation = !b })
            }
        } else super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFrostTheme()
    }

}