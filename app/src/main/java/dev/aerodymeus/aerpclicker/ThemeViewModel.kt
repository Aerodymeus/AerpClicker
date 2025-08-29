package dev.aerodymeus.aerpclicker

// Beispiel für ein separates ThemeViewModel.kt
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = getApplication<Application>().applicationContext.gameDataStore

    // Flow, um die aktuelle Theme-Einstellung aus DataStore zu lesen
    val currentThemeSetting: StateFlow<ThemeSetting> = dataStore.data
        .map { preferences ->
            when (preferences[AppThemeKeys.SELECTED_THEME]) {
                "LIGHT" -> ThemeSetting.LIGHT
                "DARK" -> ThemeSetting.DARK
                else -> ThemeSetting.SYSTEM // Standard ist System
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeSetting.SYSTEM // Anfangswert, bis DataStore geladen hat
        )

    fun setThemeSetting(setting: ThemeSetting) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[AppThemeKeys.SELECTED_THEME] = setting.name // Speichere den Enum-Namen als String
            }
        }
    }
}