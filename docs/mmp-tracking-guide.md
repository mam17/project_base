# MMP Tracking Guide (Adjust & AppsFlyer)

## Overview

MMPTracker provides unified integration with **Adjust** and **AppsFlyer** for mobile attribution, user acquisition tracking, and **ROAS (Return On Ad Spend) measurement**.

All events are automatically logged to both MMPs for comprehensive analytics and campaign optimization.

## Features

✅ **Unified API** - Single interface for both Adjust & AppsFlyer  
✅ **Revenue Tracking** - Purchases, subscriptions, ad revenue  
✅ **User Events** - Tutorial completion, level achieved  
✅ **ROAS Measurement** - Track ad spend ROI accurately  
✅ **Custom Events** - Track any custom event  
✅ **Auto-Initialize** - Starts on app launch  
✅ **Error Handling** - Safe error logging  

## Setup

### 1. Get AppsFlyer App ID

1. Go to [AppsFlyer Dashboard](https://www.appsflyer.com)
2. Get your **App ID** from Settings > App Settings
3. Update in `MyApplication.kt`:

```kotlin
val appsFlyerId = "YOUR_APPSFLYER_APP_ID"  // Replace with your ID
MMPTracker.initialize(this, appsFlyerId)
```

### 2. Configure Adjust

Configure in `AndroidManifest.xml` or via your Adjust Dashboard:

```xml
<!-- In AndroidManifest.xml -->
<meta-data
    android:name="com.adjust.sdk.AdjustConfig"
    android:value="adjust_app_token" />
```

Get your Adjust App Token from [Adjust Dashboard](https://app.adjust.com)

### 3. Configure Event Tokens in Adjust

In `MMPTracker.kt`, update the event tokens with your Adjust Dashboard tokens:

```kotlin
object AdjustEvents {
    const val PURCHASE = "YOUR_PURCHASE_TOKEN"              // Get from Adjust
    const val SUBSCRIPTION = "YOUR_SUBSCRIPTION_TOKEN"      // Get from Adjust
    const val AD_IMPRESSION = "YOUR_AD_IMPRESSION_TOKEN"    // Get from Adjust
    const val APP_OPEN = "YOUR_APP_OPEN_TOKEN"              // Get from Adjust
    // ... other events
}
```

## Quick Start

### Track Purchase (for ROAS)

```kotlin
// Simple way
trackMmpPurchase(
    revenue = 9.99,
    productId = "premium_pack"
)

// Or with transaction ID
MMPTracker.trackPurchase(
    revenue = 9.99,
    currency = "USD",
    productId = "premium_pack",
    transactionId = "tx_12345"
)
```

### Track Subscription

```kotlin
trackMmpSubscription(
    revenue = 4.99,
    subscriptionId = "monthly_premium",
    period = "monthly"
)
```

### Track Ad Revenue (CRITICAL FOR ROAS)

When ads generate revenue, track it immediately:

```kotlin
trackMmpAdRevenue(
    revenue = 0.50,
    source = "interstitial",
    adNetwork = "admob"  // or "facebook", "tiktok", etc.
)
```

### Track User Events

```kotlin
// Tutorial completed
trackMmpTutorialComplete()

// Level achieved
trackMmpLevelAchieved(level = 5)

// Custom event
trackMmpCustomEvent(
    eventName = "BattlePass",
    eventValue = 7.99,  // Optional
    currency = "USD"
)
```

## Integration Points

### In BaseActivity (Ad Events)

```kotlin
class MainActivity : BaseActivity<ActivityMainBinding>() {
    
    // Track when ad generates revenue
    fun onAdRevenue(amount: Double) {
        trackMmpAdRevenue(
            revenue = amount,
            source = "rewarded",
            adNetwork = "admob"
        )
    }
}
```

### In Purchase Handler

```kotlin
class PurchaseManager {
    
    fun processPurchase(productId: String, price: Double) {
        // Process payment...
        
        // Track for ROAS
        trackMmpPurchase(
            revenue = price,
            productId = productId,
            transactionId = generateTransactionId()
        )
    }
}
```

### In Game/Progression Events

```kotlin
class GameManager {
    
    fun onLevelComplete(level: Int) {
        // Update game state...
        
        // Track progression for cohort analysis
        trackMmpLevelAchieved(level)
    }
}
```

## ROAS Calculation

**ROAS = Revenue / Ad Spend**

To measure ROAS accurately:

1. **Track ALL Revenue**:
   - In-app purchases → `trackMmpPurchase()`
   - Subscriptions → `trackMmpSubscription()`
   - Ad revenue → `trackMmpAdRevenue()`

2. **MMPs Calculate Ad Spend**:
   - Adjust/AppsFlyer track cost from ad networks
   - Automatically calculate ROAS

3. **View in Dashboard**:
   - Adjust Dashboard → Analytics → ROAS Reports
   - AppsFlyer Dashboard → Analytics → Cost Analysis

## Event Types

| Event | Purpose | ROAS Impact | Required |
|-------|---------|-------------|----------|
| **Purchase** | In-app purchase | ✅ Yes (Revenue) | Yes |
| **Subscribe** | Subscription | ✅ Yes (Revenue) | Yes |
| **AdRevenue** | Ad impression revenue | ✅ Yes (Revenue) | Yes |
| **TutorialComplete** | User progression | ❌ No (Cohort) | No |
| **LevelAchieved** | Game progression | ❌ No (Engagement) | No |
| **CustomEvent** | Any custom | Maybe | Optional |

## Custom Data

Add custom parameters to track source, medium, campaign:

```kotlin
trackMmpPurchase(
    revenue = 9.99,
    productId = "premium",
    customData = mapOf(
        "source" to "organic",
        "campaign" to "summer_sale",
        "ab_test" to "variant_b"
    )
)
```

## Logging & Debugging

Check logcat for MMP events:

```bash
adb logcat | grep MMPTracker
```

Expected output:
```
D/MMPTracker: Purchase tracked: $9.99 USD
D/MMPTracker: AppsFlyer event tracked: af_purchase
D/MMPTracker: Adjust event tracked: abc123
```

## Best Practices

1. **Track Immediately** - Log events as they happen, not batch
2. **Revenue in USD** - Convert all revenue to USD for consistency
3. **Unique Transaction IDs** - Prevent duplicate tracking
4. **Include Metadata** - Add campaign, source, version info
5. **Test Before Launch** - Verify events in test mode dashboards
6. **Monitor ROAS** - Check dashboards weekly for campaign performance

## Troubleshooting

### Events not appearing in Adjust Dashboard

- ✅ Verify Adjust App Token is correct
- ✅ Check event tokens in `AdjustEvents` object
- ✅ Ensure `MMPTracker.initialize()` was called

### Events not appearing in AppsFlyer Dashboard

- ✅ Verify AppsFlyer App ID is correct
- ✅ Check that app has internet permission
- ✅ Review AppsFlyer logcat output for errors

### ROAS showing 0 or incorrect

- ✅ Verify `trackMmpPurchase()` is called for purchases
- ✅ Verify `trackMmpAdRevenue()` is called for ad revenue
- ✅ Check that revenue amounts are > 0
- ✅ Ensure correct currency codes

## Files

- **MMPTracker.kt** - Core MMP tracking utility (singleton)
- **MMPTrackerExtensions.kt** - Convenient extension functions
- **MyApplication.kt** - Initialize on app launch

## Example Full Integration

```kotlin
class GameMonetizationFlow : BaseActivity<ActivityGameBinding>() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Everything initialized automatically
    }
    
    fun onUserPurchasesPremium() {
        val price = 9.99
        val productId = "premium_yearly"
        
        // Track for ROAS
        trackMmpPurchase(price, productId)
        
        // Also track to revenue tracker (Facebook, TikTok)
        trackPurchaseRevenue(price, productId, 1)
    }
    
    fun onAdRevenueEarned(amount: Double) {
        // Track for ROAS calculation
        trackMmpAdRevenue(amount, "rewarded", "admob")
        
        // Also track to revenue tracker
        trackAdRevenue(amount, "USD", "rewarded")
    }
    
    fun onLevelComplete(level: Int) {
        // Track user engagement/cohort analysis
        trackMmpLevelAchieved(level)
    }
}
```

## References

- [Adjust Documentation](https://help.adjust.com/en/)
- [AppsFlyer Documentation](https://support.appsflyer.com/)
- [Adjust ROAS Guide](https://help.adjust.com/en/article/roas-reports)
- [AppsFlyer Cost Analysis](https://support.appsflyer.com/hc/en-us/articles/207932386)
