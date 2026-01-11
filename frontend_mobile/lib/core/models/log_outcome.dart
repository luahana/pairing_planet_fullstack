import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:pairing_planet2_frontend/core/constants/app_emojis.dart';

/// Outcome types for cooking logs with associated styling
enum LogOutcome {
  success('SUCCESS', 'logPost.outcomeLabel.success', Color(0xFF4CAF50), Color(0xFFE8F5E9)),
  partial('PARTIAL', 'logPost.outcomeLabel.partial', Color(0xFFFFC107), Color(0xFFFFF8E1)),
  failed('FAILED', 'logPost.outcomeLabel.failed', Color(0xFFF44336), Color(0xFFFFEBEE));

  final String value;
  final String labelKey;
  final Color primaryColor;
  final Color backgroundColor;

  const LogOutcome(this.value, this.labelKey, this.primaryColor, this.backgroundColor);

  String get emoji {
    switch (this) {
      case LogOutcome.success:
        return AppEmojis.outcomeSuccess;
      case LogOutcome.partial:
        return AppEmojis.outcomePartial;
      case LogOutcome.failed:
        return AppEmojis.outcomeFailed;
    }
  }

  String get label => labelKey.tr();

  static LogOutcome? fromString(String? value) {
    if (value == null) return null;
    return LogOutcome.values.cast<LogOutcome?>().firstWhere(
      (e) => e?.value == value,
      orElse: () => null,
    );
  }

  /// Get emoji for outcome string value.
  /// Returns default emoji if outcome is null or unknown.
  static String getEmoji(String? outcome, {String? defaultEmoji}) {
    final logOutcome = fromString(outcome);
    return logOutcome?.emoji ?? defaultEmoji ?? AppEmojis.outcomeDefault;
  }
}
