package com.example.myapplication.ui.splash

import android.annotation.SuppressLint
import android.os.Bundle
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivitySplashBinding
import com.example.myapplication.ui.language.LanguageActivity
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.utils.Constant
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    override fun initView() {
        navigateNext()
    }

    override fun initData() {}

    private fun navigateNext() {
        diComponent.appOpenAdManager.isSplash = false

        if (spManager.isCompletedOnboarding) {
            startActivityNewTask(MainActivity::class.java)
        } else {
            val bundle = Bundle().apply { putBoolean(Constant.KEY_FROM_SPLASH, true) }
            startNextActivity(LanguageActivity::class.java, bundle = bundle, isFinish = true)
        }
    }
}