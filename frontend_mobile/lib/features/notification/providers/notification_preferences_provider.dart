import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Notification types matching backend enum
enum NotificationType {
  recipeCookked,   // Someone cooked your recipe
  recipeVariation, // Someone created a variation
  newFollower,     // Someone followed you
}

/// State for notification preferences
class NotificationPreferencesState {
  final bool recipeCookedEnabled;
  final bool recipeVariationEnabled;
  final bool newFollowerEnabled;
  final bool allNotificationsEnabled;
  final bool isLoading;

  const NotificationPreferencesState({
    this.recipeCookedEnabled = true,
    this.recipeVariationEnabled = true,
    this.newFollowerEnabled = true,
    this.allNotificationsEnabled = true,
    this.isLoading = true,
  });

  NotificationPreferencesState copyWith({
    bool? recipeCookedEnabled,
    bool? recipeVariationEnabled,
    bool? newFollowerEnabled,
    bool? allNotificationsEnabled,
    bool? isLoading,
  }) {
    return NotificationPreferencesState(
      recipeCookedEnabled: recipeCookedEnabled ?? this.recipeCookedEnabled,
      recipeVariationEnabled: recipeVariationEnabled ?? this.recipeVariationEnabled,
      newFollowerEnabled: newFollowerEnabled ?? this.newFollowerEnabled,
      allNotificationsEnabled: allNotificationsEnabled ?? this.allNotificationsEnabled,
      isLoading: isLoading ?? this.isLoading,
    );
  }

  /// Check if a notification type is enabled
  bool isTypeEnabled(String type) {
    if (!allNotificationsEnabled) return false;

    switch (type.toUpperCase()) {
      case 'RECIPE_COOKED':
        return recipeCookedEnabled;
      case 'RECIPE_VARIATION':
        return recipeVariationEnabled;
      case 'NEW_FOLLOWER':
        return newFollowerEnabled;
      default:
        return true; // Allow unknown types by default
    }
  }
}

/// Provider for notification preferences
final notificationPreferencesProvider = StateNotifierProvider<
    NotificationPreferencesNotifier, NotificationPreferencesState>((ref) {
  return NotificationPreferencesNotifier();
});

class NotificationPreferencesNotifier
    extends StateNotifier<NotificationPreferencesState> {
  static const String _keyPrefix = 'notification_pref_';
  static const String _keyAllEnabled = '${_keyPrefix}all_enabled';
  static const String _keyRecipeCooked = '${_keyPrefix}recipe_cooked';
  static const String _keyRecipeVariation = '${_keyPrefix}recipe_variation';
  static const String _keyNewFollower = '${_keyPrefix}new_follower';

  NotificationPreferencesNotifier()
      : super(const NotificationPreferencesState()) {
    _loadPreferences();
  }

  Future<void> _loadPreferences() async {
    final prefs = await SharedPreferences.getInstance();

    state = state.copyWith(
      allNotificationsEnabled: prefs.getBool(_keyAllEnabled) ?? true,
      recipeCookedEnabled: prefs.getBool(_keyRecipeCooked) ?? true,
      recipeVariationEnabled: prefs.getBool(_keyRecipeVariation) ?? true,
      newFollowerEnabled: prefs.getBool(_keyNewFollower) ?? true,
      isLoading: false,
    );
  }

  Future<void> setAllNotificationsEnabled(bool enabled) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyAllEnabled, enabled);

    state = state.copyWith(allNotificationsEnabled: enabled);
  }

  Future<void> setRecipeCookedEnabled(bool enabled) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyRecipeCooked, enabled);

    state = state.copyWith(recipeCookedEnabled: enabled);
  }

  Future<void> setRecipeVariationEnabled(bool enabled) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyRecipeVariation, enabled);

    state = state.copyWith(recipeVariationEnabled: enabled);
  }

  Future<void> setNewFollowerEnabled(bool enabled) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyNewFollower, enabled);

    state = state.copyWith(newFollowerEnabled: enabled);
  }

  Future<void> resetToDefaults() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyAllEnabled, true);
    await prefs.setBool(_keyRecipeCooked, true);
    await prefs.setBool(_keyRecipeVariation, true);
    await prefs.setBool(_keyNewFollower, true);

    state = const NotificationPreferencesState(isLoading: false);
  }
}
