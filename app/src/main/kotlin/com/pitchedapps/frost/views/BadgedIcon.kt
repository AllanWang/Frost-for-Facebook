package com.pitchedapps.frost.views

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import ca.allanwang.kau.utils.*
import com.mikepenz.iconics.typeface.IIcon
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.Prefs


/**
 * Created by Allan Wang on 2017-06-19.
 */
class BadgedIcon @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val badgeTextView: TextView by bindView(R.id.badge_text)
    val badgeImage: ImageView by bindView(R.id.badge_image)

    init {
        inflate(context, R.layout.view_badged_icon, this)
        val badgeColor = Prefs.mainActivityLayout.backgroundColor().withAlpha(255).colorToForeground(0.2f)
        val badgeBackground = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(badgeColor, badgeColor))
        badgeBackground.cornerRadius = 13.dpToPx.toFloat()
        badgeTextView.background = badgeBackground
        badgeTextView.setTextColor(Prefs.mainActivityLayout.iconColor())
    }


    var iicon: IIcon? = null
        get() = field
        set(value) {
            field = value
            badgeImage.setImageDrawable(value?.toDrawable(context, color = Prefs.mainActivityLayout.iconColor()))
        }

    fun setAllAlpha(alpha: Float) {
        //badgeTextView.setTextColor(Prefs.textColor.withAlpha(alpha.toInt()))
        badgeImage.drawable.alpha = alpha.toInt()
    }

    var badgeText: String?
        get() = badgeTextView.text.toString()
        set(value) {
            if (badgeTextView.text == value) return
            badgeTextView.text = value
            if (value != null && value != "0") badgeTextView.visible()
            else badgeTextView.gone()
        }

}