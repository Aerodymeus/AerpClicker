package dev.aerodymeus.aerpclicker.ui

import androidx.annotation.StringRes
import dev.aerodymeus.aerpclicker.R
import java.util.Locale

// Datenklasse, die eine Sprache repräsentiert
data class Language(
    val code: String, // z.B. "en", "de"
    @StringRes val nameResId: Int // Ressourcen-ID für den Namen, z.B. R.string.language_english
)

// Enum, um die unterstützten Sprachen klar zu definieren
enum class LanguageSetting {
    GERMAN, ENGLISH, SPANISH, FRENCH, ITALIAN, DUTCH, FINNISH;

    fun toLanguage(): Language {
        return when (this) {
            GERMAN -> Language("de", R.string.language_german)
            ENGLISH -> Language("en", R.string.language_english)
            SPANISH -> Language("es", R.string.language_spanish)
            FRENCH -> Language("fr", R.string.language_french)
            ITALIAN -> Language("it", R.string.language_italian)
            DUTCH -> Language("nl", R.string.language_dutch)
            FINNISH -> Language("fi", R.string.language_finnish)
        }
    }

    companion object {
        fun fromCode(code: String): LanguageSetting {
            return entries.find { it.toLanguage().code == code } ?: ENGLISH // Englisch als Standard
        }
    }
}