# Firebase Tracking Guide

## Overview

FirebaseTracker provides a unified, easy-to-use interface for logging custom events to Firebase Analytics. Log events from Activities, Fragments, or anywhere in your code with just one line.

## Features

✅ **Simple API** - One-line event logging  
✅ **Activity/Fragment Extensions** - Direct `.logAnalyticsEvent()` calls  
✅ **Custom Parameters** - Add metadata to events  
✅ **User Tracking** - Scope events to user IDs  
✅ **Pre-built Event Types** - Screen view, button click, feature access, errors  
✅ **Error Handling** - Safe error logging with fallbacks  
✅ **Auto-initialize** - Starts on app launch  

## Quick Start

### Basic Event Logging (Activity)

```kotlin
class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun initView() {
        binding.btnSubmit.setOnClickListener {
            // Log button click
            logAnalyticsEvent("submit_button_clicked", "User clicked submit button")
            
            // Handle action
        }
    }
}
```

### Basic Event Logging (Fragment)

```kotlin
class HomeFragment : BaseFragment() {
    override fun initView() {
        binding.btnAction.setOnClickListener {
            // Log from fragment
            logAnalyticsEvent("home_action", "User triggered home action")
        }
    }
}
```

### With Custom Parameters

```kotlin
logAnalyticsEvent(
    eventName = "purchase_completed",
    message = "User completed purchase",
    params = mapOf(
        "amount" to "9.99",
        "currency" to "USD",
        "product" to "premium_pack"
    )
)
```

### With User ID (For User-Scoped Analytics)

```kotlin
logAnalyticsEventWithUser(
    eventName = "user_login",
    message = "User logged in successfully",
    userId = "user_12345",
    params = mapOf("auth_method" to "google")
)
```

## Pre-built Event Shortcuts

### Screen View
```kotlin
logScreenView("home_screen")
// Logs: screen_view event with screen="home_screen"
```

### Button Click
```kotlin
logButtonClick("subscribe_button")
// Logs: button_click event with button="subscribe_button"
```

### Feature Access
```kotlin
logFeatureAccess("ai_feature")
// Logs: feature_access event with feature="ai_feature"
```

### Error Logging
```kotlin
logError("api_timeout", "API request timed out after 30s")
// Logs: app_error event with error="api_timeout"
```

## Complete Integration Examples

### In Activity

```kotlin
class ProfileActivity : BaseActivity<ActivityProfileBinding>() {
    
    override fun initView() {
        logScreenView("profile_screen")
        
        binding.btnEditProfile.setOnClickListener {
            logButtonClick("edit_profile_button")
            showEditProfileDialog()
        }
        
        binding.btnLogout.setOnClickListener {
            logAnalyticsEvent(
                "user_logout",
                "User initiated logout",
                mapOf("timestamp" to System.currentTimeMillis().toString())
            )
            logout()
        }
    }
    
    private fun logout() {
        FirebaseTracker.clearUserId()
        // Handle logout...
    }
}
```

### In Fragment

```kotlin
class GameFragment : BaseFragment() {
    
    override fun initData() {
        logScreenView("game_screen")
    }
    
    fun onLevelComplete(level: Int) {
        logAnalyticsEvent(
            "level_completed",
            "User completed level $level",
            params = mapOf(
                "level" to level.toString(),
                "difficulty" to "hard",
                "time_seconds" to "120"
            )
        )
    }
    
    fun onPurchaseItem(itemName: String, price: Double) {
        logAnalyticsEvent(
            "in_game_purchase",
            "User purchased $itemName",
            params = mapOf(
                "item" to itemName,
                "price" to price.toString(),
                "currency" to "USD"
            )
        )
    }
}
```

### Error Tracking

```kotlin
try {
    val data = apiClient.fetchData()
    logAnalyticsEvent("api_success", "Data fetched successfully")
} catch (e: TimeoutException) {
    logError("api_timeout", "Request timeout: ${e.message}")
} catch (e: Exception) {
    logError("api_error", "Error: ${e.message}")
}
```

## Available Extension Functions

### Activity Extensions
- `logAnalyticsEvent(eventName, message, params)`
- `logAnalyticsEventWithUser(eventName, message, userId, params)`
- `logScreenView(screenName)`
- `logButtonClick(buttonName)`
- `logFeatureAccess(featureName)`
- `logError(errorName, errorMessage)`

### Fragment Extensions
- `logAnalyticsEvent(eventName, message, params)`
- `logAnalyticsEventWithUser(eventName, message, userId, params)`
- `logScreenView(screenName)`
- `logButtonClick(buttonName)`
- `logFeatureAccess(featureName)`
- `logError(errorName, errorMessage)`

### Context Extensions
- `logAnalyticsEvent(eventName, message, params)`

### Direct Tracker Usage
```kotlin
// If extensions not available, use FirebaseTracker directly
FirebaseTracker.logEvent("custom_event", "Some message", mapOf("key" to "value"))
FirebaseTracker.logEventWithUser("login", "User logged in", userId)
FirebaseTracker.clearUserId()
```

## Event Naming Conventions

Firebase has naming requirements:
- **Max 40 characters** for event name
- **Alphanumeric & underscores only** (spaces/special chars converted to underscore)
- **Reserved event names** (avoid): `purchase`, `refund`, `sign_up`, `login`, etc.

### Recommended Event Naming

| Event | Example |
|-------|---------|
| Screen navigation | `screen_view`, `screen_home`, `screen_profile` |
| User actions | `button_click`, `menu_open`, `search_performed` |
| Feature usage | `feature_accessed`, `feature_used`, `ai_feature_enabled` |
| Game events | `level_completed`, `level_failed`, `reward_earned` |
| Commerce | `product_viewed`, `cart_updated`, `checkout_started` |
| Errors | `app_error`, `api_error`, `db_error` |

## Parameter Guidelines

**Firebase limits:**
- **Parameter name**: Max 24 characters
- **Parameter value**: Max 100 characters
- **Total params per event**: Max 25 parameters

**Example:**
```kotlin
logAnalyticsEvent(
    "game_action",
    "User completed tutorial",
    mapOf(
        "tutorial_type" to "basic",
        "time_seconds" to "45",
        "completed" to "true"
        // Keep keys short, values concise
    )
)
```

## Best Practices

1. **Log Immediately** - Log events as they happen, not batch
2. **Use Meaningful Names** - Event names should describe what happened
3. **Add Context** - Use parameters to provide additional context
4. **Track User Journeys** - Log screen views and key actions to understand user flow
5. **Monitor Errors** - Always log errors for debugging and monitoring
6. **Avoid PII** - Never log personal information (passwords, emails, credit cards)
7. **Set User ID Early** - Call `logAnalyticsEventWithUser()` after login for user-scoped analytics
8. **Clear User ID** - Call `FirebaseTracker.clearUserId()` on logout

## Firebase Console Monitoring

### View Events
1. Go to Firebase Console → Your Project → Analytics
2. Click "Events" tab
3. View all custom events logged
4. Filter by event name, time range, user properties

### Create Audiences
Use logged events to create user audiences:
1. Analytics → Audiences
2. Define audience by events (e.g., "Users who accessed AI feature")
3. Use for targeted campaigns

### Real-time Debugging
1. Analytics → Real-time
2. See events logged in real-time as you test the app
3. Great for verifying event logging during development

## Troubleshooting

### Events not appearing in Firebase Console

- ✅ Verify `FirebaseTracker.initialize()` was called in MyApplication.kt
- ✅ Check that app has internet permission
- ✅ Verify event names use alphanumeric + underscore only
- ✅ Check Firebase project ID in `google-services.json`
- ✅ Events may take 5-10 minutes to appear in console

### Parameter values truncated

- ✅ Parameter values limited to 100 characters (auto-truncated)
- ✅ Keep values concise
- ✅ Use parameter names max 24 characters

### Too many parameters

- ✅ Firebase limits to 25 parameters per event
- ✅ Remove unnecessary parameters
- ✅ Combine related data into fewer parameters

## Files

- **FirebaseTracker.kt** - Core Firebase tracking utility
- **FirebaseTrackerExtensions.kt** - Quick extension functions for Activities/Fragments
- **MyApplication.kt** - Initialization on app launch

## Example Full Integration

```kotlin
class MainActivity : BaseActivity<ActivityMainBinding>() {
    
    private var currentUserId: String? = null
    
    override fun initView() {
        logScreenView("main_screen")
        
        binding.btnLogin.setOnClickListener {
            performLogin()
        }
        
        binding.btnFeature.setOnClickListener {
            logButtonClick("feature_button")
            accessFeature()
        }
    }
    
    private fun performLogin() {
        try {
            val userId = authManager.login(email, password)
            currentUserId = userId
            
            logAnalyticsEventWithUser(
                "user_login",
                "User logged in successfully",
                userId,
                mapOf("method" to "email")
            )
        } catch (e: Exception) {
            logError("login_failed", e.message ?: "Unknown error")
        }
    }
    
    private fun accessFeature() {
        logFeatureAccess("premium_ai_feature")
        
        logAnalyticsEvent(
            "feature_used",
            "User used AI feature",
            mapOf(
                "feature_type" to "text_generation",
                "duration_seconds" to "30"
            )
        )
    }
}
```

## References

- [Firebase Analytics Documentation](https://firebase.google.com/docs/analytics)
- [Recommended Events](https://support.google.com/analytics/answer/9322688)
- [Firebase Console](https://console.firebase.google.com)
