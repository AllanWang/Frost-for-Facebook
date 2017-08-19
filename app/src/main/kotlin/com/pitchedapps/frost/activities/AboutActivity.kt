package com.pitchedapps.frost.activities

import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import ca.allanwang.kau.about.AboutActivityBase
import ca.allanwang.kau.about.LibraryIItem
import ca.allanwang.kau.adapters.FastItemThemedAdapter
import ca.allanwang.kau.adapters.ThemableIItem
import ca.allanwang.kau.adapters.ThemableIItemDelegate
import ca.allanwang.kau.utils.*
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.entity.License
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.typeface.IIcon
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs


/**
 * Created by Allan Wang on 2017-06-26.
 */
class AboutActivity : AboutActivityBase(null, {
    textColor = Prefs.textColor
    accentColor = Prefs.accentColor
    backgroundColor = Prefs.bgColor.withMinAlpha(200)
    cutoutForeground = Prefs.accentColor
    cutoutDrawableRes = R.drawable.frost_f_256
    faqPageTitleRes = R.string.faq_title
    faqXmlRes = R.xml.frost_faq
    faqParseNewLine = false
}) {

    override fun getLibraries(libs: Libs): List<Library> {
        val include = arrayOf(
                "AboutLibraries",
                "AndroidIconics",
                "androidin_appbillingv3",
                "androidslidinguppanel",
                "Crashlytics",
                "dbflow",
                "fastadapter",
                "glide",
                "Jsoup",
                "kau",
                "kotterknife",
                "materialdialogs",
                "materialdrawer",
                "rxjava",
                "subsamplingscaleimageview"
        )

        val l = libs.prepareLibraries(this, include, null, false, true)
//        l.forEach { KL.d("Lib ${it.definedName}") }
        return l
    }

    var lastClick = -1L
    var clickCount = 0

    override fun postInflateMainPage(adapter: FastItemThemedAdapter<IItem<*, *>>) {
        /**
         * Frost may not be a library but we're conveying the same info
         */
        val frost = Library().apply {
            libraryName = string(R.string.frost_name)
            author = "Pitched Apps"
            libraryWebsite = "https://github.com/AllanWang/Frost-for-Facebook"
            isOpenSource = true
            libraryDescription = string(R.string.frost_description)
            libraryVersion = BuildConfig.VERSION_NAME
            license = License().apply {
                licenseName = "GNU GPL v3"
                licenseWebsite = "https://www.gnu.org/licenses/gpl-3.0.en.html"
            }
        }
        adapter.add(LibraryIItem(frost)).add(AboutLinks())
        adapter.withOnClickListener { _, _, item, _ ->
            if (item is LibraryIItem) {
                val now = System.currentTimeMillis()
                if (now - lastClick > 500)
                    clickCount = 0
                else
                    clickCount++
                lastClick = now
                if (clickCount == 7 && !Prefs.debugSettings) {
                    Prefs.debugSettings = true
                    L.d("Debugging section enabled")
                    toast(R.string.debug_toast_enabled)
                }
            }
            false
        }
    }

    class AboutLinks : AbstractItem<AboutLinks, AboutLinks.ViewHolder>(), ThemableIItem by ThemableIItemDelegate() {
        override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

        override fun getType(): Int = R.id.item_about_links

        override fun getLayoutRes(): Int = R.layout.item_about_links

        override fun bindView(holder: ViewHolder, payloads: MutableList<Any>?) {
            super.bindView(holder, payloads)
            with(holder) {
                bindIconColor(*images.toTypedArray())
                bindBackgroundColor(container)
            }
        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

            val container: ConstraintLayout by bindView(R.id.about_icons_container)
            val images: List<ImageView>

            /**
             * There are a lot of constraints to be added to each item just to have them chained properly
             * My as well do it programmatically
             * Initializing the viewholder will setup the icons, scale type and background of all icons,
             * link their click listeners and chain them together via a horizontal spread
             */
            init {
                val c = itemView.context
                val size = c.dimenPixelSize(R.dimen.kau_avatar_bounds)
                images = arrayOf<Pair<IIcon, () -> Unit>>(
                        GoogleMaterial.Icon.gmd_star to { c.startPlayStoreLink(R.string.play_store_package_id) },
                        CommunityMaterial.Icon.cmd_reddit to { c.startLink("https://www.reddit.com/r/FrostForFacebook/") },
                        CommunityMaterial.Icon.cmd_github_circle to { c.startLink("https://github.com/AllanWang/Frost-for-Facebook") }
                ).mapIndexed { i, (icon, onClick) ->
                    ImageView(c).apply {
                        layoutParams = ViewGroup.LayoutParams(size, size)
                        id = 109389 + i
                        setImageDrawable(icon.toDrawable(context, 32))
                        scaleType = ImageView.ScaleType.CENTER
                        background = context.resolveDrawable(android.R.attr.selectableItemBackgroundBorderless)
                        setOnClickListener { onClick() }
                        container.addView(this)
                    }
                }
                val set = ConstraintSet()
                set.clone(container)
                set.createHorizontalChain(ConstraintSet.PARENT_ID, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,
                        images.map { it.id }.toIntArray(), null, ConstraintSet.CHAIN_SPREAD_INSIDE)
                set.applyTo(container)
            }
        }
    }
}