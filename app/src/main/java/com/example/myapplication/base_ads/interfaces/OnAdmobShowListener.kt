package com.example.myapplication.base_ads.interfaces

interface OnAdmobShowListener {
    fun onShow()
    fun onError(e: String)
    fun onClosed() {}
}