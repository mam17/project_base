package com.libads.core.callback

/**
 * Kết quả trả về khi load một quảng cáo.
 * Dùng sealed class thay vì callback rời rạc để bắt buộc xử lý đủ trường hợp (when exhaustive).
 */
sealed class AdResult {
    data class Success(val adUnitId: String) : AdResult()
    data class Failure(val adUnitId: String, val errorCode: Int, val message: String) : AdResult()
    data class TimedOut(val adUnitId: String) : AdResult()
}

/**
 * Callback khi load quảng cáo xong.
 */
fun interface AdLoadCallback {
    fun onResult(result: AdResult)
}

/**
 * Callback vòng đời khi hiển thị quảng cáo (interstitial, rewarded...).
 * Tách riêng khỏi load callback vì show có nhiều event hơn.
 */
interface AdShowCallback {
    fun onAdShown() {}
    fun onAdClicked() {}
    fun onAdDismissed() {}
    fun onAdFailedToShow(errorCode: Int, message: String) {}
    /** Chỉ áp dụng cho REWARDED / REWARDED_INTERSTITIAL */
    fun onUserEarnedReward(amount: Int, type: String) {}
}
