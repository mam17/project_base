package com.example.myapplication

import android.app.Application
import com.example.ads.utilities.RevenueTracker
import com.example.myapplication.di.KoinModules
import com.example.myapplication.utils.SpManager
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
        initializeRevenueTracking()
    }

    private fun initializeRevenueTracking() {
        RevenueTracker.initialize(this)
    }
    private fun initKoin() {
        startKoin {
            androidContext(this@MyApplication)
            modules(KoinModules().modulesList)
        }
    }
}
