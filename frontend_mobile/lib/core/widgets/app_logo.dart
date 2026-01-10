import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:pairing_planet2_frontend/core/theme/app_colors.dart';

/// Reusable app logo widget displaying "Pairing Planet" text.
/// This serves as a placeholder until a proper logo asset is designed.
class AppLogo extends StatelessWidget {
  final double? fontSize;
  final Color? color;

  const AppLogo({super.key, this.fontSize, this.color});

  @override
  Widget build(BuildContext context) {
    return Text(
      'Pairing Planet',
      style: TextStyle(
        fontSize: fontSize ?? 18.sp,
        fontWeight: FontWeight.w700,
        color: color ?? AppColors.primary,
        letterSpacing: -0.5,
      ),
    );
  }
}
