class RecipeSummary {
  final String publicId;
  final String foodName; // 💡 추가
  final String? foodMasterPublicId; // 💡 추가
  final String title;
  final String description;
  final String culinaryLocale;
  final String? thumbnailUrl;
  final String creatorName;
  final int variantCount;
  final String? parentPublicId; // 💡 추가
  final String? rootPublicId;

  RecipeSummary({
    required this.publicId,
    required this.foodName, // 💡 추가
    this.foodMasterPublicId, // 💡 추가
    required this.title,
    required this.description,
    required this.culinaryLocale,
    this.thumbnailUrl,
    required this.creatorName,
    required this.variantCount,
    this.parentPublicId, // 💡 추가
    this.rootPublicId,
  });
}
