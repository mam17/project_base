package com.example.myapplication.ads

class AdCallback {
    internal var onDismissed: (() -> Unit)? = null
    internal var onFailed: ((String) -> Unit)? = null
    internal var onRewardEarned: ((amount: Int, type: String) -> Unit)? = null
    internal var action: (() -> Unit)? = null

    fun onDismissed(block: () -> Unit) {
        onDismissed = block
    }

    fun onFailed(block: (message: String) -> Unit) {
        onFailed = block
    }

    fun onRewardEarned(block: (amount: Int, type: String) -> Unit) {
        onRewardEarned = block
    }

    fun action(block: () -> Unit) {
        action = block
    }
}
