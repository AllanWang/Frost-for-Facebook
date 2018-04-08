package com.pitchedapps.frost.manager

/**
 * Created by Allan Wang on 07/04/18.
 */
data class FrostRelease(val versionName: String,
                        val timestamp: Long,
                        val downloadUrl: String,
                        val downloadCount: Long = -1,
                        val category: String = "")