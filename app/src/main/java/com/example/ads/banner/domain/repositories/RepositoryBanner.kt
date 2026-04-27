package com.example.ads.banner.domain.repositories

import com.google.android.gms.ads.AdView
import com.example.ads.banner.data.entities.ItemBannerAd
import com.example.ads.banner.presentation.enums.BannerAdType


interface RepositoryBanner {
    fun fetchBannerAd(adView: AdView, adKey: String, adId: String, bannerAdType: BannerAdType, callback: (ItemBannerAd?) -> Unit)
    fun destroyBanner(adKey: String): Boolean
}