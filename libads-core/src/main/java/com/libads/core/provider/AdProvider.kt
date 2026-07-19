package com.libads.core.provider

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import com.libads.core.AdUnit
import com.libads.core.callback.AdLoadCallback
import com.libads.core.callback.AdShowCallback

/**
 * Hợp đồng mà mọi module network ads (libads-admob, libads-facebook, libads-unity...)
 * phải implement. Core KHÔNG biết gì về SDK cụ thể của network nào —
 * điều này giúp thêm/bớt network mà không sửa code core.
 */
interface AdProvider {

    /** Tên định danh duy nhất, phải khớp với AdUnit.providerName, ví dụ "admob" */
    val name: String

    /** Khởi tạo SDK của network (thường gọi 1 lần lúc app start) */
    fun initialize(context: Context, onInitialized: (success: Boolean) -> Unit)

    /** Load quảng cáo, không tự show. Kết quả trả về qua callback. */
    fun load(context: Context, adUnit: AdUnit, callback: AdLoadCallback)

    /** Kiểm tra đã có ad sẵn sàng trong cache của provider chưa (dùng cho preload) */
    fun isReady(adUnit: AdUnit): Boolean

    /** Show quảng cáo full-screen (interstitial, rewarded, app open) */
    fun show(activity: Activity, adUnit: AdUnit, callback: AdShowCallback)

    /**
     * Gắn banner/native ad vào container có sẵn trong layout của app.
     * Với banner/native, "show" thực chất là "render vào view" nên tách API riêng.
     */
    fun renderInto(container: ViewGroup, adUnit: AdUnit, callback: AdLoadCallback)

    /** Giải phóng tài nguyên ad đã load (tránh leak khi Activity/Fragment destroy) */
    fun destroy(adUnit: AdUnit)
}
