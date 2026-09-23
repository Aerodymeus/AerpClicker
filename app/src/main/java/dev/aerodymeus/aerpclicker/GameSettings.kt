package dev.aerodymeus.aerpclicker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// DataStore-Instanz für die App
val Context.gameDataStore: DataStore<Preferences> by preferencesDataStore(name = "aerp_clicker_gamestate")

object GameStateKeys {
    val INTERNAL_SCORE = doublePreferencesKey("internal_score")

    val CLICK_BOOST_LEVEL = intPreferencesKey("click_boost_level")

    val IS_AUTO_CLICKER_BOUGHT = booleanPreferencesKey("is_auto_clicker_bought")
    val AUTO_CLICKER_INTERVAL_UPGRADE_LEVEL = intPreferencesKey("auto_clicker_interval_upgrade_level")

    val IS_PASSIVE_GENERATOR_BOUGHT = booleanPreferencesKey("is_passive_generator_bought")
    val FACTORY_PRODUCTION_UPGRADE_LEVEL = intPreferencesKey("factory_production_upgrade_level")
    val FACTORY_INTERVAL_UPGRADE_LEVEL = intPreferencesKey("factory_interval_upgrade_level")

    val LAST_KNOWN_VERSION_NAME = stringPreferencesKey("last_known_version_name")
    val PURCHASED_UPGRADES = stringPreferencesKey("purchased_upgrades")
}

object AppThemeKeys {
    val SELECTED_THEME =
        stringPreferencesKey("selected_theme")
}
