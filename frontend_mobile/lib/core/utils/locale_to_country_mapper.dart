import 'dart:io';

/// Utility class for mapping device locales to ISO country codes
/// Used for cooking style selection in recipe creation
class LocaleToCountryMapper {
  /// Map of language codes to their default country codes
  /// Used when device locale doesn't include a country code
  static const Map<String, String> languageToCountry = {
    'ko': 'KR',
    'ja': 'JP',
    'zh': 'CN',
    'en': 'US',
    'fr': 'FR',
    'de': 'DE',
    'es': 'ES',
    'it': 'IT',
    'pt': 'BR',
    'ru': 'RU',
    'el': 'GR',
    'th': 'TH',
    'vi': 'VN',
    'hi': 'IN',
    'ar': 'SA',
    'tr': 'TR',
    'nl': 'NL',
    'pl': 'PL',
    'sv': 'SE',
    'da': 'DK',
    'fi': 'FI',
    'no': 'NO',
    'id': 'ID',
    'ms': 'MY',
  };

  /// Extract ISO country code from a locale string
  /// Handles various formats: "ko_KR", "en-US", "zh-Hans", "en", etc.
  ///
  /// Examples:
  /// - "ko_KR" → "KR"
  /// - "en-US" → "US"
  /// - "zh-Hans_CN" → "CN"
  /// - "zh-Hant_TW" → "TW"
  /// - "ko" → "KR" (inferred from language)
  /// - "en" → "US" (inferred from language)
  static String getCountryCodeFromLocaleString(String localeString) {
    if (localeString.isEmpty) return 'international';

    // Normalize: replace hyphens with underscores
    final normalized = localeString.replaceAll('-', '_');
    final parts = normalized.split('_');

    // Look for a 2-letter uppercase country code in the parts (search from end)
    for (final part in parts.reversed) {
      if (part.length == 2 && part == part.toUpperCase()) {
        return part; // Found country code like "KR", "US", "CN"
      }
    }

    // No country code found, use language to infer country
    if (parts.isNotEmpty) {
      final language = parts[0].toLowerCase();
      if (languageToCountry.containsKey(language)) {
        return languageToCountry[language]!;
      }
    }

    return 'international'; // Default fallback for unknown locales
  }

  /// Get country code from current device locale
  static String getCountryCodeFromDevice() {
    return getCountryCodeFromLocaleString(Platform.localeName);
  }
}
