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
package com.pitchedapps.frost.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import ca.allanwang.kau.utils.ContextHelper
import ca.allanwang.kau.utils.fadeScaleTransition
import ca.allanwang.kau.utils.setIcon
import ca.allanwang.kau.utils.withArguments
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.iconics.typeface.IIcon
import com.pitchedapps.frost.contracts.DynamicUiContract
import com.pitchedapps.frost.contracts.FrostContentParent
import com.pitchedapps.frost.contracts.MainActivityContract
import com.pitchedapps.frost.contracts.MainFabContract
import com.pitchedapps.frost.enums.FeedSort
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.injectors.ThemeProvider
import com.pitchedapps.frost.prefs.Prefs
import com.pitchedapps.frost.utils.ARG_URL
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.REQUEST_REFRESH
import com.pitchedapps.frost.utils.REQUEST_TEXT_ZOOM
import com.pitchedapps.frost.utils.frostEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive

/**
 * Created by Allan Wang on 2017-11-07.
 *
 * All fragments pertaining to the main view Must be attached to activities implementing
 * [MainActivityContract]
 */
@AndroidEntryPoint
abstract class BaseFragment : Fragment(), CoroutineScope, FragmentContract, DynamicUiContract {

  companion object {
    private const val ARG_POSITION = "arg_position"
    private const val ARG_VALID = "arg_valid"

    internal operator fun invoke(
      base: () -> BaseFragment,
      prefs: Prefs,
      useFallback: Boolean,
      data: FbItem,
      position: Int
    ): BaseFragment {
      val fragment = if (useFallback) WebFragment() else base()
      val d = if (data == FbItem.FEED) FeedSort(prefs.feedSort).item else data
      fragment.withArguments(ARG_URL to d.url, ARG_POSITION to position)
      d.put(fragment.requireArguments())
      return fragment
    }
  }

  @Inject protected lateinit var mainContract: MainActivityContract

  @Inject protected lateinit var fbCookie: FbCookie

  @Inject protected lateinit var prefs: Prefs

  @Inject protected lateinit var themeProvider: ThemeProvider

  open lateinit var job: Job
  override val coroutineContext: CoroutineContext
    get() = ContextHelper.dispatcher + job

  override val baseUrl: String by lazy { requireArguments().getString(ARG_URL)!! }
  override val baseEnum: FbItem by lazy { FbItem[arguments]!! }
  override val position: Int by lazy { requireArguments().getInt(ARG_POSITION) }

  override var valid: Boolean
    get() = requireArguments().getBoolean(ARG_VALID, true)
    set(value) {
      if (!isActive || value || this is WebFragment) return
      requireArguments().putBoolean(ARG_VALID, value)
      frostEvent("Native Fallback", "Item" to baseEnum.name)
      mainContract.reloadFragment(this)
    }

  override var firstLoad: Boolean = true
  private var onCreateRunnable: ((FragmentContract) -> Unit)? = null

  override var content: FrostContentParent? = null

  protected abstract val layoutRes: Int

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    job = SupervisorJob()
    firstLoad = true
  }

  final override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(layoutRes, container, false)
    val content =
      view as? FrostContentParent
        ?: throw IllegalArgumentException(
          "layoutRes for fragment must return view implementing FrostContentParent"
        )
    this.content = content
    content.bind(this)
    return view
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    onCreateRunnable?.invoke(this)
    onCreateRunnable = null
    firstLoadRequest()
    attach(mainContract)
  }

  override fun setUserVisibleHint(isVisibleToUser: Boolean) {
    super.setUserVisibleHint(isVisibleToUser)
    firstLoadRequest()
  }

  override fun firstLoadRequest() {
    val core = core ?: return
    if (userVisibleHint && isVisible && firstLoad) {
      core.reloadBase(true)
      firstLoad = false
    }
  }

  override fun post(action: (fragment: FragmentContract) -> Unit) {
    onCreateRunnable = action
  }

  override fun setTitle(title: String) {
    mainContract.setTitle(title)
  }

  override fun attach(contract: MainActivityContract) {
    contract.fragmentFlow
      .flowWithLifecycle(viewLifecycleOwner.lifecycle)
      .onEach { flag ->
        when (flag) {
          REQUEST_REFRESH -> {
            core?.apply {
              clearHistory()
              firstLoad = true
              firstLoadRequest()
            }
          }
          position -> {
            contract.setTitle(baseEnum.titleId)
            updateFab(contract)
            core?.active = true
          }
          -(position + 1) -> {
            core?.active = false
          }
          REQUEST_TEXT_ZOOM -> {
            reloadTextSize()
          }
        }
      }
      .launchIn(this)
  }

  override fun updateFab(contract: MainFabContract) {
    contract.hideFab() // default
  }

  protected fun FloatingActionButton.update(iicon: IIcon, click: () -> Unit) {
    if (isShown) {
      fadeScaleTransition { setIcon(iicon, themeProvider.iconColor) }
    } else {
      setIcon(iicon, themeProvider.iconColor)
      show()
    }
    setOnClickListener { click() }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    L.i { "Fragment on destroy $position ${hashCode()}" }
    content?.destroy()
    content = null
  }

  override fun onDestroy() {
    job.cancel()
    super.onDestroy()
  }

  override fun reloadTheme() {
    reloadThemeSelf()
    content?.reloadTextSize()
  }

  override fun reloadThemeSelf() {
    // intentionally blank
  }

  override fun reloadTextSize() {
    reloadTextSizeSelf()
    content?.reloadTextSize()
  }

  override fun reloadTextSizeSelf() {
    // intentionally blank
  }

  override fun onBackPressed(): Boolean = content?.core?.onBackPressed() ?: false

  override fun onTabClick(): Unit = content?.core?.onTabClicked() ?: Unit
}
