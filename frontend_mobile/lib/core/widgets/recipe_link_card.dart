import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:go_router/go_router.dart';
import 'package:pairing_planet2_frontend/core/constants/constants.dart';
import 'package:pairing_planet2_frontend/core/theme/app_colors.dart';
import 'package:pairing_planet2_frontend/core/widgets/app_cached_image.dart';
import 'package:pairing_planet2_frontend/features/recipe/presentation/widgets/locale_badge.dart';

/// Reusable recipe link card for displaying recipe references.
/// Used in "Based on" section and "Recipe Used" section.
class RecipeLinkCard extends StatelessWidget {
  final String publicId;
  final String title;
  final String? creatorName;
  final String? thumbnailUrl;
  final String? culinaryLocale;

  const RecipeLinkCard({
    super.key,
    required this.publicId,
    required this.title,
    this.creatorName,
    this.thumbnailUrl,
    this.culinaryLocale,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: () {
        HapticFeedback.lightImpact();
        context.push(RouteConstants.recipeDetailPath(publicId));
      },
      child: Padding(
        padding: EdgeInsets.all(12.r),
        child: Row(
          children: [
            // Thumbnail
            ClipRRect(
              borderRadius: BorderRadius.circular(8.r),
              child: SizedBox(
                width: 50.w,
                height: 50.w,
                child: thumbnailUrl != null && thumbnailUrl!.isNotEmpty
                    ? AppCachedImage(
                        imageUrl: thumbnailUrl!,
                        width: 50.w,
                        height: 50.w,
                        borderRadius: 8.r,
                      )
                    : Container(
                        color: Colors.grey[200],
                        child: Icon(Icons.restaurant, size: 24.sp, color: Colors.grey),
                      ),
              ),
            ),
            SizedBox(width: 12.w),
            // Recipe info
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          title,
                          style: TextStyle(
                            fontSize: 14.sp,
                            fontWeight: FontWeight.w600,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                      if (culinaryLocale != null && culinaryLocale!.isNotEmpty) ...[
                        SizedBox(width: 8.w),
                        LocaleBadge(
                          localeCode: culinaryLocale!,
                          showLabel: false,
                          fontSize: 12.sp,
                        ),
                      ],
                    ],
                  ),
                  if (creatorName != null) ...[
                    SizedBox(height: 2.h),
                    Text(
                      '@$creatorName',
                      style: TextStyle(
                        fontSize: 12.sp,
                        color: AppColors.primary,
                      ),
                    ),
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
