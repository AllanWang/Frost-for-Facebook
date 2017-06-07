package com.pitchedapps.frost.preferences

import android.content.Context
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import com.mikepenz.iconics.typeface.IIcon
import com.pitchedapps.frost.utils.toColor
import com.pitchedapps.frost.utils.toString

/**
 * Created by Allan Wang on 2017-06-06.
 */
//fun Context.preference(setup: PreferenceBuild)

@DslMarker
annotation class PreferenceMarker

@PreferenceMarker
enum class PreferenceType() {
    HEADER, TEXT, CHECKBOX;

    fun <T> createView(builder: PrefItem<T>) {

    }
}

@PreferenceMarker
class PrefFrame(val context: Context, val theme: ThemeBuilder? = null, builder: PrefFrameBuilder.() -> Unit)

@PreferenceMarker
class PrefFrameBuilder() {
    val items: MutableList<PrefItem<*>> = mutableListOf()

    fun <T> item(item: PrefItem<T>) {
        items.add(item)
    }
}

@PreferenceMarker
class ThemeBuilder(context: Context, @ColorInt text: Int? = null, @ColorRes textRes: Int? = null,
                   @ColorInt accent: Int? = null, @ColorRes accentRes: Int? = null,
                   @ColorInt background: Int? = null, @ColorRes backgroundRes: Int? = null) {
    val textColor = text ?: textRes?.toColor(context)
    val accentColor = accent ?: accentRes?.toColor(context)
    val backgroundColor = background ?: backgroundRes?.toColor(context)
}

@PreferenceMarker
class PrefItem<T>(
        context: Context,
        val key: String,
        title: String? = null,
        @StringRes titleRes: Int? = null,
        description: String? = null,
        @StringRes descriptionRes: Int? = null,
        val onClick: (key: String, current: T, callback: (T) -> Unit) -> Unit,
        val iicon: IIcon? = null,
        val getter: (key: String) -> T,
        val setter: (key: String, value: T) -> Unit
) {
    val title: String = titleRes?.toString(context) ?: title ?: ""
    val description: String = descriptionRes?.toString(context) ?: description ?: ""
    val originalValue: T by lazy { getter.invoke(key) }
}
