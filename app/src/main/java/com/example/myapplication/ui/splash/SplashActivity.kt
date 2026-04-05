package com.example.myapplication.ui.splash

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivitySplashBinding
import com.example.myapplication.ui.language.LanguageActivity
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.utils.SpManager
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {
    override fun initView() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (spManager.isCompletedOnboarding) {
                startActivityNewTask(MainActivity::class.java)
            } else {
                startNextActivity(LanguageActivity::class.java, isFinish = true)
            }
        }, 2000L)
    }

    override fun initData() {

    }

}