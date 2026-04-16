package com.example.ads.natives.presentation.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.nativead.NativeAd
import com.example.ads.natives.domain.useCases.UseCaseNative
import com.example.ads.natives.presentation.enums.NativeAdKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModelNative(private val useCaseNative: UseCaseNative) : ViewModel() {

    private val _adViewLiveData = MutableLiveData<Pair<NativeAdKey, NativeAd>>()
    val adViewLiveData: LiveData<Pair<NativeAdKey, NativeAd>> get() = _adViewLiveData

    private val _loadFailedLiveData = MutableLiveData<NativeAdKey>()
    val loadFailedLiveData: LiveData<NativeAdKey> get() = _loadFailedLiveData

    fun loadNativeAd(nativeAdKey: NativeAdKey) = viewModelScope.launch {
        useCaseNative.loadNativeAd(nativeAdKey) { itemNativeAd ->
            itemNativeAd?.let {
                _adViewLiveData.value = Pair(nativeAdKey, it.nativeAd)
            } ?: run {
                _loadFailedLiveData.value = nativeAdKey
            }
        }
    }

    fun destroyNative(nativeAdKey: NativeAdKey) = viewModelScope.launch(Dispatchers.Default) {
        useCaseNative.destroyNative(nativeAdKey)
    }
}
