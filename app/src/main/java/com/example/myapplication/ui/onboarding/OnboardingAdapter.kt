package com.example.myapplication.ui.onboarding

import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import com.example.myapplication.ads.Ads
import com.example.myapplication.base.adapter.BaseListAdapter
import com.example.myapplication.databinding.ItemOnboardingBinding
import com.example.myapplication.domain.layer.OnboardingModel
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.ViewEx.visible

class OnboardingAdapter(
    private val activity: FragmentActivity? = null,
    private val onCloseAdClick: ((position: Int) -> Unit)? = null
) : BaseListAdapter<OnboardingModel, ItemOnboardingBinding>(
    OnboardingDiffCallback,
    ItemOnboardingBinding::inflate
) {

    val getItemId: List<OnboardingModel> get() = currentList

    object OnboardingDiffCallback : DiffUtil.ItemCallback<OnboardingModel>() {
        override fun areItemsTheSame(oldItem: OnboardingModel, newItem: OnboardingModel): Boolean {
            return if (oldItem.isNativeAd && newItem.isNativeAd) {
                oldItem.nativePlacement?.name == newItem.nativePlacement?.name
            } else {
                oldItem.resImage == newItem.resImage && oldItem.resTitle == newItem.resTitle
            }
        }

        override fun areContentsTheSame(
            oldItem: OnboardingModel,
            newItem: OnboardingModel
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun bind(binding: ItemOnboardingBinding, item: OnboardingModel, position: Int) {
        binding.apply {
            if (item.isNativeAd && item.nativePlacement != null) {
                ctContOnb.gone()
                frNativeTimeOut.root.visible()
                frNativeTimeOut.nativeFullContainer.visible()
                frNativeTimeOut.nativeFullLoadingContainer.gone()

                // Hide countdown text
                frNativeTimeOut.tvTimeCount.gone()

                // Initially hide close button container & button
                frNativeTimeOut.rlCloseAds.gone()
                frNativeTimeOut.btnCloseOnb.gone()

                // Delay 3 seconds before showing close button
                frNativeTimeOut.root.removeCallbacks(null)
                frNativeTimeOut.root.postDelayed({
                    frNativeTimeOut.rlCloseAds.visible()
                    frNativeTimeOut.btnCloseOnb.visible()
                }, CLOSE_BUTTON_DELAY_MS)

                frNativeTimeOut.btnCloseOnb.setOnClickListener {
                    onCloseAdClick?.invoke(position)
                }

                val hostActivity = activity ?: (root.context as? FragmentActivity)
                if (hostActivity != null) {
                    Ads.showInto(
                        hostActivity,
                        frNativeTimeOut.nativeFullContainer,
                        item.nativePlacement
                    )
                }
            } else {
                frNativeTimeOut.root.removeCallbacks(null)
                ctContOnb.visible()
                frNativeTimeOut.root.gone()
                imgBoarding.setImageResource(item.resImage)
                tvTitle.setText(item.resTitle)
                tvOnboarding.setText(item.resDescription)
            }
        }
    }

    fun setData(data: List<OnboardingModel>) {
        submitList(data)
    }

    companion object {
        private const val CLOSE_BUTTON_DELAY_MS = 3_000L
    }
}