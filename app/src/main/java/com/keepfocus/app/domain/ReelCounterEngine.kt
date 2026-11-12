package com.keepfocus.app.domain

class ReelCounterEngine(
    private val debounceMs: Long = 700L,
    private val resetTimeoutMs: Long = 20_000L
) {
    var currentSessionCount: Int = 0
        private set

    private var lastCountedTimestampMs: Long = 0L
    private var lastActiveInReelsTimestampMs: Long = 0L
    private var activePackage: String? = null

    enum class EventResult {
        IGNORED,
        INCREMENTED,
        THRESHOLD_REACHED
    }

    fun onReelScrolled(
        packageName: String,
        isInShortVideoFeed: Boolean,
        nowMs: Long,
        threshold: Int
    ): EventResult {
        if (!isInShortVideoFeed) {
            checkResetTimeout(nowMs)
            return EventResult.IGNORED
        }

        if (activePackage != null && activePackage != packageName) {
            reset()
        }

        if (lastActiveInReelsTimestampMs > 0L && (nowMs - lastActiveInReelsTimestampMs) > resetTimeoutMs) {
            reset()
        }

        activePackage = packageName
        lastActiveInReelsTimestampMs = nowMs

        if (lastCountedTimestampMs > 0L && (nowMs - lastCountedTimestampMs) < debounceMs) {
            return EventResult.IGNORED
        }

        lastCountedTimestampMs = nowMs
        currentSessionCount++

        return if (currentSessionCount >= threshold) {
            currentSessionCount = 0
            EventResult.THRESHOLD_REACHED
        } else {
            EventResult.INCREMENTED
        }
    }

    fun onAppSwitchedAway(newPackage: String, nowMs: Long) {
        if (activePackage != null && newPackage != activePackage) {
            reset()
        }
    }

    fun onScreenOff() {
        reset()
    }

    fun checkResetTimeout(nowMs: Long) {
        if (lastActiveInReelsTimestampMs > 0L && (nowMs - lastActiveInReelsTimestampMs) > resetTimeoutMs) {
            reset()
        }
    }

    fun reset() {
        currentSessionCount = 0
        lastCountedTimestampMs = 0L
        lastActiveInReelsTimestampMs = 0L
        activePackage = null
    }
}
