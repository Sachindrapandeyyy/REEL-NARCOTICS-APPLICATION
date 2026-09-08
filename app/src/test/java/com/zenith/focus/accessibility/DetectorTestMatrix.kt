package com.zenith.focus.accessibility

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.accessibility.detector.AdultContentDetector
import com.zenith.focus.accessibility.detector.BrowserUrlDetector
import com.zenith.focus.accessibility.detector.FacebookReelsDetector
import com.zenith.focus.accessibility.detector.InstagramReelsDetector
import com.zenith.focus.accessibility.detector.SnapchatSpotlightDetector
import com.zenith.focus.accessibility.detector.TikTokDetector
import com.zenith.focus.accessibility.detector.YouTubeShortsDetector
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectorTestMatrix {

    private val defaultConfig = ProtectionConfig()

    // 1. YouTube Shorts Matrix
    @Test
    fun testYouTubeHomeAllowed() {
        val detector = YouTubeShortsDetector()
        val context = ScreenContext(
            packageName = "com.google.android.youtube",
            className = "com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity",
            viewIds = setOf("com.google.android.youtube:id/home_content", "com.google.android.youtube:id/pivot_bar"),
            visibleTexts = listOf("Home", "Subscriptions"),
            contentDescriptions = listOf("Home tab"),
            allNormalizedTokens = setOf("home", "subscriptions")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse(result.isBlocked)
    }

    @Test
    fun testYouTubeSearchAllowed() {
        val detector = YouTubeShortsDetector()
        val context = ScreenContext(
            packageName = "com.google.android.youtube",
            className = "com.google.android.apps.youtube.app.search.SearchActivity",
            viewIds = setOf("com.google.android.youtube:id/search_results_editor", "com.google.android.youtube:id/search_chip_bar"),
            visibleTexts = listOf("Search results for kotlin tutorial"),
            contentDescriptions = listOf("Search chip"),
            allNormalizedTokens = setOf("search", "results", "kotlin")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse(result.isBlocked)
    }

    @Test
    fun testYouTubeLongVideoPlayerAllowed() {
        val detector = YouTubeShortsDetector()
        val context = ScreenContext(
            packageName = "com.google.android.youtube",
            className = "com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity",
            viewIds = setOf("com.google.android.youtube:id/watch_while_layout", "com.google.android.youtube:id/player_fragment", "com.google.android.youtube:id/play_pause_button"),
            visibleTexts = listOf("Android Jetpack Compose Full Course 2026"),
            contentDescriptions = listOf("Play video", "Pause"),
            allNormalizedTokens = setOf("android", "jetpack", "compose")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse(result.isBlocked)
    }

    @Test
    fun testYouTubeShortsFeedBlocked() {
        val detector = YouTubeShortsDetector()
        val context = ScreenContext(
            packageName = "com.google.android.youtube",
            className = "com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity",
            viewIds = setOf("com.google.android.youtube:id/reel_recycler", "com.google.android.youtube:id/reel_player_page_view"),
            visibleTexts = listOf("Shorts", "Remix"),
            contentDescriptions = listOf("Shorts video player", "Dislike this short"),
            allNormalizedTokens = setOf("shorts", "remix")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertTrue(result.isBlocked)
        assertEquals(ContentCategory.YOUTUBE_SHORTS, result.category)
        assertTrue(result.confidence >= 0.70f)
    }

    @Test
    fun testYouTubeDumpedShortsLayoutBlocked() {
        val detector = YouTubeShortsDetector()
        val context = ScreenContext(
            packageName = "com.google.android.youtube",
            className = "android.widget.FrameLayout",
            viewIds = setOf(
                "com.google.android.youtube:id/reel_watch_fragment_root",
                "com.google.android.youtube:id/reel_recycler",
                "com.google.android.youtube:id/reel_player_page_container",
                "com.google.android.youtube:id/reel_player_overlay_root",
                "com.google.android.youtube:id/reel_time_bar"
            ),
            visibleTexts = listOf("Guess the opponent! #shorts"),
            contentDescriptions = listOf("Remix this Short along with 11 lakh other remixes", "Shorts"),
            allNormalizedTokens = setOf("guess", "opponent", "shorts", "remix"),
            selectedDescriptions = setOf("Shorts")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertTrue("Dumped Shorts layout must be blocked", result.isBlocked)
        assertEquals(ContentCategory.YOUTUBE_SHORTS, result.category)
        assertEquals(1.0f, result.confidence, 0.01f)
    }

    @Test
    fun testYouTubePivotBarShortsTabSelectedBlocked() {
        val detector = YouTubeShortsDetector()
        val context = ScreenContext(
            packageName = "com.google.android.youtube",
            className = "android.widget.FrameLayout",
            viewIds = setOf("com.google.android.youtube:id/pivot_bar"),
            visibleTexts = listOf("Home", "Shorts", "Subscriptions"),
            contentDescriptions = listOf("Home", "Shorts", "Subscriptions"),
            allNormalizedTokens = setOf("home", "shorts", "subscriptions"),
            selectedDescriptions = setOf("Shorts")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertTrue("Selected Shorts tab must trigger instant block", result.isBlocked)
        assertEquals(1.0f, result.confidence, 0.01f)
    }

    @Test
    fun testYouTubePivotBarHomeTabSelectedAllowed() {
        val detector = YouTubeShortsDetector()
        val context = ScreenContext(
            packageName = "com.google.android.youtube",
            className = "android.widget.FrameLayout",
            viewIds = setOf("com.google.android.youtube:id/pivot_bar", "com.google.android.youtube:id/feed_filter_bar"),
            visibleTexts = listOf("Home", "Shorts", "Subscriptions", "Trending"),
            contentDescriptions = listOf("Home", "Shorts", "Subscriptions"),
            allNormalizedTokens = setOf("home", "shorts", "subscriptions", "trending"),
            selectedDescriptions = setOf("Home")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse("Home tab with unselected Shorts tab in pivot bar should NOT be blocked", result.isBlocked)
    }

    // 2. Instagram Reels Matrix
    @Test
    fun testInstagramFeedAllowed() {
        val detector = InstagramReelsDetector()
        val context = ScreenContext(
            packageName = "com.instagram.android",
            className = "com.instagram.mainactivity.MainActivity",
            viewIds = setOf("com.instagram.android:id/feed_recycler"),
            visibleTexts = listOf("Liked by 42 others", "Summer vacation photo"),
            contentDescriptions = listOf("Double tap to like"),
            allNormalizedTokens = setOf("liked", "summer", "vacation")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse(result.isBlocked)
    }

    @Test
    fun testInstagramDirectMessageAllowed() {
        val detector = InstagramReelsDetector()
        val context = ScreenContext(
            packageName = "com.instagram.android",
            className = "com.instagram.modal.ModalActivity",
            viewIds = setOf("com.instagram.android:id/direct_thread_feed", "com.instagram.android:id/row_thread_composer"),
            visibleTexts = listOf("Hey, let's meet at 5 PM"),
            contentDescriptions = listOf("Send message"),
            allNormalizedTokens = setOf("meet", "send", "message")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse(result.isBlocked)
    }

    @Test
    fun testInstagramReelsViewerBlocked() {
        val detector = InstagramReelsDetector()
        val context = ScreenContext(
            packageName = "com.instagram.android",
            className = "com.instagram.modal.ModalActivity",
            viewIds = setOf("com.instagram.android:id/clips_viewer_view_pager", "com.instagram.android:id/clips_video_container"),
            visibleTexts = listOf("Use audio", "Original audio"),
            contentDescriptions = listOf("Reels, tab 4 of 5", "Reel by @creator"),
            allNormalizedTokens = setOf("reels", "audio")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertTrue(result.isBlocked)
        assertEquals(ContentCategory.INSTAGRAM_REELS, result.category)
        assertTrue(result.confidence >= 0.70f)
    }

    // 3. Snapchat Spotlight Matrix
    @Test
    fun testSnapchatChatAllowed() {
        val detector = SnapchatSpotlightDetector()
        val context = ScreenContext(
            packageName = "com.snapchat.android",
            className = "com.snap.chat.ChatActivity",
            viewIds = setOf("chat_v3_container", "chat_input_text_field"),
            visibleTexts = listOf("Chat with Alex"),
            contentDescriptions = listOf("Send snap"),
            allNormalizedTokens = setOf("chat", "alex")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse(result.isBlocked)
    }

    @Test
    fun testSnapchatSpotlightBlocked() {
        val detector = SnapchatSpotlightDetector()
        val context = ScreenContext(
            packageName = "com.snapchat.android",
            className = "com.snap.spotlight.SpotlightActivity",
            viewIds = setOf("spotlight_container", "spotlight_fullscreen"),
            visibleTexts = listOf("Spotlight", "Remix Snap"),
            contentDescriptions = listOf("Spotlight tab"),
            allNormalizedTokens = setOf("spotlight", "remix")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertTrue(result.isBlocked)
        assertEquals(ContentCategory.SNAPCHAT_SPOTLIGHT, result.category)
    }

    // 4. Facebook Reels Matrix
    @Test
    fun testFacebookFeedAllowed() {
        val detector = FacebookReelsDetector()
        val context = ScreenContext(
            packageName = "com.facebook.katana",
            className = "com.facebook.katana.FBNewsFeedActivity",
            viewIds = setOf("newsfeed_recycler", "story_tray"),
            visibleTexts = listOf("What's on your mind?"),
            contentDescriptions = listOf("News Feed tab"),
            allNormalizedTokens = setOf("newsfeed", "story")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse(result.isBlocked)
    }

    @Test
    fun testFacebookReelsBlocked() {
        val detector = FacebookReelsDetector()
        val context = ScreenContext(
            packageName = "com.facebook.katana",
            className = "com.facebook.katana.ReelsActivity",
            viewIds = setOf("reel_fullscreen_view", "fb_shorts_container"),
            visibleTexts = listOf("Reels and short videos"),
            contentDescriptions = listOf("Reel by"),
            allNormalizedTokens = setOf("reels", "short", "videos")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertTrue(result.isBlocked)
        assertEquals(ContentCategory.FACEBOOK_REELS, result.category)
    }

    // 5. TikTok Matrix
    @Test
    fun testTikTokBlocked() {
        val detector = TikTokDetector()
        val context = ScreenContext(
            packageName = "com.zhiliaoapp.musically",
            className = "com.ss.android.ugc.aweme.main.MainActivity",
            viewIds = emptySet(),
            visibleTexts = emptyList(),
            contentDescriptions = emptyList(),
            allNormalizedTokens = emptySet()
        )
        val result = detector.evaluate(context, defaultConfig)
        assertTrue(result.isBlocked)
        assertEquals(ContentCategory.TIKTOK, result.category)
    }

    // 6. Adult Content Matrix (Scunthorpe Prevention)
    @Test
    fun testExplicitKeywordBlocked() {
        val detector = AdultContentDetector()
        val context = ScreenContext(
            packageName = "com.android.chrome",
            className = "org.chromium.chrome.browser.ChromeTabbedActivity",
            viewIds = emptySet(),
            visibleTexts = listOf("Free xxx porn videos online"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("free", "xxx", "porn", "videos", "online")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertTrue(result.isBlocked)
        assertEquals(ContentCategory.ADULT_KEYWORD, result.category)
    }

    @Test
    fun testBenignMedicalEducationalContextAllowed() {
        val detector = AdultContentDetector()
        // Word containing "sex" inside safe educational/medical context
        val context = ScreenContext(
            packageName = "com.android.chrome",
            className = "org.chromium.chrome.browser.ChromeTabbedActivity",
            viewIds = emptySet(),
            visibleTexts = listOf("Documentary on human biology and sexual education curriculum in Sussex"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("documentary", "biology", "sexual", "education", "curriculum", "sussex")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse(result.isBlocked)
    }
}
