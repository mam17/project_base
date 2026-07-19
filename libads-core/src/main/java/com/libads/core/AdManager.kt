package com.libads.core

import android.app.Activity
import android.app.Application
import android.view.ViewGroup
import com.libads.core.callback.AdLoadCallback
import com.libads.core.callback.AdShowCallback
import com.libads.core.provider.AdProvider

/**
 * API duy nhất mà app bên ngoài cần biết. App KHÔNG được đụng trực tiếp
 * vào AdProvider hay SDK network cụ thể — mọi thứ đi qua đây.
 *
 * Cách dùng ở app:
 * ```
 * AdManager.init(application) {
 *     registerProvider(AdMobProvider())   // đến từ module libads-admob
 * }
 * AdManager.getInstance().preload(myInterstitialUnit)
 * AdManager.getInstance().show(activity, myInterstitialUnit, callback)
 * ```
 */
interface AdManager {

    /** Đăng ký 1 provider (network ads). Gọi trong init{}, trước khi load/show. */
    fun registerProvider(provider: AdProvider)

    /** Preload/cache sẵn quảng cáo cho 1 placement, không show ngay. */
    fun preload(adUnit: AdUnit, callback: AdLoadCallback? = null)

    /** Kiểm tra quảng cáo đã sẵn sàng để show chưa (đã cache/preload thành công) */
    fun isReady(adUnit: AdUnit): Boolean

    /** Show quảng cáo full-screen. Tự load nếu chưa cache, có timeout an toàn. */
    fun show(activity: Activity, adUnit: AdUnit, callback: AdShowCallback? = null)

    /** Render banner/native ad vào 1 container trong layout */
    fun renderInto(container: ViewGroup, adUnit: AdUnit, callback: AdLoadCallback? = null)

    /** Giải phóng tài nguyên của 1 placement cụ thể */
    fun destroy(adUnit: AdUnit)

    /** Giải phóng toàn bộ, gọi khi app kết thúc (thường không cần thiết, tự quản qua lifecycle) */
    fun destroyAll()

    companion object {
        @Volatile
        private var instance: AdManager? = null

        /**
         * Khởi tạo lib, chỉ gọi 1 lần trong Application.onCreate().
         * [setup] dùng để đăng ký các provider.
         */
        fun init(application: Application, setup: AdManager.() -> Unit): AdManager {
            return instance ?: synchronized(this) {
                instance ?: com.libads.core.internal.AdManagerImpl(application)
                    .also { it.setup() }
                    .also { instance = it }
            }
        }

        fun getInstance(): AdManager =
            instance ?: error(
                "AdManager chưa được init. Gọi AdManager.init(application) { ... } trong Application.onCreate() trước."
            )
    }
}
