package com.example.myapplication

import android.app.Application
import com.example.myapplication.utils.SpManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Singleton

@HiltAndroidApp
@Singleton
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
