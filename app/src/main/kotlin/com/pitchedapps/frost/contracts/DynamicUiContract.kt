package com.pitchedapps.frost.contracts

/**
 * Functions that will modify the current ui
 */
interface DynamicUiContract {

    /**
     * Change all necessary view components to the new theme
     * Also propagate where applicable
     */
    fun reloadTheme()

    /**
     * Change theme without propagation
     */
    fun reloadThemeSelf()

    /**
     * Change text size & propagate
     */
    fun reloadTextSize()


    /**
     * Change text size without propagation
     */
    fun reloadTextSizeSelf()

}