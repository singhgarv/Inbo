package com.inbo.app.data.repository

import com.inbo.app.data.local.InboDatabaseHelper
import com.inbo.app.data.local.SettingsDao
import com.inbo.app.model.InboSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Single source of truth repository managing Inbo user settings and preferences.
 * Exposes a reactive StateFlow<InboSettings> to the UI and persists setting changes
 * asynchronously via SettingsDao.
 */
class SettingsRepository(
    private val settingsDao: SettingsDao,
    private val scope: CoroutineScope
) {
    private val _settings = MutableStateFlow(InboSettings())
    val settings: StateFlow<InboSettings> = _settings.asStateFlow()

    init {
        scope.launch {
            _settings.value = settingsDao.getSettings()
        }
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(darkModeEnabled = enabled)
        scope.launch {
            settingsDao.setSetting(InboDatabaseHelper.KEY_DARK_MODE, enabled.toString())
        }
    }

    fun setQuietHoursEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(quietHoursEnabled = enabled)
        scope.launch {
            settingsDao.setSetting(InboDatabaseHelper.KEY_QUIET_HOURS, enabled.toString())
        }
    }

    fun setMorningDigestEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(morningDigestEnabled = enabled)
        scope.launch {
            settingsDao.setSetting(InboDatabaseHelper.KEY_MORNING_DIGEST, enabled.toString())
        }
    }

    fun setTaskRemindersEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(taskRemindersEnabled = enabled)
        scope.launch {
            settingsDao.setSetting(InboDatabaseHelper.KEY_TASK_REMINDERS, enabled.toString())
        }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(hapticFeedbackEnabled = enabled)
        scope.launch {
            settingsDao.setSetting(InboDatabaseHelper.KEY_HAPTIC_FEEDBACK, enabled.toString())
        }
    }

    fun setAutoArchiveEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(autoArchiveEnabled = enabled)
        scope.launch {
            settingsDao.setSetting(InboDatabaseHelper.KEY_AUTO_ARCHIVE, enabled.toString())
        }
    }

    fun setDefaultLaunchScreen(screen: String) {
        _settings.value = _settings.value.copy(defaultLaunchScreen = screen)
        scope.launch {
            settingsDao.setSetting(InboDatabaseHelper.KEY_DEFAULT_LAUNCH_SCREEN, screen)
        }
    }
}
