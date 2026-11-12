package com.keepfocus.app.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.keepfocus.app.data.local.DailyStatEntity
import com.keepfocus.app.data.local.StatsDao
import com.keepfocus.app.data.repository.UserProfile
import com.keepfocus.app.data.repository.UserRepository
import com.keepfocus.app.domain.ReelCounterEngine
import com.keepfocus.app.ui.popup.MotivationActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class FocusAccessibilityService : AccessibilityService() {

    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var statsDao: StatsDao

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val counterEngine = ReelCounterEngine(debounceMs = 700L, resetTimeoutMs = 20_000L)
    @Volatile private var cachedProfile = UserProfile()

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                counterEngine.onScreenOff()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            userRepository.userProfileFlow.collectLatest { profile ->
                cachedProfile = profile
            }
        }
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenOffReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenOffReceiver, filter)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val profile = cachedProfile
        if (!profile.protectActive) return

        val pkg = event.packageName?.toString() ?: return
        val now = System.currentTimeMillis()

        val isInsta = pkg == "com.instagram.android" && profile.trackInstagram
        val isYt = pkg == "com.google.android.youtube" && profile.trackYouTube

        if (!isInsta && !isYt) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                counterEngine.onAppSwitchedAway(pkg, now)
            }
            return
        }

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            val inShortFeed = isInsideReelsOrShorts(pkg)
            val result = counterEngine.onReelScrolled(
                packageName = pkg,
                isInShortVideoFeed = inShortFeed,
                nowMs = now,
                threshold = profile.threshold
            )

            if (result == ReelCounterEngine.EventResult.INCREMENTED ||
                result == ReelCounterEngine.EventResult.THRESHOLD_REACHED
            ) {
                val triggeredPopup = (result == ReelCounterEngine.EventResult.THRESHOLD_REACHED)
                recordReelIncrement(triggeredPopup)
                if (triggeredPopup) {
                    launchMotivationPopup(profile.threshold)
                }
            }
        }
    }

    private fun isInsideReelsOrShorts(packageName: String): Boolean {
        val root = try {
            rootInActiveWindow
        } catch (e: Exception) {
            null
        } ?: return false

        return try {
            if (packageName == "com.instagram.android") {
                hasNodeMatching(
                    root,
                    viewIds = listOf(
                        "com.instagram.android:id/clips_viewer_view_pager",
                        "com.instagram.android:id/reel_viewer_root"
                    ),
                    descKeywords = listOf("Reel", "Reels")
                )
            } else {
                hasNodeMatching(
                    root,
                    viewIds = listOf(
                        "com.google.android.youtube:id/reel_recycler",
                        "com.google.android.youtube:id/shorts_player_container"
                    ),
                    descKeywords = listOf("Shorts", "Like this video", "Dislike this video")
                )
            }
        } catch (e: Exception) {
            false
        } finally {
            try {
                @Suppress("DEPRECATION")
                root.recycle()
            } catch (_: Exception) {
            }
        }
    }

    private fun hasNodeMatching(
        node: AccessibilityNodeInfo?,
        viewIds: List<String>,
        descKeywords: List<String>,
        depth: Int = 0
    ): Boolean {
        if (node == null || depth > 8) return false

        val id = node.viewIdResourceName
        if (id != null && viewIds.any { id.contains(it, ignoreCase = true) }) {
            return true
        }

        val desc = node.contentDescription?.toString()
        if (desc != null && descKeywords.any { desc.contains(it, ignoreCase = true) }) {
            return true
        }

        for (i in 0 until node.childCount) {
            val child = try {
                node.getChild(i)
            } catch (e: Exception) {
                null
            } ?: continue

            val found = hasNodeMatching(child, viewIds, descKeywords, depth + 1)
            try {
                @Suppress("DEPRECATION")
                child.recycle()
            } catch (_: Exception) {
            }
            if (found) return true
        }
        return false
    }

    private fun recordReelIncrement(popupShown: Boolean) {
        serviceScope.launch {
            val today = LocalDate.now().toString()
            val current = statsDao.getStatForDate(today) ?: DailyStatEntity(date = today)
            statsDao.insertOrUpdate(
                current.copy(
                    reelsWatched = current.reelsWatched + 1,
                    popupsShown = current.popupsShown + if (popupShown) 1 else 0
                )
            )
        }
    }

    private fun launchMotivationPopup(sessionReels: Int) {
        val intent = Intent(this, MotivationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("session_reels", sessionReels)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenOffReceiver)
        } catch (_: Exception) {
        }
        serviceScope.cancel()
    }
}
