package com.pitchedapps.frost.activities


/**
 * Created by Allan Wang on 2017-06-19.
 *
 * Replica of [FrostOverlayActivityBase] with a different base url
 * Didn't use activity-alias because it causes issues when only one activity has the singleInstance mode
 */
class FrostWebActivity : FrostOverlayActivityBase(false)

/**
 * Replica of [FrostOverlayActivityBase] with a different base url
 * Also forces the use of the basic user agent
 *
 * The default [WebOverlayActivity] is able to distinguish new message links,
 * so this is largely for internal use
 */
class FrostWebMessageActivity : FrostOverlayActivityBase(true)

class WebOverlayActivity : FrostOverlayActivityBase(false)