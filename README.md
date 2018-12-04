# frictionscreen
For a content based android app where it require to show some kind of friction screen when it meets certain conditions then this small library can be very handy. For example, a free news app would like to show a subscription screen to nudge users to buy subscription after reading 10 articles in last 7 days then you can just set this two peice of information in the config and the library will keep monitor user's reading activity and trigger when the subscription screen need to be shown.


## Configuration
To configure the FrictionScreenManager, preferebly only once in your application class: 

```
val config = FrictionScreenManager.Config(xContentRead, minDays, isEnables, requireConnectivity)
frictionScreenManager = FrictionScreenManager(activity, config)
```
**xContentRead**: number of minimum contents/articles users need to consume (e.g. 10)
**minDays**: minimum days over which users need to consume the content (e.g. 7)
**isEnables**: set to `false` if need to be (e.g. if you want to not trigger it until some other conditions are meet)
**requireConnectivity**: set to `true` if app require connectivity when it triggers

When user consume a piece of content call this method, preferebly on onCreate of activity or similar for fragment:
```
frictionScreenManager.recordArticleRead(uniqueContentId)
```
**uniqueContentId**: set unique id (e.g. `article- + System.currentTimeMillis()`)

To show the friction screen (subs/premium purchase screen) check and show the screen, preferebly do it in `onResume`:
```
override fun onResume() {
    super.onResume()
    if(frictionScreenManager.shouldShowSubsScreen()) {
        showPremiumDialog()
        frictionScreenManager.setSubscriptionScreenDisplayed()
    }
}
```

Thats it, user will not see the screen again until `minDays` period is over.
