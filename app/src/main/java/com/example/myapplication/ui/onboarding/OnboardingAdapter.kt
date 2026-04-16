package com.example.myapplication.ui.onboarding

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import com.example.ads.natives.presentation.enums.NativeAdKey
import com.example.myapplication.base.adapter.BaseMultiListAdapter
import com.example.myapplication.databinding.LayoutNativeFsTimeCountBinding
import com.example.myapplication.databinding.ItemOnboardingBinding
import com.example.myapplication.domain.layer.OnboardingModel
import com.google.android.gms.ads.nativead.NativeAd

class OnboardingAdapter : BaseMultiListAdapter<OnboardingAdapter.Item>(DIFF_CALLBACK) {

    sealed class Item {
        data class Content(val model: OnboardingModel) : Item()
        data class NativeFs(val key: NativeAdKey, val nativeAd: NativeAd? = null) : Item()
    }

    var onCloseClick: (() -> Unit)? = null

    private val countdownTimers = mutableMapOf<NativeAdKey, CountDownTimer>()

    companion object {
        private const val TYPE_CONTENT = 0
        private const val TYPE_NATIVE_FS = 1

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item) = when (oldItem) {
                is Item.Content if newItem is Item.Content -> oldItem.model == newItem.model
                is Item.NativeFs if newItem is Item.NativeFs -> oldItem.key == newItem.key
                else -> false
            }

            override fun areContentsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem
        }
    }

    override fun getViewType(item: Item, position: Int) = when (item) {
        is Item.Content -> TYPE_CONTENT
        is Item.NativeFs -> TYPE_NATIVE_FS
    }

    override fun getBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBinding {
        return when (viewType) {
            TYPE_CONTENT -> ItemOnboardingBinding.inflate(inflater, parent, false)
            else -> LayoutNativeFsTimeCountBinding.inflate(inflater, parent, false)
        }
    }

    override fun bind(binding: ViewBinding, item: Item, position: Int) {
        when (binding) {
            is ItemOnboardingBinding if item is Item.Content -> {
                binding.imgBoarding.setImageResource(item.model.resImage)
                binding.tvTitle.setText(item.model.resTitle)
                binding.tvOnboarding.setText(item.model.resDescription)
            }

            is LayoutNativeFsTimeCountBinding if item is Item.NativeFs -> {
                item.nativeAd?.let { binding.adNativeFullScreen.setNativeAd(it) }
                startCountdown(binding, item.key)
            }
        }
    }

    private fun startCountdown(binding: LayoutNativeFsTimeCountBinding, key: NativeAdKey) {
        countdownTimers[key]?.cancel()

        binding.tvTimeCount.visibility = View.VISIBLE
        binding.btnCloseOnb.visibility = View.GONE
        binding.tvTimeCount.text = "3"

        countdownTimers[key] = object : CountDownTimer(3000L, 1000L) {
            var count = 3

            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimeCount.text = count.toString()
                count--
            }

            override fun onFinish() {
                countdownTimers.remove(key)
                binding.tvTimeCount.visibility = View.GONE
                binding.btnCloseOnb.visibility = View.VISIBLE
                binding.btnCloseOnb.setOnClickListener { onCloseClick?.invoke() }
            }
        }.start()
    }

    fun setNativeAd(key: NativeAdKey, nativeAd: NativeAd) {
        val updated = currentList.map { item ->
            if (item is Item.NativeFs && item.key == key) item.copy(nativeAd = nativeAd) else item
        }
        submitList(updated)
    }

    fun isContentPage(position: Int) = getItem(position) is Item.Content
}
