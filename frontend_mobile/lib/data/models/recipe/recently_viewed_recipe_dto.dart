import 'package:pairing_planet2_frontend/domain/entities/recipe/recipe_summary.dart';

/// Lightweight DTO for storing recently viewed recipes locally.
/// Contains only essential info needed for recipe picker display.
class RecentlyViewedRecipeDto {
  final String publicId;
  final String title;
  final String foodName;
  final String? thumbnailUrl;
  final DateTime viewedAt;

  RecentlyViewedRecipeDto({
    required this.publicId,
    required this.title,
    required this.foodName,
    this.thumbnailUrl,
    required this.viewedAt,
  });

  factory RecentlyViewedRecipeDto.fromJson(Map<String, dynamic> json) {
    // Helper to safely parse foodName which might be String or Map (from stale cache)
    String parseFoodName(dynamic value) {
      if (value == null) return 'Unknown Food';
      if (value is String) return value;
      if (value is Map) {
        if (value.containsKey('ko-KR')) return value['ko-KR']?.toString() ?? 'Unknown Food';
        if (value.containsKey('en-US')) return value['en-US']?.toString() ?? 'Unknown Food';
        return value.values.firstOrNull?.toString() ?? 'Unknown Food';
      }
      return value.toString();
    }

    return RecentlyViewedRecipeDto(
      publicId: json['publicId'] as String,
      title: json['title'] as String,
      foodName: parseFoodName(json['foodName']),
      thumbnailUrl: json['thumbnailUrl'] as String?,
      viewedAt: DateTime.parse(json['viewedAt'] as String),
    );
  }

  Map<String, dynamic> toJson() => {
        'publicId': publicId,
        'title': title,
        'foodName': foodName,
        'thumbnailUrl': thumbnailUrl,
        'viewedAt': viewedAt.toIso8601String(),
      };

  /// Convert to RecipeSummary for UI compatibility
  RecipeSummary toRecipeSummary() => RecipeSummary(
        publicId: publicId,
        title: title,
        foodName: foodName,
        thumbnailUrl: thumbnailUrl,
        description: '',
        culinaryLocale: '',
        creatorName: '',
        variantCount: 0,
        logCount: 0,
      );
}
