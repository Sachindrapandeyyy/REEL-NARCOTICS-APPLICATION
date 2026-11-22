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

        while (queue.isNotEmpty() && visitedCount < MAX_NODES) {
            val (node, depth) = queue.poll() ?: break
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
                val normalized = TextNormalizer.normalize(text)
                allNormalizedTokens.addAll(TextNormalizer.extractTokens(normalized))
            }

            // Extract Content Description
            val desc = runCatching { node.contentDescription?.toString() }.getOrNull()
            if (!desc.isNullOrBlank()) {
                contentDescriptions.add(desc)
                if (isSelected) {
                    selectedDescriptions.add(desc)
                }
                val normalized = TextNormalizer.normalize(desc)
                allNormalizedTokens.addAll(TextNormalizer.extractTokens(normalized))
            }

            // Inspect potential bottom navigation tab bounds for Shorts, Reels, or Spotlight
            if (detectedTabBounds == null) {
                val t = text?.trim()?.lowercase(java.util.Locale.US) ?: ""
                val d = desc?.trim()?.lowercase(java.util.Locale.US) ?: ""
                val v = entryName.lowercase(java.util.Locale.US)
                val isShortsOrReelsTab = (t == "shorts" || t == "reels" || t == "spotlight" ||
                        d.startsWith("shorts") || d.startsWith("reels") || d.startsWith("spotlight") ||
                        v.contains("shorts_tab") || v.contains("reels_tab") || v.contains("spotlight_tab"))
                if (isShortsOrReelsTab) {
                    val rect = android.graphics.Rect()
                    runCatching { node.getBoundsInScreen(rect) }
                    if (rect.width() > 0 && rect.height() > 0 && rect.top > 0) {
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
