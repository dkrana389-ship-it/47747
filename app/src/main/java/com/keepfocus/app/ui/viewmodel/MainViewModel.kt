package com.keepfocus.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepfocus.app.data.local.DailyStatEntity
import com.keepfocus.app.data.local.StatsDao
import com.keepfocus.app.data.repository.UserProfile
import com.keepfocus.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val statsDao: StatsDao
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = userRepository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val todayStr = LocalDate.now().toString()

    val todayStats: StateFlow<DailyStatEntity?> = statsDao.observeStatForDate(todayStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyStatEntity(date = todayStr))

    val weeklyStats: StateFlow<List<DailyStatEntity>> = statsDao.observeRecentStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun copyPhoto(uri: Uri): String {
        return userRepository.copyPhotoToPrivateStorage(uri)
    }

    fun saveProfile(
        fullName: String,
        nickname: String,
        dream: String,
        age: Int,
        profession: String,
        photoPath: String,
        dreamLine: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            userRepository.saveProfile(
                fullName = fullName,
                nickname = nickname,
                dream = dream,
                age = age,
                profession = profession,
                photoPath = photoPath,
                dreamLine = dreamLine
            )
            onComplete()
        }
    }

    fun toggleProtection(active: Boolean) {
        viewModelScope.launch {
            userRepository.setProtectionActive(active)
        }
    }

    fun updateTrackingSettings(threshold: Int, trackInstagram: Boolean, trackYouTube: Boolean) {
        viewModelScope.launch {
            userRepository.updateTrackingSettings(threshold, trackInstagram, trackYouTube)
        }
    }

    fun recordCloseAppClicked() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val existing = statsDao.getStatForDate(today) ?: DailyStatEntity(date = today)
            statsDao.insertOrUpdate(existing.copy(closeAppClicked = existing.closeAppClicked + 1))
        }
    }
}
