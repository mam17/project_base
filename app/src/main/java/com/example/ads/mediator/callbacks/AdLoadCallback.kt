package com.example.ads.mediator.callbacks

/**
 * Unified callback for ad loading across all ad types.
 * Simplifies callback handling from multiple callback interfaces.
 */
interface AdLoadCallback {
    fun onSuccess()
    fun onFailure(errorMessage: String? = null)
}
