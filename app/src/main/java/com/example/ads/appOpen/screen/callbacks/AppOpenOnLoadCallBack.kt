package com.example.ads.appOpen.screen.callbacks


interface AppOpenOnLoadCallBack {
    fun onResponse(successfullyLoaded: Boolean, errorMessage: String? = null)
}