package com.zenith.focus.domain.model

enum class ContentCategory(val displayName: String, val defaultPackageName: String) {
    YOUTUBE_SHORTS("YouTube Shorts", "com.google.android.youtube"),
    INSTAGRAM_REELS("Instagram Reels", "com.instagram.android"),
    SNAPCHAT_SPOTLIGHT("Snapchat Spotlight", "com.snapchat.android"),
    FACEBOOK_REELS("Facebook Reels", "com.facebook.katana"),
    TIKTOK("TikTok", "com.zhiliaoapp.musically"),
    OTHER_SHORT_VIDEO("Other Short Video", "generic.shortvideo"),
    ADULT_WEBSITE("Adult Website", "browser.adult.domain"),
    ADULT_KEYWORD("Explicit Content", "content.explicit.keyword"),
    SYSTEM_TAMPER("Tamper Protection", "com.android.settings"),
    APP_LOCK("App Lock Shield", "app.lock.shield");

    companion object {
        fun fromPackage(packageName: String): ContentCategory? {
            return values().firstOrNull { it.defaultPackageName == packageName }
        }
    }
}
