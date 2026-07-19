package com.example.myapplication.ui.dialog

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import com.example.myapplication.base.dialog.BaseDialog
import com.example.myapplication.databinding.DialogLoadingAdsBinding

class DialogLoadingAds(context: Context) :
    BaseDialog<DialogLoadingAdsBinding>(context, DialogLoadingAdsBinding::inflate) {

    override fun initView() {
        super.initView()
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setGravity(Gravity.CENTER)
        }
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}
