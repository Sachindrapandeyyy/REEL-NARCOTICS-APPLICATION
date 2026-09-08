package com.zenith.focus.domain.model

data class ProtectionConfig(
    val blockYouTubeShorts: Boolean = true,
    val blockInstagramReels: Boolean = true,
    val blockSnapchatSpotlight: Boolean = true,
    val blockFacebookReels: Boolean = true,
    val blockTikTok: Boolean = true,
    val blockOtherShortVideo: Boolean = true,
    val blockAdultWebsites: Boolean = true,
    val blockAdultKeywords: Boolean = true,
    val browserProtectionEnabled: Boolean = true,
    val strictMode: Boolean = true,
    val nuclearMode: Boolean = false, // When true, no early unlock is allowed whatsoever
    val frictionType: FrictionType = FrictionType.HOLD_BUTTON,
    val unlockPhrase: String = "I choose my long term goals over cheap dopamine",
    val pinHash: String = "",
    val pinSalt: String = ""
) {
    fun isCategoryBlocked(category: ContentCategory): Boolean {
        return when (category) {
            ContentCategory.YOUTUBE_SHORTS -> blockYouTubeShorts
            ContentCategory.INSTAGRAM_REELS -> blockInstagramReels
            ContentCategory.SNAPCHAT_SPOTLIGHT -> blockSnapchatSpotlight
            ContentCategory.FACEBOOK_REELS -> blockFacebookReels
            ContentCategory.TIKTOK -> blockTikTok
            ContentCategory.OTHER_SHORT_VIDEO -> blockOtherShortVideo
            ContentCategory.ADULT_WEBSITE -> blockAdultWebsites
            ContentCategory.ADULT_KEYWORD -> blockAdultKeywords
            ContentCategory.SYSTEM_TAMPER -> true
        }
    }
}
