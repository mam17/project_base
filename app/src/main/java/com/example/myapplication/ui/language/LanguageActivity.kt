package com.example.myapplication.ui.language

import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityLanguageBinding
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.ui.onboarding.OnboardingActivity
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.SystemUtil
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.ViewEx.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate) {
    private val viewModel: LanguageViewModel by viewModels()
    private var mLanguageAdapter = LanguageAdapter()
    private var fromSplash = false

    override fun initView() {
        fromSplash = intent.getBooleanExtra(Constant.KEY_FROM_SPLASH, false)
        Log.i("TAG_LANGUAGE", "initUI: fromSplash $fromSplash")
        initUI()
        initListener()
    }

    private fun initListener() {
        binding.apply {
            toolBarLanguage.btnSelect.setOnClickListener {
                mLanguageAdapter.getSelectedModel()?.let { model ->
                    SystemUtil.saveLanguage(this@LanguageActivity, model)
                    if (spManager.isCompletedOnboarding) {
                        startActivityNewTask(MainActivity::class.java)
                    } else {
                        startNextActivity(OnboardingActivity::class.java, isFinish = true)
                    }
                }
            }
        }
    }

    private fun initUI() {
        binding.apply {
            toolBarLanguage.btnBack.isVisible = !fromSplash
            toolBarLanguage.tvTitle.text = getString(R.string.txt_language)
            toolBarLanguage.btnBack.setOnClickListener { onBack() }
            toolBarLanguage.btnSelect.visible()
            toolBarLanguage.btnAction.gone()
            rclLanguage.adapter = mLanguageAdapter
            mLanguageAdapter.setOnItemClick { _, position ->
                mLanguageAdapter.selectItem(position)
            }
        }
    }

    override fun initData() {
        viewModel.loadListLanguage()
    }

    override fun initObserver() {
        viewModel.listLanguage.observe(this) { list ->
            val currentCode = SystemUtil.getLanguage(this)
            list.forEach { model ->
                model.selected = model.languageCode == currentCode
            }
            mLanguageAdapter.setData(list)
        }
    }


}