# Changelog

## Beta Updates
* Update secondary background for transparent themes to be more visible.
* Pressing enter when searching will launch the full search page
* Add different backgrounds for news feed articles.
* Add option to get image/video from default camera or gallery app.
* Fix some bug reports.
* Remove error dialog for IAB. It will now depend solely on the google services dialogs.

## v1.4.5
* Create more robust IM notification fetcher with a timeout
* Add hidden debugging options for certain views
* Separate IM and general notification groups
* Add click actions to group notifications. They will launch the message page or the notification page respectively
* Add behaviour setting to force message threads to scroll to the bottom after loading.
* Add faq for disabling video auto play

## v1.4.2
* Experimental: Add notifications for messages; report to me if this drains your battery
* Add FAQ in the about section
* Add video uploading
* Add open link option in context menu
* Add geolocation
* Update theme
* Fix notification titles
* ALPHA: Add support for downloading videos (hit the download button)
* Deny intents for login so the page loads properly (thank you @Zenexer)
* Reduce injection offset and move injectors to an earlier method
* Add option to disable media loading on metered network
* Fix menu section
* Add more background setters to help transparent themes

## v1.4.1
* Add intro pages
* Style new comment highlights
* Style reaction background
* Disable pull to refresh when typing is detected

## v1.4
* Update IAB helper
* Create image viewing and downloading; long press any image!
* Start filtering out unnecessary loads
* Fix notification duplicates
* Fix long pressing album images
* Add friend request tab in nav bar
* Aggressively filter nonrecent posts in recents mode
* Add download option for full sized images
* Fix rounded icons
* Fix regex bug for some devices
* Fix notification text
* Update round icons
* Allow for multiple result flags in settings to better reload the main view
* Add custom image picker
* Check if activities can open intent before loading in browser
* Add url debugging. Long press a button and press debug link to send me an email
* Update theme
* Move search bar style to default
* Allow for image downloads in messages

## v1.3
* Create toggle for notifications only from primary account
* Micro string optimizations
* Add profile icons to notifications
* Make notifications expandable
* Add notification trigger in settings
* Fix bug where only single latest notification is showing
* Reduce Menu loading logic
* Load js injectors after showing webview
* Add toggles for sound, vibration, and lights
* Avoid restricting facebook features (such as user tagging)
* Add option to disable loading in overlays
* Fixed experimental search result text

## v1.2
* Scale browser on keyboard pop up
* Clean up web overlay
* Allow customization of overlay swipe
* Add sharing menu options in the overlay app
* Improved rounding icons
* Add web text scaling
* Create context menu; long press on a link!
* Intelligently stop horizontal page scrolling on long press
* More theming

## v1.1
* Add universal experimental toggle
* Fixed up billing properties and add auto checker
* Open status updates in new window
* Allow for photo uploads
* Improve search bar to stop when not in view
* Integrate CI
* Add more theme fixes
* Initial Reddit beta release

## v1.0
* Add more global preferences
* Add fully customizable theme engine
* Add support for in app billing
* Huge changes internally from KAU
* Add credits section
* Add experimental section
* Add search option
* Fix up main layout
* Fix some theme components
* Add behaviour settings
* Add about section with links

## v0.3
* Add rounded icons option
* Sort preferences
* Add adblock base
* Add feed configurations
* Animated settings panels
* Add notification filters

## v0.2
* Remove unnecessary permissions
* Add notifications
* Theme more components
* Separate independent web overlay from in app overlay
* Allow notifications from any account
* Smooth transition from Settings and only restart when necessary
* Add logout option from drawer
* Add many more drawer urls
* Add anonymous analytics
* Move settings to drawer
* Add feedback option

## v0.1
* Initial Changelog
* Create core databases
* Implement CSS/Js injectors
* Implement ripple preferences
* Create multiple account caching
* Create web overlay
