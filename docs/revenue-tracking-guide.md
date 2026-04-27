# Revenue Tracking Guide (Facebook & TikTok)

## Overview

RevenueTracker utility provides simple integration for tracking revenue events to Facebook and TikTok SDKs. Automatically logs to both platforms for better ad optimization and monetization tracking.

## Features

- ✅ Auto-initialize on app launch
- ✅ Track ad impressions/revenue
- ✅ Track in-app purchases
- ✅ Track subscriptions
- ✅ Custom event tracking
- ✅ Support for multiple currencies
- ✅ Error handling & logging

## Quick Start

### 1. Track Ad Revenue

When ads generate revenue:

```kotlin
// Using extension function (recommended)
trackAdRevenue(
    revenue = 0.50,  // Revenue amount in USD
    source = "interstitial",  // e.g., "interstitial", "rewarded", "banner"
    currency = "USD"  // Default: USD
)

// Or using direct method
RevenueTracker.trackAdImpression(0.50, "USD", "interstitial")
```

### 2. Track In-App Purchase

When user makes a purchase:

```kotlin
trackPurchaseRevenue(
    revenue = 9.99,
    productId = "premium_pack",
    quantity = 1,
    currency = "USD"
)

// Or
RevenueTracker.trackPurchase(
    revenue = 9.99,
    currency = "USD",
    productId = "premium_pack",
    quantity = 1
)
```

### 3. Track Subscription

For subscription revenue:

```kotlin
trackSubscriptionRevenue(
    revenue = 4.99,
    subscriptionId = "monthly_premium",
    period = "monthly",  // e.g., "monthly", "yearly"
    currency = "USD"
)

// Or
RevenueTracker.trackSubscription(
    revenue = 4.99,
    currency = "USD",
    subscriptionId = "monthly_premium",
    period = "monthly"
)
```

### 4. Custom Event Tracking

For other revenue events:

```kotlin
trackCustomRevenue(
    eventName = "BattlePass",
    revenue = 7.99,
    currency = "USD",
    customData = mapOf(
        "season" to "5",
        "type" to "battle_pass"
    )
)

// Or
RevenueTracker.trackCustomEvent(
    eventName = "BattlePass",
    revenue = 7.99,
    currency = "USD",
    customData = mapOf(
        "season" to "5",
        "type" to "battle_pass"
    )
)
```

## Integration Points

### In BaseActivity (for Ad Revenue)

```kotlin
// When showing rewarded ads
showAd(adKey,
    onRewarded = {
        // User earned reward from ads
        trackAdRevenue(
            revenue = calculateAdRevenue(),
            source = "rewarded",
            currency = "USD"
        )
    }
)
```

### In Purchase Handler

```kotlin
// When purchase completes
fun onPurchaseSuccess(orderId: String, productId: String, amount: Double) {
    trackPurchaseRevenue(
        revenue = amount,
        productId = productId,
        quantity = 1,
        currency = "USD"
    )
    // Rest of purchase logic...
}
```

### In Subscription Manager

```kotlin
// When subscription activated
fun activateSubscription(subId: String, price: Double) {
    trackSubscriptionRevenue(
        revenue = price,
        subscriptionId = subId,
        period = "monthly",
        currency = "USD"
    )
    // Rest of subscription logic...
}
```

## Supported Currencies

All standard ISO 4217 currency codes are supported:
- USD (default)
- EUR
- GBP
- JPY
- AUD
- CAD
- INR
- etc.

## Event Types

### Predefined Events

| Event | Description | Revenue Tracking |
|-------|-------------|------------------|
| **AdImpression** | Ad was shown and earned revenue | ✅ Yes |
| **Purchase** | In-app purchase completed | ✅ Yes |
| **Subscribe** | Subscription activated | ✅ Yes |
| **Custom** | Any custom event | ✅ Yes |

## Logging

Revenue tracking includes comprehensive logging. Check logcat for:

```
D/RevenueTracker: Tracked to Facebook: purchase - $9.99 USD
D/RevenueTracker: Tracked to TikTok: purchase - $9.99 USD
```

## Best Practices

1. **Track in correct currency** - Always use the actual transaction currency
2. **Validate amounts** - Amounts must be > 0
3. **Use consistent product IDs** - Same product should have same ID everywhere
4. **Track immediately** - Log revenue as soon as transaction completes
5. **Include metadata** - Add custom data for better analytics (product ID, season, type, etc.)

## Troubleshooting

### No revenue appearing in Facebook Insights

- Verify Facebook SDK is properly initialized
- Check that revenue amounts are valid (> 0)
- Ensure correct App ID in AndroidManifest.xml

### No revenue appearing in TikTok Events Manager

- Verify TikTok SDK is properly initialized  
- Check Business Account has correct app access
- Verify Pixel/Event ID configuration in TikTok Manager

### Debug Logging

Enable detailed logging:

```kotlin
// Check logcat for RevenueTracker and RevenueTracker tags
adb logcat | grep RevenueTracker
```

## Files

- **RevenueTracker.kt** - Core tracking utility (singleton object)
- **RevenueTrackerExtensions.kt** - Convenient extension functions
- **MyApplication.kt** - Initialization on app launch

## Example Implementation

```kotlin
// In your purchase workflow
class PremiumFeatureActivity : BaseActivity<ActivityPremiumFeatureBinding>() {

    fun onBuyPremiumClicked() {
        // Process purchase...
        val price = 9.99
        val productId = "premium_annual"
        
        purchaseManager.purchase(productId) { success ->
            if (success) {
                // Track revenue immediately
                trackPurchaseRevenue(
                    revenue = price,
                    productId = productId,
                    quantity = 1,
                    currency = "USD"
                )
                showToast("Premium activated!")
            }
        }
    }
}
```

## References

- [Facebook App Events Documentation](https://developers.facebook.com/docs/app-events)
- [TikTok Business SDK Documentation](https://business-api.tiktok.com/)
