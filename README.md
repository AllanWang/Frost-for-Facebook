# Frost-for-Facebook

[![Releaes Version](https://img.shields.io/github/release/AllanWang/Frost-for-Facebook.svg)](https://github.com/AllanWang/Frost-for-Facebook/releases)
[![Build Status](https://travis-ci.com/AllanWang/Frost-for-Facebook.svg?branch=dev)](https://travis-ci.com/AllanWang/Frost-for-Facebook)
[![Crowdin](https://d322cqt584bo4o.cloudfront.net/frost-for-facebook/localized.svg)](https://crowdin.com/project/frost-for-facebook)
[![GitHub license](https://img.shields.io/badge/license-GPL--v3-blue.svg)](https://raw.githubusercontent.com/AllanWang/Frost-for-Facebook/dev/LICENSE)

[Download from Github Releases](https://github.com/AllanWang/Frost-for-Facebook/releases)

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
    alt="Get it on F-Droid"
    height="80">](https://f-droid.org/packages/com.pitchedapps.frost)
    
**This project is no longer actively maintained**. I still use it, but I've largely been off of Facebook for years. Bugs relating to logins are region dependent, and web wrappers don't have stable APIs, so please use at your own discretion.

**Note** Some keystores are public for the sake of automatic builds and consistent signing across devices.
This means that others can build apps with the same signature. The only valid download sources are through my github releases and F-Droid.

Frost is a third party Facebook wrapper geared towards design and functionality.
It contains many features, including:
* Support for multiple accounts and fast switching
* Full theming across all activities
* PIP videos
* Overlaying browser to read posts and get right back to your previous task
* Extensive notification support, with bundling, filtering, battery friendly scheduling, icons, and multi user support
* Context menu from any link via long press
* Native image viewer and downloader via long press
* Reactive based loading
* The transparency of open sourced development

Frost is the only third party Facebook app to have:
* Transparent themes and a fully customizable theme engine
* True multiuser support, along with multiuser notifications
* Fully swipable overlays
* Fully customizable tabs

Test builds can be found [here](https://github.com/AllanWang/Frost-for-Facebook-APK-Builder/releases).
Note that these builds occur for every commit, including unstable ones.
Typically, those merged into `master` are stable, and those merged into `dev` have been tested.

## Showcase

![Transparency](https://raw.githubusercontent.com/AllanWang/Storage-Hub/master/frost/screenshots/thumbnails/frost_themes.png)&ensp;
![Transparency](https://raw.githubusercontent.com/AllanWang/Storage-Hub/master/frost/screenshots/thumbnails/frost_glass.png)

![Transparency](https://raw.githubusercontent.com/AllanWang/Storage-Hub/master/frost/screenshots/thumbnails/frost_multi_accounts.png)&ensp;
![Transparency](https://raw.githubusercontent.com/AllanWang/Storage-Hub/master/frost/screenshots/thumbnails/frost_pip.png)

![Transparency](https://raw.githubusercontent.com/AllanWang/Storage-Hub/master/frost/screenshots/thumbnails/frost_swipe.png)&ensp;
![Transparency](https://raw.githubusercontent.com/AllanWang/Storage-Hub/master/frost/screenshots/thumbnails/frost_quick_links.png)

## Translations

Frost depends on translations crowdsourced by the general public.
If you would like to contribute, please visit [here](https://crwd.in/frost-for-facebook).
Note that this project heavily depends on [KAU](https://github.com/AllanWang/KAU), which also needs to be translated [here](https://crwd.in/kotlin-android-utils).

Special thanks to the following awesome people for translating significant portions of Frost!

| Language | Contributors |
|----------|--------------|
| Arabic | [Faris Sabaa](https://crowdin.com/profile/farissabaa) &bull; [hdmed.dev](https://crowdin.com/profile/hdmed) &bull; [Mohammed Qubati](https://crowdin.com/profile/Mrkqubati) |
| Catalan | [Jaime Muñoz Martín](https://crowdin.com/profile/jmmartin_5) |
| Chinese (Simplified) | [Zhengang](https://crowdin.com/profile/Zhengang) |
| Chinese (Traditional) | [StanAttack](https://crowdin.com/profile/StanAttack) &bull; [yipinghuang](https://crowdin.com/profile/yipinghuang) &bull; [jpss](https://crowdin.com/profile/jpss) &bull; [BrLi](https://crowdin.com/profile/brli) &bull; [Артём Х. Местный](https://crowdin.com/profile/megaahmadf) &bull; [Julio M.](https://crowdin.com/profile/juliomc31) |
| Czech | [Richard Janda](https://crowdin.com/profile/risajanda) &bull; [Misat11](https://crowdin.com/profile/Misat11) |
| Danish | [mhtorp](https://crowdin.com/profile/mhtorp) |
| Dutch | [ItGuillaume](https://crowdin.com/profile/ltGuillaume) &bull; [msoehnchen](https://crowdin.com/profile/msoehnchen) &bull; [T.T.](https://crowdin.com/profile/LifeisallBeerndSkittles) &bull; [just_a_tech](https://crowdin.com/profile/just_a_tech) |
| French | [Vincent Kulak](https://github.com/VonOx) &bull; [Jean-Philippe Gravel](https://crowdin.com/profile/wokija) |
| Galician | [Xesús M. Mosquera](https://twitter.com/xesusmmc?lang=en) |
| German | [Bushido1992](https://forum.xda-developers.com/member.php?u=5179246) &bull; [Marcel Soehnchen](https://crowdin.com/profile/msoehnchen) &bull; [3LD0mi HA](https://forum.xda-developers.com/member.php?u=5860523) |
| Greek | [Nick Choremiotis](https://crowdin.com/profile/FeVerSeCtioN) &bull; [George Kitsopoulos](https://crowdin.com/profile/GeorgeKitsopoulos) |
| Hungarian | [János Erkli](https://crowdin.com/profile/erklijani0521) &bull; [Bálint Csurgai-Horváth](https://crowdin.com/profile/cshbalint) |
| Indonesian | [M. Angga Ariska](https://www.youtube.com/channel/UCkqMw81s2aw7bYO-U2YhD7w) |
| Italian | [Bonnee](https://github.com/Bonnee) |
| Korean | [잇스테이크](https://crowdin.com/profile/bexco2010) &bull; [Jun-woo Kim](https://crowdin.com/profile/junwookapa) |
| Malayalam | [Abhishek M](https://crowdin.com/profile/abhishekabhi789) |
| Norwegian | [Julian Madsen](https://crowdin.com/profile/julianmadsen1) |
| Polish | [Mroczny](https://crowdin.com/profile/Mroczny) &bull; [pantinPL](https://crowdin.com/profile/pantinPL) &bull; [B.O.S.S.](https://crowdin.com/profile/B.O.S.S) |
| Portuguese | [Ekzos](https://crowdin.com/profile/Ekzos) &bull; [Sérgio Marques](https://crowdin.com/profile/smarquespt) &bull; [Francisco Fernandes](https://crowdin.com/profile/alex2fernandes) &bull; [Cláudio Faria](https://crowdin.com/profile/claudiofariacf) |
| Portuguese (Brazilian) | [TheusKhan](https://crowdin.com/profile/TheusKhan) |
| Romanian | [Drăgan Florin Ovidiu](https://crowdin.com/profile/ovidiudragan2012) &bull; [Marian Bailescu](https://crowdin.com/profile/marianbailescu) |
| Russian | [Eugene Tareyev](https://crowdin.com/profile/haired) &bull; [Vitali Bl](https://crowdin.com/profile/vital0000000) &bull; [Felix Fester](https://crowdin.com/profile/slendy00880) &bull; [Вадим Жушман](https://crowdin.com/profile/android54544)|
| Serbian | [vuklozo](https://crowdin.com/profile/vuklozo) &bull; [Nikola Radmanović](https://crowdin.com/profile/nikoladradmanovic) &bull; [M23](https://crowdin.com/profile/M23) |
| Spanish | [Jahir Fiquitiva](https://jahirfiquitiva.me/) &bull; [Nefi Salazar](https://plus.google.com/u/0/105547968033551087431) |
| Swedish | [Artswitcher](https://crowdin.com/profile/Artswitcher) &bull; [Henrik Mattsson-Mårn](https://crowdin.com/profile/rchk) |
| Tagalog | [Cryptoffer Translator](https://crowdin.com/profile/toffer0219) |
| Thai | [Thanawat Hanthong](https://crowdin.com/profile/peet6015) |
| Turkish | [upvotelife](https://crowdin.com/profile/upvotelife) &bull; [Kardelen Sepetçi](https://crowdin.com/profile/kardeland) |
| Ukrainian | [Таня Делікатна](https://crowdin.com/profile/delikatna_i) &bull; [Вадим Жушман](https://crowdin.com/profile/android54544) |
| Vietnamese | [Alienz](https://crowdin.com/profile/alienyd) &bull; [Nguyễn Thành Nam](https://crowdin.com/profile/nguyenthanhnam_246) &bull; [Volodymyr Lisivka](https://crowdin.com/profile/vlisivka) |

The full activity stream for the translations can be found [here](https://crowdin.com/project/frost-for-facebook/activity_stream)
