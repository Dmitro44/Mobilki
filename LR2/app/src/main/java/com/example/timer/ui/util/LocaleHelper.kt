package com.example.timer.ui.util

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.timer.data.local.preferences.Language
import java.util.Locale

object LocaleHelper {
    fun setLocale(context: Context, language: Language) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as LocaleManager
            localeManager.applicationLocales = LocaleList.forLanguageTags(language.code)
        } else {
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(language.code)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    fun wrapContext(context: Context, language: Language): Context {
        val locale = Locale.forLanguageTag(language.code)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            config.setLocales(localeList)
        } else {
            config.setLocale(locale)
        }
        
        return context.createConfigurationContext(config)
    }

    fun getCurrentLocaleCode(context: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as LocaleManager
            val locales = localeManager.applicationLocales.toLanguageTags()
            return if (locales.isEmpty()) Locale.getDefault().language else locales
        } else {
            val locales = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            return if (locales.isEmpty()) Locale.getDefault().language else locales
        }
    }
}
