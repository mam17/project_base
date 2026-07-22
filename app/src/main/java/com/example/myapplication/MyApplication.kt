package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.myapplication.ads.AdMobAds
import com.example.myapplication.utils.AppEx.setupAppShortcuts
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.notification.NotificationUtils
import com.libads.core.AdManager
import com.libads.core.provider.admob.AdMobProvider
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Singleton

@HiltAndroidApp
@Singleton
class MyApplication : Application(), Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {
    private var startedActivityCount = 0
    private var currentActivity: FragmentActivity? = null
    private var hasCompletedFirstForeground = false
    private var shouldShowAppOpenOnResume = false
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super<Application>.onCreate()
        mInstance = this
        context = applicationContext
        setupAppShortcuts(this)
        registerActivityLifecycleCallbacks(this)
        AdManager.init(this) {
            registerProvider(AdMobProvider())
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityResumed(p0: Activity) {
        currentActivity = p0 as? FragmentActivity
        if (shouldShowAppOpenOnResume) {
            shouldShowAppOpenOnResume = false
            mainHandler.postDelayed({
                currentActivity?.let { AdMobAds.showAppOpenResume(it) }
            }, APP_OPEN_SHOW_DELAY_MS)
        }
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityStarted(p0: Activity) {
        startedActivityCount++
        currentActivity = p0 as? FragmentActivity
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity.isChangingConfigurations) return

        startedActivityCount = (startedActivityCount - 1).coerceAtLeast(0)
        if (startedActivityCount == 0) {
            val sp = SpManager.get(applicationContext)
            if (!sp.isCompletedOnboarding) {
                NotificationUtils.scheduleOnboardingReminder(applicationContext)
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (!hasCompletedFirstForeground) {
            hasCompletedFirstForeground = true
            return
        }

        shouldShowAppOpenOnResume = true
    }

    companion object {
        private const val TAG = "TAG_MyApplication"
        private const val APP_OPEN_SHOW_DELAY_MS = 300L

        @SuppressLint("StaticFieldLeak")
        var context: Context? = null

        @SuppressLint("StaticFieldLeak")
        private var mInstance: MyApplication? = null
        val instance get() = mInstance
    }
}
