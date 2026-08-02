package com.example.myapplication.ads

import android.view.ViewGroup
import com.libads.core.AdManager
import com.libads.core.CollapsiblePositionType
import com.libads.core.callback.AdLoadCallback

object AdBannerUtils {

    fun showBanner(
        container: ViewGroup,
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
        callback: AdLoadCallback? = null
    ) {
        val adUnit = AdUnits.mainBanner.copy(
            collapsiblePositionType = collapsiblePositionType
        )
        AdManager.getInstance().renderInto(container, adUnit, callback)
    }
}
