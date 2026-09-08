# Zenith Focus Detection Engine ??

The Zenith Focus Detection Engine implements a resilient, multi-signal heuristic evaluation architecture. Rather than relying on fragile single-token text matching, it evaluates UI node structural layouts, resource IDs, navigation state, and content descriptions.

---

## 1. Hierarchy Traversal & Performance Safeguards

Accessibility event floods can cause CPU spikes and battery drain if unmanaged. Zenith Focus deploys four defensive performance layers:

1. **Strategic Event Filtering**:
   - Only listens to TYPE_WINDOW_STATE_CHANGED, TYPE_WINDOW_CONTENT_CHANGED, and TYPE_VIEW_SCROLLED.
   - Rejects own package (com.zenith.focus), Android system UI, and home launchers immediately.
2. **Debouncing**:
   - Frequent TYPE_WINDOW_CONTENT_CHANGED events are rate-limited to at most once per 250ms per package.
3. **Bounded Breadth-First Traversal**:
   - Maximum traversal depth: 10
   - Maximum inspected nodes: 150
   - Automatically releases AccessibilityNodeInfo references to prevent memory leaks.
4. **Overlay Cooldown**:
   - Enforces a 800ms cooldown window after the user dismisses a block screen ("GO BACK") so the dying surface does not trigger an instant re-block loop.

---

## 2. Multi-Signal Scoring System

Each detector calculates a cumulative confidence score in the range [0.0, 1.0]:

| Confidence Score | Classification | Action Taken |
|---|---|---|
| **>= 0.70** | HIGH CONFIDENCE | **CONFIRMED BLOCK** (Show overlay + record event) |
| **0.55 - 0.69** | MEDIUM CONFIDENCE | **BLOCK** if Strict Mode enabled, otherwise ALLOW |
| **< 0.55** | LOW CONFIDENCE | **ALLOW** (Fail-safe prevention of false positives) |

---

## 3. Supported Detectors

### YouTube Shorts Detector (YouTubeShortsDetector)
- **Target Package**: com.google.android.youtube
- **Exclusions (Allowed)**:
  - watch_while_layout + player_fragment / 	ime_bar -> Standard long-form video player
  - search_results_editor -> YouTube search results
  - Subscriptions / Library tabs
- **Signals (Shorts)**:
  - eel_recycler / eel_player_page_view (+0.65)
  - pivot_bar Shorts tab selected (+0.50)
  - "Shorts video player" content description (+0.40)
  - "Remix" / "Use sound" action tokens (+0.35)

### Instagram Reels Detector (InstagramReelsDetector)
- **Target Package**: com.instagram.android
- **Exclusions (Allowed)**:
  - direct_thread_feed / ow_thread_composer -> Direct Messages
  - profile_tab -> User profile page
  - Standard feed photos and carousels
- **Signals (Reels)**:
  - clips_viewer_view_pager / clips_video_container (+0.70)
  - Reels tab selected in bottom navigation (+0.60)
  - "Reel by..." / "Use audio" / "Remix this reel" tokens (+0.35)

### Snapchat Spotlight Detector (SnapchatSpotlightDetector)
- **Target Package**: com.snapchat.android
- **Exclusions (Allowed)**: Chat inbox, camera viewfinder, friend stories.
- **Signals (Spotlight)**: spotlight_container (+0.70), Spotlight tab selected (+0.50).

### Facebook Reels Detector (FacebookReelsDetector)
- **Target Package**: com.facebook.katana
- **Signals (Reels)**: eel_fullscreen_view, b_shorts_container (+0.75), "Reels and short videos" header (+0.40).

### Browser URL Detector (BrowserUrlDetector)
- **Target Browsers**: Chrome, Firefox, Edge, Brave, Opera, Samsung Internet, DuckDuckGo, Vivaldi.
- **Inspection**: Extracts address bar text from resource IDs (url_bar, search_box_text, location_bar_edit_text).
- **Domain Matching**: Normalizes host (strips www., m., protocols), checks against offline list of 500+ top adult domains, and detects explicit TLDs (.xxx, .porn, .adult, .cam, .sex).

### Adult Content Detector (AdultContentDetector)
- **Text Normalization**:
  - Unicode NFKD decomposition (stripping diacritics)
  - Leetspeak decoding (p0rn -> porn, s3x -> sex, @dult -> dult)
  - Obfuscation decoders (stripping . or _ inside spaced letters p.o.r.n -> porn)
- **Scunthorpe Guard**: Whitelist tokens (documentary, iology, medical, natomy, education, 	herapy) ensure harmless educational or scientific materials are never blocked.
