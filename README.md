# Friction Screen
A library for content-based apps that require a friction mechanism. Simplifies the process of managing conditions for displaying a friction screen.

For example, a free news app might like to show a subscription screen to nudge users into buying subscription after reading 10 articles in the last 7 days.


## Configuration
Configure the `FrictionScreenManager`, preferably only once in your application:

```kotlin
val config = FrictionScreenManager.Config(articleReadThreshold, minDaysThreshold, isEnabled, showFrictionScreenWhileOnline)
frictionScreenManager = FrictionScreenManager(context, config)
```

* `articleReadThreshold`: number of minimum content/articles users need to consume (e.g. 10)
* `minDaysThreshold`: minimum days over which users need to consume the content/articles (e.g. 7)
* `isEnabled`: set to `false` if you want to not trigger it until conditions are met
* `showFrictionScreenWhileOnline`: set to `true` if app requires connectivity when it triggers


When user consume a piece of content call this method, preferably in the `onCreate` method of an `Activity` or a `Fragment`:

```kotlin
frictionScreenManager.recordArticleRead(articleId)
```
* `articleId`: a unique id for an article (e.g. `article- + System.currentTimeMillis()`)


To show the friction screen (subs/premium purchase screen) check and show the screen, preferebly do it in `onResume`:
```kotlin
override fun onResume() {
    super.onResume()
    if(frictionScreenManager.shouldShowSubsScreen()) {
        showPremiumDialog()
        frictionScreenManager.setSubscriptionScreenDisplayed()
    }
}
```

Thats it, a user will not see the screen again until `minDaysThreshold` period is over.
