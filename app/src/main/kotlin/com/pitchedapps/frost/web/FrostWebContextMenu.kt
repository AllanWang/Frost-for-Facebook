package com.pitchedapps.frost.web

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.widget.TextView
import ca.allanwang.kau.logging.KL
import ca.allanwang.kau.utils.*
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-07-06.
 */
class FrostWebContextMenu @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var url = ""

    val urlHolder = TextView(context, attrs, defStyleAttr)

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        elevation = 20f
        setBackgroundColor(0x80000000.toInt())
        gone()

        val tc = Prefs.textColor
        val bg = Prefs.bgColor.colorToForeground(0.1f).withAlpha(255)

        urlHolder.apply {
            isVerticalScrollBarEnabled = true
            movementMethod = ScrollingMovementMethod()
            maxHeight = 60.dpToPx
        }
        addView(urlHolder)

        //collection of items in our menu and their click event
        val data = arrayOf(
                R.string.copy_link to { context.copyToClipboard(url) }
        )

        //add views and extract ids
        val views = data.map {
            (textId, onClick) ->
            val tv = TextView(context).apply {
                text = context.string(textId)
                setOnClickListener({ onClick(); close() })
            }
            addView(tv)
            tv
        }.toMutableList()

        views.add(0, urlHolder)

        val ids = views.mapIndexed { index, textView ->
            textView.apply {
                id = 74329 + index //totally arbitrary
                setTextColor(tc)
                setBackgroundColor(bg)
            }
            KL.d("ID ${textView.text}")
            textView.id
        }

        //clone to set only after ids are set
        val set = ConstraintSet()
        set.clone(this)

        ids.forEach {
            set.connect(it, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 16)
            set.connect(it, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16)
        }


        set.createVerticalChain(ConstraintSet.PARENT_ID, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,
                ids.toIntArray(), null, ConstraintSet.CHAIN_PACKED)

        set.applyTo(this)
        setOnClickListener {
            close()
        }
    }

    fun close() {
        transitionAuto()
        gone()
    }

    fun show(url: String) {
        this.url = url
        urlHolder.text = this.url
        transitionAuto()
        visible()
    }

}