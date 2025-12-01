package dev.aerodymeus.aerpclicker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.edit
import dev.aerodymeus.aerpclicker.ui.ThemeSetting
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.aerodymeus.aerpclicker.ui.LanguageSetting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

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

    // NEU: StateFlow für die Sprache
    private val _languageSetting = MutableStateFlow(getCurrentAppLanguage())
    val languageSetting: StateFlow<LanguageSetting> = _languageSetting.asStateFlow()

    // NEU: Funktion, um die Sprache zu ändern
    fun setLanguage(languageSetting: LanguageSetting) {
        viewModelScope.launch {
            // Speichere die Einstellung im DataStore
            dataStore.edit { preferences ->
                preferences[ThemeStateKeys.APP_LANGUAGE] = languageSetting.toLanguage().code
            }
            // Aktualisiere die App-Sprache
            val locale = LocaleListCompat.forLanguageTags(languageSetting.toLanguage().code)
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(locale)

            // Aktualisiere den internen State
            _languageSetting.value = languageSetting
        }
    }

    // NEU: Hilfsfunktion, um die aktuelle Sprache zu ermitteln
    private fun getCurrentAppLanguage(): LanguageSetting {
        val currentLocale = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().get(0)
        return LanguageSetting.fromCode(currentLocale?.language ?: "en")
    }

    // NEU: Lade die Sprache aus dem DataStore
    private fun loadLanguageSetting() {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            val savedLanguageCode = prefs[ThemeStateKeys.APP_LANGUAGE]
            if (savedLanguageCode != null) {
                _languageSetting.value = LanguageSetting.fromCode(savedLanguageCode)
            }
        }
    }

    init {
        //loadThemeSetting()
        loadLanguageSetting() // Lade die Sprache beim Start
    }

}


// Füge den neuen Key in ThemeStateKeys hinzu
object ThemeStateKeys {
    val THEME_SETTING = stringPreferencesKey("theme_setting")
    val APP_LANGUAGE = stringPreferencesKey("app_language") // NEU
}