import 'package:url_launcher/url_launcher.dart';

/// Utility class for launching external URLs
/// Uses externalApplication mode for App Store/Play Store compliance
class UrlLauncherUtils {
  /// Launch an external URL in the system browser (not WebView)
  /// Returns true if successful, false otherwise
  static Future<bool> launchExternalUrl(String url) async {
    final uri = Uri.tryParse(url);
    if (uri == null) return false;

    try {
      return await launchUrl(
        uri,
        mode: LaunchMode.externalApplication,
      );
    } catch (e) {
      return false;
    }
  }

  /// Launch YouTube URL
  /// Adds https:// prefix if missing
  static Future<bool> launchYoutube(String url) async {
    String normalizedUrl = url.trim();
    if (!normalizedUrl.startsWith('http')) {
      normalizedUrl = 'https://$normalizedUrl';
    }
    return launchExternalUrl(normalizedUrl);
  }

  /// Launch Instagram profile
  /// Accepts handle (with or without @) or full URL
  static Future<bool> launchInstagram(String handleOrUrl) async {
    String normalizedUrl = handleOrUrl.trim();

    // If it's already a URL
    if (normalizedUrl.contains('instagram.com')) {
      if (!normalizedUrl.startsWith('http')) {
        normalizedUrl = 'https://$normalizedUrl';
      }
      return launchExternalUrl(normalizedUrl);
    }

    // If it's just a handle
    final handle =
        normalizedUrl.startsWith('@') ? normalizedUrl.substring(1) : normalizedUrl;
    return launchExternalUrl('https://www.instagram.com/$handle');
  }
}
