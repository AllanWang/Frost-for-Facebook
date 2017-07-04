package com.pitchedapps.frost

import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import ca.allanwang.kau.about.AboutActivityBase
import ca.allanwang.kau.adapters.FastItemThemedAdapter
import ca.allanwang.kau.adapters.ThemableIItem
import ca.allanwang.kau.adapters.ThemableIItemDelegate
import ca.allanwang.kau.iitems.LibraryIItem
import ca.allanwang.kau.utils.*
import ca.allanwang.kau.views.createSimpleRippleDrawable
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.entity.License
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.typeface.IIcon
import com.pitchedapps.frost.utils.Prefs


/**
 * Created by Allan Wang on 2017-06-26.
 */
class AboutActivity : AboutActivityBase(R.string::class.java, configBuilder = {
    textColor = Prefs.textColor
    accentColor = Prefs.accentColor
    backgroundColor = Prefs.bgColor.withMinAlpha(200)
    cutoutForeground = if (0xff3b5998.toInt().isColorVisibleOn(Prefs.bgColor)) 0xff3b5998.toInt() else Prefs.accentColor
    cutoutDrawableRes = R.drawable.frost_f_256
}) {

    override fun getLibraries(libs: Libs): List<Library> {
        val include = arrayOf(
                "materialdialogs",
                "kotterknife",
                "glide",
                "jsoup"
        )

        /*
         * These are great libraries, but either aren't used directly or are too common to be listed
         * Give more emphasis on the unique libs!
         */
        val exclude = arrayOf(
                "GoogleMaterialDesignIcons",
                "intellijannotations",
                "MaterialDesignIconicIcons",
                "MaterialDesignIcons",
                "materialize",
                "appcompat_v7",
                "design",
                "recyclerview_v7",
                "support_v4"
        )
        val l = libs.prepareLibraries(this, include, exclude, true, true)
//        l.forEach { KL.d("Lib ${it.definedName}") }
        return l
    }

    override fun postInflateMainPage(adapter: FastItemThemedAdapter<IItem<*, *>>) {
        /**
         * Frost may not be a library but we're conveying the same info
         */
        val frost = Library().apply {
            libraryName = string(R.string.app_name)
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

    }

    class AboutLinks : AbstractItem<AboutLinks, AboutLinks.ViewHolder>(), ThemableIItem by ThemableIItemDelegate() {
        override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

        override fun getType(): Int = R.id.item_about_links

        override fun getLayoutRes(): Int = R.layout.item_about_links

        override fun bindView(holder: ViewHolder, payloads: MutableList<Any>?) {
            super.bindView(holder, payloads)
            with(holder) {
                bindIconColor(*icons.toTypedArray())
                bindBackgroundColor(container)
            }
        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

            val container: ConstraintLayout by bindView(R.id.about_icons_container)
            val icons: List<ImageView>

            /**
             * There are a lot of constraints to be added to each item just to have them chained properly
             * My as well do it programmatically
             * Initializing the viewholder will setup the icons, scale type and background of all icons,
             * link their click listeners and chain them together via a horizontal spread
             */
            init {
                val c = itemView.context
                val iconData = arrayOf<Pair<Pair<Int, IIcon>, () -> Unit>>(
                        R.id.about_rate to GoogleMaterial.Icon.gmd_star to { c.startPlayStoreLink(R.string.play_store_package_id) },
                        R.id.about_reddit to CommunityMaterial.Icon.cmd_reddit to { c.startLink("https://www.reddit.com/r/FrostForFacebook/") },
                        R.id.about_github to CommunityMaterial.Icon.cmd_github_circle to { c.startLink("https://github.com/AllanWang/Frost-for-Facebook") }
                ).map {
                    //decouple and setup, then only return the image
                    (pair, onClick) ->
                    val (id, icon: IIcon) = pair
                    val image = itemView.findViewById<ImageView>(id)
                    setup(image, icon, onClick)
                    Pair(image, id)
                }
                icons = iconData.map { it.first }
                val ids = iconData.map { it.second }.toIntArray()
                val set = ConstraintSet()
                set.clone(container)
                set.createHorizontalChain(ConstraintSet.PARENT_ID, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,
                        ids, null, ConstraintSet.CHAIN_SPREAD_INSIDE)
                set.applyTo(container)
            }

            fun setup(image: ImageView, icon: IIcon, onClick: () -> Unit) {
                with(image) {
                    setImageDrawable(icon.toDrawable(context, 32))
                    scaleType = ImageView.ScaleType.CENTER
                    background = context.resolveDrawable(android.R.attr.selectableItemBackgroundBorderless)
                    setOnClickListener({ onClick() })
                }
            }
        }
    }
}