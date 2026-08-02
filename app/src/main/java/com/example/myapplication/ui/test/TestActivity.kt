package com.example.myapplication.ui.test

import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityTestBinding

class TestActivity : BaseActivity<ActivityTestBinding>(ActivityTestBinding::inflate) {


    override fun initView() {
        binding.tvShowInterWith2f.setOnClickListener {

        }

        binding.tvShowRewardWith2f.setOnClickListener {

        }
    }

    override fun initData() {

    }
}
