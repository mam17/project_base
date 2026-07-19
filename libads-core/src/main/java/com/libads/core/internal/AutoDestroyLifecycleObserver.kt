package com.libads.core.internal

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.libads.core.AdManager
import com.libads.core.AdUnit

/**
 * Gắn vào lifecycle của Activity/Fragment để tự động gọi destroy() cho các AdUnit
 * khi màn hình bị huỷ, tránh trường hợp app quên tự dọn dẫn đến leak Activity context
 * (lỗi rất phổ biến khi tích hợp ads SDK thủ công).
 *
 * Cách dùng ở app:
 * ```
 * lifecycle.addObserver(AutoDestroyLifecycleObserver(AdManager.getInstance(), listOf(interstitialUnit)))
 * ```
 */
class AutoDestroyLifecycleObserver(
    private val adManager: AdManager,
    private val adUnits: List<AdUnit>
) : DefaultLifecycleObserver {

    override fun onDestroy(owner: LifecycleOwner) {
        adUnits.forEach { adManager.destroy(it) }
    }
}
