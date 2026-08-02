package com.libads.core.provider.admob

import com.google.android.gms.ads.ResponseInfo
import com.libads.core.callback.AdMediationInfo

internal fun ResponseInfo?.toAdMediationInfo(): AdMediationInfo? {
    val responseInfo = this ?: return null
    val adapterInfo = responseInfo.loadedAdapterResponseInfo
    val adapterClassName = adapterInfo?.adapterClassName
        ?: responseInfo.mediationAdapterClassName
    val adSourceName = adapterInfo?.adSourceName
    if (adapterClassName.isNullOrBlank() && adSourceName.isNullOrBlank()) return null

    return AdMediationInfo(
        networkName = mediationNetworkName(adSourceName, adapterClassName),
        adapterClassName = adapterClassName,
        adSourceName = adSourceName,
        adSourceId = adapterInfo?.adSourceId,
        adSourceInstanceName = adapterInfo?.adSourceInstanceName,
        adSourceInstanceId = adapterInfo?.adSourceInstanceId,
        latencyMillis = adapterInfo?.latencyMillis
    )
}

internal fun mediationNetworkName(adSourceName: String?, adapterClassName: String?): String {
    val identity = "$adSourceName $adapterClassName".lowercase()
    return when {
        "facebook" in identity || "meta" in identity || "audience" in identity -> "facebook"
        "applovin" in identity -> "applovin"
        "vungle" in identity || "liftoff" in identity -> "vungle"
        "pangle" in identity || "bytedance" in identity -> "pangle"
        "mintegral" in identity || "mbridge" in identity -> "mintegral"
        "inmobi" in identity -> "inmobi"
        "ironsource" in identity || "levelplay" in identity -> "ironsource"
        "admob" in identity || "google" in identity -> "admob"
        else -> adSourceName
            ?.trim()
            ?.lowercase()
            ?.replace(Regex("[^a-z0-9]+"), "_")
            ?.trim('_')
            ?.takeIf { it.isNotEmpty() }
            ?: "unknown"
    }
}
