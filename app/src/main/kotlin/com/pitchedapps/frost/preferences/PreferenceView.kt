package com.pitchedapps.frost.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.ColorInt
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.ButterKnife
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.bindView
import com.pitchedapps.frost.utils.toDrawable

@SuppressLint("ViewConstructor")
/**
 * Created by Allan Wang on 2017-06-06.
 */
open class PreferenceView<T>(
        context: Context, builder: PrefItem<T>, themeBuilder: ThemeBuilder?
) : LinearLayout(context) {

    val iconFrame: LinearLayout by bindView(R.id.icon_frame)
    val icon: ImageView by bindView(R.id.icon)
    val title: TextView by bindView(R.id.title)
    val desc: TextView by bindView(R.id.summary)
    val checkbox: CheckBox by bindView(R.id.checkbox)
    val key = builder.key
    private val getter = builder.getter
    private val setter = builder.setter
    var pref: T
        get() = getter.invoke(key)
        set(value) {
            setter.invoke(key, value)
        }
    val original = pref
    val hasChanged: Boolean
        get() = original == pref

    init {
        ButterKnife.bind(this)
        title.text = builder.title
        desc.text = builder.description
        if (builder.iicon == null) iconFrame.visibility = View.GONE
        else icon.setImageDrawable(builder.iicon.toDrawable(context, sizeDp = 48))
        if (themeBuilder != null) {
            with(themeBuilder) {
                if (textColor != null) setTextColor(textColor)
                if (accentColor != null) setAccentColor(accentColor)
            }
        }
        setClick(builder.onClick)
    }

    fun setClick(listener: (key: String, current: T, callback: (T) -> Unit) -> Unit) {
        viewWithClick().setOnClickListener {
            listener.invoke(key, pref, { pref = it })
        }
    }

    open fun viewWithClick(): View = this

    open fun setTextColor(@ColorInt color: Int) {
        title.setTextColor(color)
        desc.setTextColor(color)
        desc.alpha = 0.7f
    }

    //Accent color is not needed for basic prefs
    open fun setAccentColor(@ColorInt color: Int) {}

}