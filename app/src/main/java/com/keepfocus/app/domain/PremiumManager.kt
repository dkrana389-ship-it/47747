package com.keepfocus.app.domain

interface PremiumManager {
    fun isPremium(): Boolean
    fun canUseCustomThreshold(): Boolean
    fun canTrackMultipleApps(): Boolean
}

class FreeVersionManager : PremiumManager {
    override fun isPremium(): Boolean = true
    override fun canUseCustomThreshold(): Boolean = true
    override fun canTrackMultipleApps(): Boolean = true
}
