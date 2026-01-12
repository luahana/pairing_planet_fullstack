import 'dart:io';
import 'package:flutter_dotenv/flutter_dotenv.dart';

/// Resolves URLs for different platforms (iOS vs Android) during local development.
/// Handles the difference between localhost (iOS) and 10.0.2.2 (Android emulator).
class PlatformUrlResolver {
  static String get baseUrl {
    final envUrl = dotenv.maybeGet('BASE_URL');

    // If .env specifies a URL, check if it's a local dev URL that needs platform adjustment
    if (envUrl != null && envUrl.isNotEmpty) {
      // If it's a local dev URL (localhost or 10.0.2.2), adjust for platform
      if (envUrl.contains('10.0.2.2') || envUrl.contains('localhost')) {
        return _adjustLocalUrl(envUrl);
      }
      return envUrl;
    }

    // Default fallback with platform awareness
    return _getDefaultLocalUrl();
  }

  static String _adjustLocalUrl(String url) {
    return adjustUrlForPlatform(url);
  }

  /// Adjusts any URL containing local dev hostnames (10.0.2.2 or localhost)
  /// to use the correct hostname for the current platform.
  /// Use this for image URLs, API URLs, or any other network resources.
  /// Note: Real IP addresses (like 10.0.0.x) are not modified.
  static String adjustUrlForPlatform(String url) {
    // Don't modify URLs with real IP addresses (for real device testing)
    if (_hasRealIpAddress(url)) {
      return url;
    }
    if (Platform.isIOS) {
      // iOS simulator uses localhost
      return url.replaceAll('10.0.2.2', 'localhost');
    } else if (Platform.isAndroid) {
      // Android emulator uses 10.0.2.2 for host machine
      return url.replaceAll('localhost', '10.0.2.2');
    }
    return url;
  }

  /// Check if URL contains a real IP address (not localhost or 10.0.2.2)
  static bool _hasRealIpAddress(String url) {
    // Match IP addresses but exclude 10.0.2.2 (Android emulator)
    final ipRegex = RegExp(r'\b(?!10\.0\.2\.2)\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b');
    return ipRegex.hasMatch(url);
  }

  static String _getDefaultLocalUrl() {
    final host = Platform.isIOS ? 'localhost' : '10.0.2.2';
    return 'http://$host:4001/api/v1';
  }

  static bool get isDev => dotenv.get('ENV', fallback: 'dev') == 'dev';
}
