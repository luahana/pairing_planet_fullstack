package com.cookstemma.app.data.api

import androidx.appcompat.app.AppCompatDelegate
import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Get current app locale
        val locales = AppCompatDelegate.getApplicationLocales()
        val languageCode = if (locales.isEmpty) {
            Locale.getDefault().language
        } else {
            locales[0]?.language ?: Locale.getDefault().language
        }

        val newRequest = request.newBuilder()
            .addHeader("Accept-Language", languageCode)
            .build()

        return chain.proceed(newRequest)
    }
}
