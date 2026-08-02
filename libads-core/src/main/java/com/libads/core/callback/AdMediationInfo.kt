package com.libads.core.callback

data class AdMediationInfo(
    val networkName: String,
    val adapterClassName: String? = null,
    val adSourceName: String? = null,
    val adSourceId: String? = null,
    val adSourceInstanceName: String? = null,
    val adSourceInstanceId: String? = null,
    val latencyMillis: Long? = null
)
