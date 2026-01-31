package com.cookstemma.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

// AppCompat is required for per-app language preferences on Android < 13

enum class AppLanguage(
    val code: String,
    val displayName: String
) {
    ENGLISH("en", "English"),
    KOREAN("ko", "한국어"),
    JAPANESE("ja", "日本語"),
    CHINESE("zh-CN", "简体中文"),
    SPANISH("es", "Español"),
    FRENCH("fr", "Français"),
    GERMAN("de", "Deutsch"),
    RUSSIAN("ru", "Русский"),
    DUTCH("nl", "Nederlands");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code == code } ?: ENGLISH
        }

        fun fromLocale(locale: Locale): AppLanguage {
            val languageTag = locale.toLanguageTag()
            // Handle Chinese specifically
            if (languageTag.startsWith("zh")) {
                return CHINESE
            }
            return entries.find { it.code == locale.language } ?: ENGLISH
        }
    }
}

@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getCurrentLanguage(): AppLanguage {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        return if (currentLocales.isEmpty) {
            // Use system locale
            AppLanguage.fromLocale(Locale.getDefault())
        } else {
            val locale = currentLocales[0] ?: Locale.getDefault()
            AppLanguage.fromLocale(locale)
        }
    }

    fun setLanguage(language: AppLanguage) {
        val localeList = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun getAllLanguages(): List<AppLanguage> = AppLanguage.entries
}
