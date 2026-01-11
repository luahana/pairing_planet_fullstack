import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:pairing_planet2_frontend/core/theme/app_colors.dart';

/// A visual search bar wrapped in Hero for smooth page transitions.
/// When navigating to search screen, the bar morphs into the search field.
class HeroSearchBar extends StatelessWidget {
  /// Called when the search bar is tapped.
  final VoidCallback onTap;

  /// Hint text displayed in the search bar.
  final String hintText;

  /// Hero tag for the animation.
  final String heroTag;

  /// Height of the search bar.
  final double? height;

  const HeroSearchBar({
    super.key,
    required this.onTap,
    required this.hintText,
    this.heroTag = 'search-hero',
    this.height,
  });

  @override
  Widget build(BuildContext context) {
    return Hero(
      tag: heroTag,
      flightShuttleBuilder: _buildFlightShuttle,
      child: Material(
        color: Colors.transparent,
        child: GestureDetector(
          onTap: onTap,
          child: Container(
            height: height ?? 44.h,
            decoration: BoxDecoration(
              color: AppColors.background,
              borderRadius: BorderRadius.circular(12.r),
              border: Border.all(color: AppColors.border),
            ),
            child: Row(
              children: [
                SizedBox(width: 12.w),
                Icon(
                  Icons.search,
                  color: AppColors.textSecondary,
                  size: 20.sp,
                ),
                SizedBox(width: 8.w),
                Text(
                  hintText,
                  style: TextStyle(
                    color: AppColors.textSecondary,
                    fontSize: 14.sp,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  /// Custom flight shuttle that transforms from bar to search field.
  Widget _buildFlightShuttle(
    BuildContext flightContext,
    Animation<double> animation,
    HeroFlightDirection flightDirection,
    BuildContext fromHeroContext,
    BuildContext toHeroContext,
  ) {
    return AnimatedBuilder(
      animation: animation,
      builder: (context, child) {
        final progress = animation.value;

        return Material(
          color: Colors.transparent,
          child: Container(
            height: height ?? 44.h,
            decoration: BoxDecoration(
              color: AppColors.background.withValues(alpha: 1.0 - progress * 0.5),
              borderRadius: BorderRadius.circular(12.r * (1.0 - progress * 0.5)),
              border: Border.all(
                color: AppColors.border.withValues(alpha: 1.0 - progress),
              ),
            ),
            child: Row(
              children: [
                SizedBox(width: 12.w),
                Icon(
                  Icons.search,
                  color: AppColors.textSecondary,
                  size: 20.sp,
                ),
                SizedBox(width: 8.w),
                Opacity(
                  opacity: 1.0 - progress,
                  child: Text(
                    hintText,
                    style: TextStyle(
                      color: AppColors.textSecondary,
                      fontSize: 14.sp,
                    ),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}
