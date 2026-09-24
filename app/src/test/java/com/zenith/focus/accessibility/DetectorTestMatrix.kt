package com.zenith.focus.accessibility

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.accessibility.detector.AdultContentDetector
import com.zenith.focus.accessibility.detector.BrowserUrlDetector
import com.zenith.focus.accessibility.detector.FacebookReelsDetector
import com.zenith.focus.accessibility.detector.InstagramReelsDetector
import com.zenith.focus.accessibility.detector.SnapchatSpotlightDetector
import com.zenith.focus.accessibility.detector.TikTokDetector
import com.zenith.focus.accessibility.detector.YouTubeShortsDetector
import com.zenith.focus.accessibility.detector.GenericShortFormDetector
import com.zenith.focus.accessibility.detector.DetectionResult
import com.zenith.focus.accessibility.policy.NuclearProtectionPolicy
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.LockState
import com.zenith.focus.domain.model.ProtectionConfig
import com.zenith.focus.domain.nuclear.NuclearSession
import com.zenith.focus.domain.nuclear.NuclearSessionStatus
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

    @Test
    fun testInstagramLiteReelsBlocked() {
        val detector = InstagramReelsDetector()
        val context = ScreenContext(
            packageName = "com.instagram.lite",
            className = "com.instagram.lite.MainActivity",
            viewIds = setOf("video_player", "reels_tab"),
            visibleTexts = listOf("Reels", "Original audio"),
            contentDescriptions = listOf("Watch Reels"),
            allNormalizedTokens = setOf("reels", "audio")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertTrue("Instagram Lite Reels must be blocked", result.isBlocked)
        assertEquals(ContentCategory.INSTAGRAM_REELS, result.category)
        assertEquals(1.0f, result.confidence, 0.01f)
    }

    @Test
    fun testInstagramHomeWithUnselectedReelsTabAllowed() {
        val detector = InstagramReelsDetector()
        val context = ScreenContext(
            packageName = "com.instagram.android",
            className = "com.instagram.mainactivity.MainActivity",
            viewIds = setOf("com.instagram.android:id/feed_recycler", "com.instagram.android:id/action_bar_container"),
            visibleTexts = listOf("Instagram", "Liked by friends"),
            contentDescriptions = listOf("Home, tab 1 of 5, selected", "Search", "Create", "Reels, tab 4 of 5", "Profile"),
            allNormalizedTokens = setOf("instagram", "reels", "home"),
            selectedDescriptions = setOf("Home, tab 1 of 5, selected")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse("Instagram Home feed with unselected Reels tab in bottom bar must be ALLOWED", result.isBlocked)
    }

    // 3. Snapchat Spotlight Matrix
    @Test
    fun testSnapchatCameraWithBottomBarAllowed() {
        val detector = SnapchatSpotlightDetector()
        val context = ScreenContext(
            packageName = "com.snapchat.android",
            className = "com.snapchat.android.LandingPageActivity",
            viewIds = setOf("camera_capture_button", "navigation_host"),
            visibleTexts = listOf("Camera"),
            contentDescriptions = listOf("Map", "Chat", "Camera, selected", "Stories", "Spotlight"),
            allNormalizedTokens = setOf("camera", "spotlight"),
            selectedDescriptions = setOf("Camera, selected")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse("Snapchat camera with unselected Spotlight tab in bottom bar must be ALLOWED", result.isBlocked)
    }

    @Test
    fun testSnapchatStoriesDiscoverFeedAllowed() {
        val detector = SnapchatSpotlightDetector()
        val context = ScreenContext(
            packageName = "com.snapchat.android",
            className = "com.snapchat.android.LandingPageActivity",
            viewIds = setOf("discover_feed", "stories_carousel"),
            visibleTexts = listOf("Friends", "Subscriptions", "Discover"),
            contentDescriptions = listOf("Map", "Chat", "Camera", "Stories, selected", "Spotlight"),
            allNormalizedTokens = setOf("stories", "discover", "spotlight"),
            selectedDescriptions = setOf("Stories, selected")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse("Snapchat Discover/Stories feed must be ALLOWED", result.isBlocked)
    }

    @Test
    fun testSnapchatSpotlightTabSelectedBlocked() {
        val detector = SnapchatSpotlightDetector()
        val context = ScreenContext(
            packageName = "com.snapchat.android",
            className = "com.snapchat.android.LandingPageActivity",
            viewIds = setOf("navigation_host"),
            visibleTexts = listOf("Spotlight"),
            contentDescriptions = listOf("Map", "Chat", "Camera", "Stories", "Spotlight, selected"),
            allNormalizedTokens = setOf("spotlight"),
            selectedDescriptions = setOf("Spotlight, selected")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertTrue("Snapchat Spotlight tab actively selected must be BLOCKED", result.isBlocked)
        assertEquals(ContentCategory.SNAPCHAT_SPOTLIGHT, result.category)
    }

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

    @Test
    fun testSnapchatOperaViewerBlocked() {
        val detector = SnapchatSpotlightDetector()
        val context = ScreenContext(
            packageName = "com.snapchat.android",
            className = "com.snap.opera.OperaActivity",
            viewIds = setOf("opera_page_view", "opera_story_viewer"),
            visibleTexts = listOf("Trending Sound"),
            contentDescriptions = listOf("Watch Spotlight"),
            allNormalizedTokens = setOf("spotlight", "opera")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertTrue("Snapchat Opera player must be blocked", result.isBlocked)
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
    fun testFacebookFeedWithInlineReelsShelfAllowed() {
        val detector = FacebookReelsDetector()
        val context = ScreenContext(
            packageName = "com.facebook.katana",
            className = "com.facebook.katana.FBNewsFeedActivity",
            viewIds = setOf("newsfeed_recycler", "story_tray", "fb_shorts_container"),
            visibleTexts = listOf("News Feed", "Reels and short videos", "Watch more"),
            contentDescriptions = listOf("News Feed tab, selected", "Reels"),
            allNormalizedTokens = setOf("newsfeed", "story", "reels"),
            selectedDescriptions = setOf("News Feed tab, selected")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse("Facebook News Feed with inline Reels shelf must be ALLOWED", result.isBlocked)
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
        assertFalse("FacebookReelsDetector must never inspect Messenger (pure chat app)", detector.canHandle("com.facebook.orca"))
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

    @Test
    fun testYouTubeSearchWithReelRecyclerShelfAllowed() {
        val detector = YouTubeShortsDetector()
        val context = ScreenContext(
            packageName = "com.google.android.youtube",
            className = "com.google.android.apps.youtube.app.search.SearchActivity",
            viewIds = setOf(
                "com.google.android.youtube:id/search_results_editor",
                "com.google.android.youtube:id/search_chip_bar",
                "com.google.android.youtube:id/reel_recycler"
            ),
            visibleTexts = listOf("Physics Wallah Alakh Pandey", "Shorts", "PW Motivation"),
            contentDescriptions = listOf("Search chip"),
            allNormalizedTokens = setOf("physics", "wallah", "alakh", "pandey", "shorts", "pw")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse("YouTube search results with reel_recycler shelf must NEVER be blocked!", result.isBlocked)
    }

    @Test
    fun testInstagramStoryAllowed() {
        val detector = InstagramReelsDetector()
        val context = ScreenContext(
            packageName = "com.instagram.android",
            className = "com.instagram.modal.ModalActivity",
            viewIds = setOf("com.instagram.android:id/reel_viewer", "com.instagram.android:id/reel_viewer_root"),
            visibleTexts = listOf("Friend's 24h Story", "Send message"),
            contentDescriptions = listOf("Story by @bestfriend", "Send message"),
            allNormalizedTokens = setOf("story", "send", "message")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse("Instagram 24-hour Story (reel_viewer) must NOT be blocked as a Reel!", result.isBlocked)
    }

    @Test
    fun testInstagramExploreGridAllowed() {
        val detector = InstagramReelsDetector()
        val context = ScreenContext(
            packageName = "com.instagram.android",
            className = "com.instagram.mainactivity.MainActivity",
            viewIds = setOf(
                "com.instagram.android:id/explore_tab",
                "com.instagram.android:id/clips_item",
                "com.instagram.android:id/clips_item_container"
            ),
            visibleTexts = listOf("Search"),
            contentDescriptions = listOf("Explore tab"),
            allNormalizedTokens = setOf("search", "explore")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse("Instagram Explore Grid thumbnails (clips_item) must NOT be blocked!", result.isBlocked)
    }

    @Test
    fun testBrowserUrlDetectorBlockedSuffixes() {
        val detector = BrowserUrlDetector()
        val suffixes = listOf(".cam", ".sex", ".adult", ".porn", ".xxx")
        for (suffix in suffixes) {
            val domain = "model$suffix"
            val context = ScreenContext(
                packageName = "com.android.chrome",
                viewIds = setOf("url_bar"),
                nodeTextMap = mapOf("url_bar" to "https://$domain/watch"),
                visibleTexts = listOf("https://$domain/watch")
            )
            val result = detector.evaluate(context, defaultConfig)
            assertTrue("Domain ending in $suffix must be blocked by BrowserUrlDetector", result.isBlocked)
            assertEquals(ContentCategory.ADULT_WEBSITE, result.category)
        }
    }

    @Test
    fun testEducationalPlatformsImmunity() {
        val genericDetector = com.zenith.focus.accessibility.detector.GenericShortFormDetector()
        val educationalApps = listOf(
            "xyz.penpencil.physicswala",
            "com.physicswallah",
            "com.unacademyapp",
            "org.khanacademy.android",
            "org.coursera.android",
            "com.udemy.android",
            "com.byjus.thelearningapp",
            "com.vedantu.student",
            "com.doubtnut",
            "com.allen.allenapp",
            "com.testbook.tbapp"
        )
        for (pkg in educationalApps) {
            assertFalse("Generic detector must NEVER inspect $pkg", genericDetector.canHandle(pkg))
            assertTrue("Accessibility service must recognize $pkg as essential utility",
                com.zenith.focus.accessibility.service.ZenithAccessibilityService.isEssentialUtility(pkg)
            )
        }
    }

    @Test
    fun testFocusLockHonorsProtectionConfigToggles() {
        val now = System.currentTimeMillis()
        val activeLockState = LockState(
            isActive = true,
            endTimeMillis = now + 3600000L,
            enabledCategories = emptySet() // Empty means follow user's configured toggles
        )
        val inactiveNuclear = NuclearSession(
            status = NuclearSessionStatus.INACTIVE
        )
        // User explicitly turned off YouTube Shorts blocking
        val configWithShortsDisabled = ProtectionConfig(
            blockYouTubeShorts = false,
            blockInstagramReels = true
        )

        val shortsResult = DetectionResult(
            isBlocked = true,
            confidence = 1.0f,
            category = ContentCategory.YOUTUBE_SHORTS,
            ruleId = "TEST_SHORTS",
            reason = "Test short"
        )

        val reelsResult = DetectionResult(
            isBlocked = true,
            confidence = 1.0f,
            category = ContentCategory.INSTAGRAM_REELS,
            ruleId = "TEST_REELS",
            reason = "Test reel"
        )

        val shouldBlockShorts = NuclearProtectionPolicy.shouldBlock(
            result = shortsResult,
            nuclearSession = inactiveNuclear,
            lockState = activeLockState,
            config = configWithShortsDisabled,
            nowWallClock = now
        )
        assertFalse("Standard Focus Lock must NOT block YouTube Shorts when user toggled it OFF", shouldBlockShorts)

        val shouldBlockReels = NuclearProtectionPolicy.shouldBlock(
            result = reelsResult,
            nuclearSession = inactiveNuclear,
            lockState = activeLockState,
            config = configWithShortsDisabled,
            nowWallClock = now
        )
        assertTrue("Standard Focus Lock MUST block Instagram Reels when user toggled it ON", shouldBlockReels)
    }

    @Test
    fun testYouTubeLongVideoWithControlsHiddenAllowed() {
        val detector = YouTubeShortsDetector()
        // Real-world YouTube long video playback: controls faded out after 2s,
        // Dislike button has "Dislike this video", and action bar has "Remix" button.
        val context = ScreenContext(
            packageName = "com.google.android.youtube",
            className = "com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity",
            viewIds = setOf(
                "com.google.android.youtube:id/watch_while_layout",
                "com.google.android.youtube:id/player_view",
                "com.google.android.youtube:id/video_title",
                "com.google.android.youtube:id/channel_name",
                "com.google.android.youtube:id/subscribe_button",
                "com.google.android.youtube:id/comments_entry_point"
            ),
            visibleTexts = listOf(
                "Complete Kotlin & Android Development Masterclass",
                "Tech Channel",
                "Subscribe",
                "Comments 1.2K"
            ),
            contentDescriptions = listOf(
                "Like this video along with 45K other people",
                "Dislike this video",
                "Remix",
                "Share",
                "Download video",
                "Enter full screen"
            ),
            allNormalizedTokens = setOf("kotlin", "android", "development", "masterclass", "tech", "subscribe", "comments")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse("Long video with faded controls and Dislike/Remix buttons must NEVER be blocked!", result.isBlocked)
    }

    @Test
    fun testYouTubeLongVideoWithShortsCarouselBelowAllowed() {
        val detector = YouTubeShortsDetector()
        // Long video where the suggestions list below contains an inline Shorts shelf with reel_container / reel_layout
        val context = ScreenContext(
            packageName = "com.google.android.youtube",
            className = "com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity",
            viewIds = setOf(
                "com.google.android.youtube:id/watch_while_layout",
                "com.google.android.youtube:id/watch_scroll_view",
                "com.google.android.youtube:id/player_view",
                "com.google.android.youtube:id/reel_container",
                "com.google.android.youtube:id/reel_shelf",
                "com.google.android.youtube:id/reel_layout",
                "com.google.android.youtube:id/video_title"
            ),
            visibleTexts = listOf(
                "Building Scalable Backend Systems with gRPC",
                "Shorts",
                "Quick tip #shorts"
            ),
            contentDescriptions = listOf(
                "Dislike this video",
                "Remix",
                "Expand description"
            ),
            allNormalizedTokens = setOf("building", "scalable", "backend", "grpc", "shorts", "quick", "tip")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse("Long video with recommended Shorts shelf below must NEVER be blocked!", result.isBlocked)
    }

    @Test
    fun testYouTubeFullscreenLandscapeAllowed() {
        val detector = YouTubeShortsDetector()
        val context = ScreenContext(
            packageName = "com.google.android.youtube",
            className = "com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity",
            viewIds = setOf(
                "com.google.android.youtube:id/watch_while_layout",
                "com.google.android.youtube:id/watch_while_coordinator",
                "com.google.android.youtube:id/fullscreen_button"
            ),
            visibleTexts = emptyList(),
            contentDescriptions = listOf("Exit full screen", "Seek to 14 minutes 20 seconds"),
            allNormalizedTokens = setOf("exit", "full", "screen")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse("Fullscreen landscape playback must NEVER be blocked!", result.isBlocked)
    }

    @Test
    fun testGenericShortFormDetectorExcludesYouTube() {
        val genericDetector = GenericShortFormDetector()
        assertFalse(
            "GenericShortFormDetector must NEVER handle YouTube (delegated to specialized detector)",
            genericDetector.canHandle("com.google.android.youtube")
        )
        assertFalse(
            "GenericShortFormDetector must NEVER handle Instagram",
            genericDetector.canHandle("com.instagram.android")
        )
    }

    @Test
    fun testFacebookMessengerImmunity() {
        val detector = FacebookReelsDetector()
        assertFalse(
            "FacebookReelsDetector must NEVER inspect Facebook Messenger (pure messaging app)",
            detector.canHandle("com.facebook.orca")
        )
    }

    @Test
    fun testFacebookStoryWithReelViewerAllowed() {
        val detector = FacebookReelsDetector()
        val context = ScreenContext(
            packageName = "com.facebook.katana",
            className = "com.facebook.katana.activity.FbMainTabActivity",
            viewIds = setOf("com.facebook.katana:id/reel_viewer"),
            visibleTexts = listOf("John Doe's story"),
            contentDescriptions = listOf("Story"),
            allNormalizedTokens = setOf("john", "doe", "story")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse("Facebook 24h photo/status story must NEVER be blocked as a Reel!", result.isBlocked)
    }

    @Test
    fun testInstagramHomeFeedVideoPostAllowed() {
        val detector = InstagramReelsDetector()
        // Feed post with inline video Litho components (clips_video, clips_media_component) on main feed
        val context = ScreenContext(
            packageName = "com.instagram.android",
            className = "com.instagram.mainactivity.MainActivity",
            viewIds = setOf(
                "com.instagram.android:id/feed_recycler",
                "com.instagram.android:id/main_feed",
                "com.instagram.android:id/clips_video_container"
            ),
            visibleTexts = listOf("Friend's Birthday Celebration!"),
            contentDescriptions = listOf("Like", "Comment", "Share"),
            allNormalizedTokens = setOf("friend", "birthday", "celebration")
        )
        val result = detector.evaluate(context, defaultConfig)
        assertFalse("Instagram Home Feed video posts must NEVER be blocked!", result.isBlocked)
    }
}
