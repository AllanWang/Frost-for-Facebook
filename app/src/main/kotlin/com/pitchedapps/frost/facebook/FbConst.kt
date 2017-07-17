package com.pitchedapps.frost.facebook

/**
 * Created by Allan Wang on 2017-06-01.
 */
const val HTTPS_FACEBOOK_COM = "https://facebook.com"
const val FACEBOOK_COM = "facebook.com"
const val FB_URL_BASE = "https://m.facebook.com/"
fun PROFILE_PICTURE_URL(id: Long) = "https://graph.facebook.com/$id/picture?type=large"

const val USER_AGENT_FULL = "Mozilla/5.0 (Linux; Android 4.4.2; en-us; SAMSUNG SM-G900T Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Version/1.6 Chrome/28.0.1500.94 Mobile Safari/537.36"
const val USER_AGENT_BASIC = "Mozilla/5.0 (Linux; U; Android 2.3.3; en-gb; Nexus S Build/GRI20) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1"
const val USER_AGENT_MESSENGER = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"