package com.zenith.focus.accessibility.analyzer

import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import java.util.ArrayDeque

object HierarchyTraverser {
    private const val MAX_DEPTH = 30
    private const val MAX_NODES = 200

    fun inspect(root: AccessibilityNodeInfo?): ScreenContext {
        if (root == null) {
            return ScreenContext(
                packageName = "",
                className = "",
                viewIds = emptySet(),
                visibleTexts = emptyList(),
                contentDescriptions = emptyList(),
                allNormalizedTokens = emptySet(),
                selectedTexts = emptySet(),
                selectedDescriptions = emptySet()
            )
        }

        val packageName = root.packageName?.toString() ?: ""
        val className = root.className?.toString() ?: ""

        val classNames = mutableSetOf<String>()
        if (className.isNotBlank()) {
            classNames.add(className)
        }

        val viewIds = mutableSetOf<String>()
        val visibleTexts = mutableListOf<String>()
        val contentDescriptions = mutableListOf<String>()
        val allNormalizedTokens = mutableSetOf<String>()
        val selectedTexts = mutableSetOf<String>()
        val selectedDescriptions = mutableSetOf<String>()
        val nodeTextMap = mutableMapOf<String, String>()
        var detectedTabBounds: android.graphics.Rect? = null

        val queue = ArrayDeque<Pair<AccessibilityNodeInfo, Int>>()
        queue.add(root to 0)
        var visitedCount = 0

        val lowerRootPkg = packageName.lowercase(java.util.Locale.US)
        val isRootLauncher = com.zenith.focus.accessibility.detector.TamperDetectionEngine.isLauncherPackage(lowerRootPkg)
        val isShortFormHost = lowerRootPkg.contains("youtube") || lowerRootPkg.contains("instagram") ||
            lowerRootPkg.contains("facebook") || lowerRootPkg.contains("snapchat")
        val rootRect = android.graphics.Rect().also { runCatching { root.getBoundsInScreen(it) } }

        while (queue.isNotEmpty() && visitedCount < MAX_NODES) {
            val (node, depth) = queue.poll() ?: break

            if (node !== root) {
                val isVisible = runCatching { node.isVisibleToUser }.getOrDefault(true)
                if (!isVisible) {
                    if (Build.VERSION.SDK_INT < 34) {
                        @Suppress("DEPRECATION")
                        runCatching { node.recycle() }
                    }
                    continue
                }

                // Isolate foreign embedded widgets on launchers (e.g., Google Search bar widget shortcut_btn)
                if (isRootLauncher) {
                    val nodePkg = runCatching { node.packageName?.toString()?.lowercase(java.util.Locale.US) }.getOrNull()
                    if (!nodePkg.isNullOrBlank() && nodePkg != lowerRootPkg) {
                        if (Build.VERSION.SDK_INT < 34) {
                            @Suppress("DEPRECATION")
                            runCatching { node.recycle() }
                        }
                        continue
                    }
                }
            }

            visitedCount++

            val nodeClass = runCatching { node.className?.toString() }.getOrNull()
            if (!nodeClass.isNullOrBlank()) {
                classNames.add(nodeClass)
            }

            val isChecked = runCatching { node.isChecked }.getOrDefault(false)
            val isSelected = runCatching { node.isSelected }.getOrDefault(false) || isChecked

            // Extract View ID
            val viewId = runCatching { node.viewIdResourceName }.getOrNull()
            var entryName = ""
            if (!viewId.isNullOrBlank()) {
                viewIds.add(viewId)
                entryName = viewId.substringAfterLast(":id/")
                if (entryName.isNotBlank()) {
                    viewIds.add(entryName)
                }
            }

            // Extract Visible Text
            val text = runCatching { node.text?.toString() }.getOrNull()
            if (!text.isNullOrBlank()) {
                visibleTexts.add(text)
                if (isSelected) {
                    selectedTexts.add(text)
                }
                if (!viewId.isNullOrBlank()) {
                    nodeTextMap[viewId] = text
                    if (entryName.isNotBlank()) {
                        nodeTextMap[entryName] = text
                    }
                }
                allNormalizedTokens.addAll(TextNormalizer.extractTokens(text))
            }

            // Extract Content Description
            val desc = runCatching { node.contentDescription?.toString() }.getOrNull()
            if (!desc.isNullOrBlank()) {
                contentDescriptions.add(desc)
                if (isSelected) {
                    selectedDescriptions.add(desc)
                }
                allNormalizedTokens.addAll(TextNormalizer.extractTokens(desc))
            }

            // Inspect bottom navigation tab bounds ONLY for short-form social apps in portrait normal-size windows
            val isPortraitFullWindow = rootRect.width() <= 0 || rootRect.height() <= 0 ||
                (rootRect.height() > rootRect.width() && rootRect.height() >= 600)
            if (detectedTabBounds == null && isShortFormHost && isPortraitFullWindow) {
                val t = text?.trim()?.lowercase(java.util.Locale.US) ?: ""
                val d = desc?.trim()?.lowercase(java.util.Locale.US) ?: ""
                val v = entryName.lowercase(java.util.Locale.US)
                val isShortsOrReelsTab = (t == "shorts" || t == "reels" || t == "spotlight" ||
                        d == "shorts" || d.startsWith("shorts,") || d.startsWith("shorts tab") ||
                        d == "reels" || d.startsWith("reels,") || d.startsWith("reels tab") ||
                        d == "spotlight" || d.startsWith("spotlight,") || d.startsWith("spotlight tab") ||
                        v.contains("shorts_tab") || v.contains("reels_tab") || v.contains("spotlight_tab"))
                if (isShortsOrReelsTab) {
                    val rect = android.graphics.Rect()
                    runCatching { node.getBoundsInScreen(rect) }
                    val inBottomNavRegion = if (rootRect.height() > 0 && rootRect.width() > 0) {
                        val bottomThreshold = rootRect.top + (rootRect.height() * 0.72f).toInt()
                        rect.top >= bottomThreshold &&
                            rect.width() <= (rootRect.width() * 0.45f).toInt() &&
                            rect.height() <= (rootRect.height() * 0.16f).toInt()
                    } else {
                        true
                    }
                    if (inBottomNavRegion && rect.width() > 0 && rect.height() > 0 && rect.top > 0) {
                        detectedTabBounds = rect
                    }
                }
            }

            // Queue children if within max depth
            if (depth < MAX_DEPTH) {
                val childCount = runCatching { node.childCount }.getOrDefault(0)
                for (i in 0 until childCount) {
                    val child = runCatching { node.getChild(i) }.getOrNull()
                    if (child != null) {
                        queue.add(child to (depth + 1))
                    }
                }
            }

            // Recycle intermediate child nodes to prevent IPC handle leaks on API < 34
            if (Build.VERSION.SDK_INT < 34 && node !== root) {
                @Suppress("DEPRECATION")
                runCatching { node.recycle() }
            }
        }

        // Drain and recycle any remaining uninspected nodes if loop terminated early
        if (Build.VERSION.SDK_INT < 34) {
            while (queue.isNotEmpty()) {
                val (leftover, _) = queue.poll() ?: break
                if (leftover !== root) {
                    @Suppress("DEPRECATION")
                    runCatching { leftover.recycle() }
                }
            }
        }

        return ScreenContext(
            packageName = packageName,
            className = className,
            classNames = classNames,
            viewIds = viewIds,
            visibleTexts = visibleTexts,
            contentDescriptions = contentDescriptions,
            allNormalizedTokens = allNormalizedTokens,
            selectedTexts = selectedTexts,
            selectedDescriptions = selectedDescriptions,
            nodeTextMap = nodeTextMap,
            shortFormTabBounds = detectedTabBounds
        )
    }
}
