package com.keepfocus.app.data.repository

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "keep_focus_prefs")

data class UserProfile(
    val fullName: String = "",
    val nickname: String = "",
    val dream: String = "",
    val age: Int = 20,
    val profession: String = "",
    val photoPath: String = "",
    val dreamLine: String = "",
    val onboardingCompleted: Boolean = false,
    val threshold: Int = 30,
    val protectActive: Boolean = true,
    val trackInstagram: Boolean = true,
    val trackYouTube: Boolean = true
)

@Singleton
class UserRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val FULL_NAME = stringPreferencesKey("full_name")
        val NICKNAME = stringPreferencesKey("nickname")
        val DREAM = stringPreferencesKey("dream")
        val AGE = intPreferencesKey("age")
        val PROFESSION = stringPreferencesKey("profession")
        val PHOTO_PATH = stringPreferencesKey("photo_path")
        val DREAM_LINE = stringPreferencesKey("dream_line")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THRESHOLD = intPreferencesKey("threshold")
        val PROTECT_ACTIVE = booleanPreferencesKey("protect_active")
        val TRACK_INSTAGRAM = booleanPreferencesKey("track_instagram")
        val TRACK_YOUTUBE = booleanPreferencesKey("track_youtube")
    }

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        UserProfile(
            fullName = prefs[Keys.FULL_NAME] ?: "",
            nickname = prefs[Keys.NICKNAME] ?: "",
            dream = prefs[Keys.DREAM] ?: "",
            age = prefs[Keys.AGE] ?: 20,
            profession = prefs[Keys.PROFESSION] ?: "",
            photoPath = prefs[Keys.PHOTO_PATH] ?: "",
            dreamLine = prefs[Keys.DREAM_LINE] ?: "",
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            threshold = prefs[Keys.THRESHOLD] ?: 30,
            protectActive = prefs[Keys.PROTECT_ACTIVE] ?: true,
            trackInstagram = prefs[Keys.TRACK_INSTAGRAM] ?: true,
            trackYouTube = prefs[Keys.TRACK_YOUTUBE] ?: true
        )
    }

    suspend fun copyPhotoToPrivateStorage(sourceUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, "profile")
            if (!dir.exists()) dir.mkdirs()
            val destFile = File(dir, "avatar_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun saveProfile(
        fullName: String,
        nickname: String,
        dream: String,
        age: Int,
        profession: String,
        photoPath: String,
        dreamLine: String
    ) {
        val effectiveNickname = nickname.trim().ifEmpty { fullName.trim().split(" ").firstOrNull() ?: "Friend" }
        val generatedDreamLine = if (dreamLine.isBlank()) {
            "$effectiveNickname, remember your goal: ${dream.trim()}"
        } else {
            dreamLine.trim()
        }

        context.dataStore.edit { prefs ->
            prefs[Keys.FULL_NAME] = fullName.trim()
            prefs[Keys.NICKNAME] = effectiveNickname
            prefs[Keys.DREAM] = dream.trim()
            prefs[Keys.AGE] = age
            prefs[Keys.PROFESSION] = profession.trim()
            prefs[Keys.PHOTO_PATH] = photoPath
            prefs[Keys.DREAM_LINE] = generatedDreamLine
            prefs[Keys.ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun setProtectionActive(active: Boolean) {
        context.dataStore.edit { it[Keys.PROTECT_ACTIVE] = active }
    }

    suspend fun updateTrackingSettings(threshold: Int, trackInstagram: Boolean, trackYouTube: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THRESHOLD] = threshold
            prefs[Keys.TRACK_INSTAGRAM] = trackInstagram
            prefs[Keys.TRACK_YOUTUBE] = trackYouTube
        }
    }
}
