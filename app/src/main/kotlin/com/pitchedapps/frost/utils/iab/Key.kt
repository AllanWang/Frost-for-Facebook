package com.pitchedapps.frost.utils.iab

/**
 * Created by Allan Wang on 2017-06-23.
 *
 * NOTE
 *
 * Since this is an open source project and all other components are essentially public,
 * I have decided to add the keys here too;
 * they can be reverse engineered relatively easily since they are constants anyways.
 * This is the public billing key from the play store
 */
private const val play_key_1 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgyTZS"
private const val play_key_2 = "K9Bd3ALpr9KJUsVGczP9CcPelWkdnJfNrrzu1EztJyrHRsGQ4"
private const val play_key_3 = "QVWY9NZwc6Nrk9qdJlEdr8AJAxJ+JiwUqsj3/TxxUYm/G7q8Z"
private const val play_key_4 = "7zo8jSkYZyzqwoAl2PDx2kleI4sZLkhCRLyE6dGQEZQmvJ6kk"
private const val play_key_5 = "W12Gz3FagAM5luRGsoWZj40pJItUrGJA9euMWq4rMhVZv4mVk"
private const val play_key_6 = "KFJB9/vhF/XGz7txpYlFxMESzXuKsbEDKmKCHzvySLq8Ki4N9"
private const val play_key_7 = "DzbgUiw+VzA2KpSVp66JH3GEU8egO8i9SvAWeCPikuolooRVh"
private const val play_key_8 = "jwfBV7gDxZztoLuvmQU6kXvCwRnRa+mkfUnBKKLkH1QIDAQAB"

internal val PUBLIC_BILLING_KEY: String by lazy {
    StringBuilder()
            .append(play_key_1)
            .append(play_key_2)
            .append(play_key_3)
            .append(play_key_4)
            .append(play_key_5)
            .append(play_key_6)
            .append(play_key_7)
            .append(play_key_8)
            .toString()
}