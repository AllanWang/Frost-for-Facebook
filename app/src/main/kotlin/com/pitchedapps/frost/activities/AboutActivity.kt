package com.pitchedapps.frost.activities

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import ca.allanwang.kau.about.AboutActivityBase
import ca.allanwang.kau.about.LibraryIItem
import ca.allanwang.kau.adapters.FastItemThemedAdapter
import ca.allanwang.kau.adapters.ThemableIItem
import ca.allanwang.kau.adapters.ThemableIItemDelegate
import ca.allanwang.kau.animators.FadeScaleAnimatorAdd
import ca.allanwang.kau.animators.KauAnimator
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
import com.pitchedapps.frost.utils.Prefs
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.security.InvalidParameterException


/**
 * Created by Allan Wang on 2017-06-26.
 */
class AboutActivity : AboutActivityBase(null, {
    textColor = Prefs.textColor
    accentColor = Prefs.accentColor
    backgroundColor = Prefs.bgColor.withMinAlpha(200)
    cutoutForeground = if (0xff3b5998.toInt().isColorVisibleOn(Prefs.bgColor)) 0xff3b5998.toInt() else Prefs.accentColor
    cutoutDrawableRes = R.drawable.frost_f_256
}) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        faqAdapter = FastItemThemedAdapter(configs)
    }

    override val pageCount: Int = 3

    override fun getPage(position: Int, layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return when (position) {
            0 -> inflateMainPage(layoutInflater, parent, position)
            1 -> inflateLibPage(layoutInflater, parent, position)
            else -> throw InvalidParameterException()
        }
    }

    private var faqPage:Int = -1
    private var faqRecycler: RecyclerView?=null
    private lateinit var faqAdapter:FastItemThemedAdapter<IItem<*, *>>

    fun inflateFaqPage(layoutInflater: LayoutInflater, parent: ViewGroup, position: Int): View {
        faqPage = position
        val v = layoutInflater.inflate(R.layout.kau_recycler_detached_background, parent, false)
        val recycler = v.findViewById<RecyclerView>(R.id.kau_recycler_detached)
        faqRecycler = recycler
        recycler.withMarginDecoration(16, KAU_BOTTOM)
        recycler.adapter = libAdapter
        recycler.itemAnimator = KauAnimator(addAnimator = FadeScaleAnimatorAdd(scaleFactor = 0.7f, itemDelayFactor = 0.2f)).apply { addDuration = 300; interpolator = AnimHolder.decelerateInterpolator(this@AboutActivityBase) }
        val background = v.findViewById<View>(R.id.kau_recycler_detached_background)
        if (configs.backgroundColor != null) background.setBackgroundColor(configs.backgroundColor!!.colorToForeground())
        doAsync {
            libItems = getLibraries(
                    if (rClass == null) Libs(this@AboutActivityBase) else Libs(this@AboutActivityBase, Libs.toStringArray(rClass.fields))
            ).map { LibraryIItem(it) }
            if (libPage >= 0 && pageStatus[libPage] == 1)
                uiThread { addLibItems() }
        }
        return v
    }

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
                "subsamplingscaleimageview"
        )

        val l = libs.prepareLibraries(this, include, null, false, true)
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
                        setOnClickListener({ onClick() })
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