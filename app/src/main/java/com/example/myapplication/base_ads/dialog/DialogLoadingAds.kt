package com.example.myapplication.base_ads.dialog

import android.content.Context
import android.widget.RelativeLayout
import com.example.myapplication.base.dialog.BaseDialog
import com.example.myapplication.databinding.LayoutLoadingAdsBinding

class DialogLoadingAds(context: Context) :
    BaseDialog<LayoutLoadingAdsBinding>(context, LayoutLoadingAdsBinding::inflate) {
    override fun initView() {
        super.initView()
        setCancelable(false)
        window?.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
    }
}