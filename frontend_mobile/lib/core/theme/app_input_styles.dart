import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:pairing_planet2_frontend/core/theme/app_colors.dart';

/// Reusable input decoration styles for consistent editable textbox styling
class AppInputStyles {
  AppInputStyles._();

  /// Default border radius for editable inputs
  static double get borderRadius => 12.r;

  /// BoxDecoration for Container-wrapped TextFields (no internal border)
  /// Use with TextField that has `border: InputBorder.none`
  static BoxDecoration get editableBoxDecoration => BoxDecoration(
        color: AppColors.editableBackground,
        borderRadius: BorderRadius.circular(borderRadius),
        border: Border.all(color: AppColors.editableBorder),
      );

  /// InputDecoration for TextField with OutlineInputBorder styling
  /// Includes filled background and proper border states
  static InputDecoration editableInputDecoration({
    String? hintText,
    TextStyle? hintStyle,
    EdgeInsetsGeometry? contentPadding,
  }) {
    return InputDecoration(
      hintText: hintText,
      hintStyle: hintStyle ?? TextStyle(color: Colors.grey[400]),
      filled: true,
      fillColor: AppColors.editableBackground,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(borderRadius),
        borderSide: BorderSide(color: AppColors.editableBorder),
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(borderRadius),
        borderSide: BorderSide(color: AppColors.editableBorder),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(borderRadius),
        borderSide: BorderSide(color: AppColors.primary, width: 2),
      ),
      contentPadding: contentPadding ?? EdgeInsets.all(16.r),
    );
  }

  /// BoxDecoration for add photo/media buttons
  static BoxDecoration get addButtonDecoration => BoxDecoration(
        color: AppColors.editableBackground,
        borderRadius: BorderRadius.circular(borderRadius),
        border: Border.all(color: AppColors.editableBorder),
      );
}
