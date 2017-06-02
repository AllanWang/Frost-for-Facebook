package com.pitchedapps.frost.web

enum class FrostWebEnums {
    LOADING, LOADED, ERROR
}

enum class FrostWebOverlay(val match: String) {
    STORY("story.php?story_fbid");

    companion object {
        val values = values()
    }
}