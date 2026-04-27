package com.example.myapplication

import android.app.Application
import com.example.ads.utilities.FirebaseTracker
import com.example.ads.utilities.MMPTracker
import com.example.ads.utilities.RevenueTracker
import com.example.myapplication.di.KoinModules
import com.example.myapplication.utils.SpManager
import com.example.myapplication.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import javax.inject.Singleton

@HiltAndroidApp
@Singleton
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
        initializeTracking()
    }

    private fun initializeTracking() {
        // Initialize Firebase Analytics tracking
        FirebaseTracker.initialize(this)

        // Initialize revenue tracking (Facebook)
        RevenueTracker.initialize(this)

        // Initialize MMP tracking (AppsFlyer)
        // TODO: Replace with your AppsFlyer App ID from https://www.appsflyer.com
        val appsFlyerId = "YOUR_APPSFLYER_APP_ID"
        MMPTracker.initialize(this, appsFlyerId)
    }
    private fun initKoin() {
        startKoin {
            androidContext(this@MyApplication)
            modules(KoinModules().modulesList)
        }
    }
}
