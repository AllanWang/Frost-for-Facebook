# Changelog

## v1.7.9
* Fix image loading
* Fix sending emails
* Greatly improve load time by displaying page sooner
* Fix blank web overlays
* Revert back to m.facebook.com

## v1.7.8
* Emergency fix for notifications

## v1.7.7
* Fix overlay loading
* Improve image loading
* Launch image viewer when opening full sized image
* Improve filtering system

## v1.7.5
* Mark notifications as read when clicked!
* Create menu parser
* Implement automatic web fallback
* Optimize logging
* Fix link loading for some locations (eg changing profile pictures)

## v1.7.2
* Optimize login view
* Rewrite parsers
* Fix message notification icons
* Small theme updates

## v1.7.1
* Fix launching messages in new overlay
* Fix some errors in launching pages
* Redid base design to prepare for native views
* Automatically bring toolbar up when keyboard is shown
* Rewrite theme components to fully support AMOLED and improve light
* Properly pause webviews when not in use

## v1.7.0
* Fully customize your tabs! Check out settings > appearance > main activity tabs
* Optimize scripts
* Add more theme components
* Fixed issue with deleting posts
* Automatically close video player if url cannot be loaded

## v1.6.7
* Fix icons in tablets
* Add Portuguese translations
* Add Galician translations
* Remove generic translations

## v1.6.5
* Allow for truly full screen videos
* Support pip video everywhere
* Support gifs (auto loop)
* Add Italian translations
* Add Korean translations
* Add Vietnamese translations
* Fix bad date param for videos and images
* Allow for explicit intent launches
* Updated all dependencies

## v1.6.2
* Fix search update from Facebook
* Fix url parsing errors again
* Fix pip for bottom bar
* Fix pip after rotation

## v1.6.0
* Add Spanish translations
* Add French translations
* Add German translations
* Check permissions before attempting upload or download
* Add pip video support
* Add video downloader
* Fix bugs with parsing url queries
* Fix search update from Facebook

## v1.5.9
* Add notification support for Android O

## v1.5.8
* Fix theme for newer comments layout
* Revert media picker to use system default

## v1.5.7
* Allow hiding of composer and groups you may knnow
* Theme overflow icon
* Greatly improve search bar

## v1.5.6
* Greatly improve background notification fetcher
* Support fetching messages from all accounts
* Update theme

## v1.5.5
* Numerous bug fixes in KAU
* Set background back to white on non facebook pages
* Make read notification/message colors more obvious
* Clean up and small bug fixes
* Fix facebook link parsing issue for many links

## v1.5.2
* Add default download manager to download all files
* Limit notification sounds when multiple notifications come in
* Check that job scheduler exists before scheduling notifications

## v1.5.1
* Release day is here!
* Add full support for messaging in overlays. We will dynamically launch new overlays when required to.
* Prevent bad messenger intent from launching
* Add toggle for recents feed. Aggressive loading removes extra posts that are not really recent, whereas disabling it will show exactly what you get on Facebook
* Add contextual menu items. Easily go to your full list of notifications or messages from the overlay.
* Ensure that bottom bar layout does not hide the web content
* Add option to share external links to Frost
* Trigger notification service on each app start

## v1.4.13
* Prevent image loading from trimming too many characters
* Fix most recent mode for news feed
* Add link to disable video autoplay in settings > newsfeed
* Update theme

## v1.4.12
* Fix new messages not showing
* Fix theme for sharing and new messages
* Prevent search bar settings from disabling itself if auto suggestions fail
* Fix numerous crashes relating to search bar layouts
* Add debugging for menu

## v1.4.11
* Fix url loading bug and add option to launch urls in default browser (behaviour setting)
* Allow for bottom bar tabs (appearance setting)
* Allow custom ringtones for notifications and messages (notification setting)
* Improve logout logic when account is invalid
* Fix material light background for certain items
* Fix initial login not sticking
* Allow back press in login activity
* Update themes

## v1.4.7
* Update secondary background for transparent themes to be more visible.
* Pressing enter when searching will launch the full search page
* Add different backgrounds for news feed articles.
* Add option to get image/video from default camera or gallery app.
* Fix some bug reports.
* Remove error dialog for IAB. It will now depend solely on the google services dialogs.
* Fix loading issue for old conversations
* Add debugger for failed image activities

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
