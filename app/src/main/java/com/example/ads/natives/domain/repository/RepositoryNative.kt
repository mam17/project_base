package com.example.ads.natives.domain.repository

import com.example.ads.natives.data.entities.ItemNativeAd



interface RepositoryNative {
    fun fetchNativeAd(adKey: String, adId: String, callback: (ItemNativeAd?) -> Unit)
    fun destroyNative(adKey: String): Boolean
}