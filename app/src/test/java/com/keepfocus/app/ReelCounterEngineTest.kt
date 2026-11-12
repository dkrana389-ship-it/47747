package com.keepfocus.app

import com.keepfocus.app.domain.ReelCounterEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class ReelCounterEngineTest {

    @Test
    fun testDebouncePreventsDoubleCounting() {
        val engine = ReelCounterEngine(debounceMs = 700L, resetTimeoutMs = 20_000L)

        val first = engine.onReelScrolled("com.instagram.android", true, 1000L, 30)
        assertEquals(ReelCounterEngine.EventResult.INCREMENTED, first)
        assertEquals(1, engine.currentSessionCount)

        // Within 700ms debounce window -> ignored
        val second = engine.onReelScrolled("com.instagram.android", true, 1400L, 30)
        assertEquals(ReelCounterEngine.EventResult.IGNORED, second)
        assertEquals(1, engine.currentSessionCount)

        // After 700ms -> counted
        val third = engine.onReelScrolled("com.instagram.android", true, 1800L, 30)
        assertEquals(ReelCounterEngine.EventResult.INCREMENTED, third)
        assertEquals(2, engine.currentSessionCount)
    }

    @Test
    fun testResetAfter20SecondsInactivity() {
        val engine = ReelCounterEngine(debounceMs = 700L, resetTimeoutMs = 20_000L)
        engine.onReelScrolled("com.google.android.youtube", true, 1000L, 30)
        engine.onReelScrolled("com.google.android.youtube", true, 2000L, 30)
        assertEquals(2, engine.currentSessionCount)

        // 21 seconds later -> resets to 0 then increments to 1
        engine.onReelScrolled("com.google.android.youtube", true, 23_500L, 30)
        assertEquals(1, engine.currentSessionCount)
    }

    @Test
    fun testThresholdTriggersAndResets() {
        val engine = ReelCounterEngine(debounceMs = 700L, resetTimeoutMs = 20_000L)
        var time = 1000L
        for (i in 1..29) {
            val res = engine.onReelScrolled("com.instagram.android", true, time, 30)
            assertEquals(ReelCounterEngine.EventResult.INCREMENTED, res)
            time += 1000L
        }
        val trigger = engine.onReelScrolled("com.instagram.android", true, time, 30)
        assertEquals(ReelCounterEngine.EventResult.THRESHOLD_REACHED, trigger)
        assertEquals(0, engine.currentSessionCount)
    }
}
