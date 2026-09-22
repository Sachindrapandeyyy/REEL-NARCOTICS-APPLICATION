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
    fun testYouTubePWSearchAllowedWithShortsInFeed() {
        val detector = YouTubeShortsDetector()
        val context = ScreenContext(
            packageName = "com.google.android.youtube",
            className = "com.google.android.apps.youtube.app.search.SearchActivity",
            viewIds = setOf(
                "com.google.android.youtube:id/search_results_editor",
                "com.google.android.youtube:id/search_chip_bar",
                "com.google.android.youtube:id/reel_shelf",
                "com.google.android.youtube:id/reel_shelf_header"
            ),
            visibleTexts = listOf(
                "Physics Wallah - Alakh Pandey",
                "PW Lakshya JEE Full Physics Lecture",
                "PW Motivation #shorts",
                "Shorts"
            ),
            contentDescriptions = listOf("Search chip", "Shorts shelf"),
            allNormalizedTokens = setOf("physics", "wallah", "alakh", "pandey", "pw", "lakshya", "jee", "shorts", "motivation")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse("Search results for PW with inline shorts shelf must be allowed!", result.isBlocked)
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

    @Test
    fun testYouTubeShortsWithReelItemChildLayoutsBlocked() {
        val detector = YouTubeShortsDetector()
        // Scenario: Short is playing inside reel_recycler, and child items contain "reel_item".
        // Must be blocked and NOT misclassified as shelf!
        val context = ScreenContext(
            packageName = "com.google.android.youtube",
            className = "android.widget.FrameLayout",
            viewIds = setOf(
                "com.google.android.youtube:id/reel_recycler",
                "com.google.android.youtube:id/reel_item_holder_layout",
                "com.google.android.youtube:id/reel_player_page_view"
            ),
            visibleTexts = listOf("Viral Short #shorts"),
            contentDescriptions = listOf("Shorts video player", "Dislike this short", "Remix this Short"),
            allNormalizedTokens = setOf("viral", "short", "shorts", "remix")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertTrue("Active Short playing in reel_recycler must be blocked even if reel_item child views exist", result.isBlocked)
        assertEquals(ContentCategory.YOUTUBE_SHORTS, result.category)
        assertEquals(1.0f, result.confidence, 0.01f)
    }

    @Test
    fun testYouTubeShortsTabSelectedWithoutPivotBarBlocked() {
        val detector = YouTubeShortsDetector()
        // Scenario: Navigation bar ID is not pivot_bar (e.g. custom or obfuscated), but Shorts tab is selected
        val context = ScreenContext(
            packageName = "com.google.android.youtube",
            className = "android.widget.FrameLayout",
            viewIds = setOf("com.google.android.youtube:id/bottom_navigation_bar"),
            visibleTexts = listOf("Home", "Shorts", "Subscriptions"),
            contentDescriptions = listOf("Home", "Shorts", "Subscriptions"),
            allNormalizedTokens = setOf("home", "shorts", "subscriptions"),
            selectedDescriptions = setOf("Shorts")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertTrue("Selected Shorts tab without pivot_bar view ID must trigger instant block", result.isBlocked)
        assertEquals(1.0f, result.confidence, 0.01f)
    }

    @Test
    fun testYouTubeShortsTabSelectedViaContentDescriptionBlocked() {
        val detector = YouTubeShortsDetector()
        // Scenario: Bottom tab has accessibility description indicating selected state
        val context = ScreenContext(
            packageName = "com.google.android.youtube",
            className = "android.widget.FrameLayout",
            viewIds = setOf("com.google.android.youtube:id/navigation_bar"),
            visibleTexts = listOf("Home", "Shorts"),
            contentDescriptions = listOf("Shorts, tab 2 of 5, selected"),
            allNormalizedTokens = setOf("home", "shorts", "selected")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertTrue("Shorts tab indicated via content description must be blocked", result.isBlocked)
        assertEquals(1.0f, result.confidence, 0.01f)
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

    @Test
    fun testInstagramClipsContainerBlocked() {
        val detector = InstagramReelsDetector()
        val context = ScreenContext(
            packageName = "com.instagram.android",
            className = "com.instagram.modal.ModalActivity",
            viewIds = setOf("com.instagram.android:id/clips_viewer_container", "com.instagram.android:id/clips_item"),
            visibleTexts = listOf("Watch more Reels"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("reels")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertTrue("Clips container must be blocked", result.isBlocked)
        assertEquals(ContentCategory.INSTAGRAM_REELS, result.category)
        assertEquals(1.0f, result.confidence, 0.01f)
    }

    @Test
    fun testInstagramReelsTabSelectedBlocked() {
        val detector = InstagramReelsDetector()
        val context = ScreenContext(
            packageName = "com.instagram.android",
            className = "com.instagram.mainactivity.MainActivity",
            viewIds = setOf("com.instagram.android:id/tab_bar"),
            visibleTexts = emptyList(),
            contentDescriptions = listOf("Home", "Search", "Create", "Reels, tab 4 of 5, selected", "Profile"),
            allNormalizedTokens = setOf("reels")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertTrue("Reels tab actively selected must be blocked", result.isBlocked)
        assertEquals(ContentCategory.INSTAGRAM_REELS, result.category)
        assertTrue(result.confidence >= 0.95f)
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

    @Test
    fun testGoogleSearchIASOfficerHolidaySecurityAllowed() {
        val adultDetector = AdultContentDetector()
        val googleSearchPkg = "com.google.android.googlequicksearchbox"
        
        // 1. AdultContentDetector must NOT handle Google Search app (browsers only)
        assertFalse("AdultContentDetector must never handle Google Search app", adultDetector.canHandle(googleSearchPkg))

        // 2. Even if evaluated, safe tokens like ias, holiday, security, rules must prevent any block
        val context = ScreenContext(
            packageName = googleSearchPkg,
            className = "com.google.android.apps.search.googleapp.activity.GoogleAppActivity",
            viewIds = setOf("search_box", "results_view"),
            visibleTexts = listOf("ias officer ki holiday security escort vehicle official rules"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("ias", "officer", "ki", "holiday", "security", "escort", "vehicle", "official", "rules")
        )
        val result = adultDetector.evaluate(context, defaultConfig)
        assertFalse("Official IAS security query must never be blocked", result.isBlocked)
    }

    @Test
    fun testChromeIASOfficerSecurityEscortQueryAllowed() {
        val adultDetector = AdultContentDetector()
        // Query inside Chrome browser containing the word escort and official security context
        val context = ScreenContext(
            packageName = "com.android.chrome",
            className = "org.chromium.chrome.browser.ChromeTabbedActivity",
            viewIds = setOf("url_bar"),
            visibleTexts = listOf("Government rules for IAS officer holiday security escort protocol"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("government", "rules", "ias", "officer", "holiday", "security", "escort", "protocol")
        )
        val result = adultDetector.evaluate(context, defaultConfig)
        assertFalse("Government escort protocol in Chrome must never trigger adult block", result.isBlocked)
    }

    @Test
    fun testEssentialAppExclusionFromGenericShortFormDetector() {
        val genericDetector = com.zenith.focus.accessibility.detector.GenericShortFormDetector()
        val essentialApps = listOf(
            "com.google.android.googlequicksearchbox",
            "com.android.phone",
            "com.google.android.dialer",
            "com.google.android.apps.messaging",
            "com.google.android.gm",
            "com.whatsapp",
            "org.telegram.messenger",
            "com.google.android.calculator",
            "com.google.android.deskclock",
            "com.google.android.apps.maps",
            "com.android.chrome"
        )
        for (pkg in essentialApps) {
            assertFalse("Generic detector must never handle $pkg", genericDetector.canHandle(pkg))
        }
    }

    @Test
    fun testTikTokLiteDetection() {
        val detector = TikTokDetector()
        assertTrue("TikTok detector must handle TikTok Lite", detector.canHandle("com.zhiliaoapp.musically.go"))
        val context = ScreenContext(packageName = "com.zhiliaoapp.musically.go")
        val result = detector.evaluate(context, defaultConfig)
        assertTrue("TikTok Lite feed must be blocked", result.isBlocked)
        assertEquals(ContentCategory.TIKTOK, result.category)
    }

    @Test
    fun testFacebookMessengerChatAllowed() {
        val detector = FacebookReelsDetector()
        assertTrue(detector.canHandle("com.facebook.orca"))
        val context = ScreenContext(
            packageName = "com.facebook.orca",
            viewIds = setOf("thread_view", "composer_text_view"),
            visibleTexts = listOf("Hey, check out this message", "Send")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse("Active Messenger chat thread must never be blocked", result.isBlocked)
    }

    @Test
    fun testBrowserUrlDetectorPrioritizesAddressBarOverBodyText() {
        val detector = BrowserUrlDetector(initialBlockedDomains = setOf("badsite.com"))
        val context = ScreenContext(
            packageName = "com.android.chrome",
            viewIds = setOf("url_bar"),
            nodeTextMap = mapOf("url_bar" to "https://wikipedia.org"),
            visibleTexts = listOf("Visit badsite.com for information about cybersecurity")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse("Address bar indicates safe site; body text with badsite.com must not trigger block", result.isBlocked)
    }
}
