package com.pitchedapps.frost

import android.support.v7.app.AppCompatActivity
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.materialDialog

/**
 * Created by Allan Wang on 2017-06-12.
 */
open class BaseActivity : AppCompatActivity() {
    override fun onBackPressed() {
        if (isTaskRoot && Prefs.exitConfirmation) {
            materialDialog {
                title(R.string.exit)
                content(R.string.exit_confirmation)
                positiveText(android.R.string.yes)
                negativeText(android.R.string.no)
                onPositive { _, _ -> super.onBackPressed() }
                checkBoxPromptRes(R.string.do_not_show_again, false, { _, b -> Prefs.exitConfirmation = !b })
                show()
            }
        } else super.onBackPressed()
    }
}