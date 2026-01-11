import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';

/// A search icon button wrapped in Hero for smooth page transitions.
/// When navigating to search screen, the icon morphs into the search field.
class HeroSearchIcon extends StatelessWidget {
  /// Called when the search icon is tapped.
  final VoidCallback onTap;

  /// Color of the search icon.
  final Color? color;

  /// Size of the search icon.
  final double? size;

  /// Hero tag for the animation. Use unique tags if multiple
  /// search icons could be visible simultaneously.
  final String heroTag;

  const HeroSearchIcon({
    super.key,
    required this.onTap,
    this.color,
    this.size,
    this.heroTag = 'search-hero',
  });

  @override
  Widget build(BuildContext context) {
    return Hero(
      tag: heroTag,
      flightShuttleBuilder: _buildFlightShuttle,
      child: Material(
        color: Colors.transparent,
        child: IconButton(
          icon: Icon(
            Icons.search,
            color: color,
            size: size ?? 24.sp,
          ),
          onPressed: onTap,
        ),
      ),
    );
  }

  /// Custom flight shuttle that shows the search icon during animation.
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
        // During forward flight, fade out the icon and fade in destination
        // During reverse flight, do the opposite
        final isForward = flightDirection == HeroFlightDirection.push;
        final progress = animation.value;

        return Material(
          color: Colors.transparent,
          child: Icon(
            Icons.search,
            color: (color ?? Colors.black).withValues(
              alpha: isForward ? 1.0 - progress : progress,
            ),
            size: size ?? 24.sp,
          ),
        );
      },
    );
  }
}
