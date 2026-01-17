class RecipeSummary {
  final String publicId;
  final String foodName;
  final String? foodMasterPublicId;
  final String title;
  final String description;
  final String cookingStyle;
  final String? thumbnailUrl;
  final String? creatorPublicId; // Creator's publicId for profile navigation
  final String userName;
  final int variantCount;
  final int logCount; // Activity count: number of cooking logs
  final String? parentPublicId;
  final String? rootPublicId;
  final String? rootTitle; // For displaying root recipe link in variants
  final int servings;
  final String cookingTimeRange;
  final List<String>? hashtags; // Hashtag names (first 3)

  RecipeSummary({
    required this.publicId,
    required this.foodName,
    this.foodMasterPublicId,
    required this.title,
    required this.description,
    required this.cookingStyle,
    this.thumbnailUrl,
    this.creatorPublicId,
    required this.userName,
    required this.variantCount,
    this.logCount = 0,
    this.parentPublicId,
    this.rootPublicId,
    this.rootTitle,
    this.servings = 2,
    this.cookingTimeRange = 'MIN_30_TO_60',
    this.hashtags,
  });

  bool get isVariant => rootPublicId != null;
}
