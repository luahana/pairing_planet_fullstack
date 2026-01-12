/// Cooking time range options for recipes.
/// Uses approximate ranges since cooking time varies by skill level.
enum CookingTimeRange {
  under15min('UNDER_15_MIN'),
  min15to30('MIN_15_TO_30'),
  min30to60('MIN_30_TO_60'),
  hour1to2('HOUR_1_TO_2'),
  over2hours('OVER_2_HOURS');

  final String code;
  const CookingTimeRange(this.code);

  /// Get the translation key for this cooking time range
  String get translationKey => 'recipe.cookingTime.$code';

  /// Get CookingTimeRange from backend code string
  static CookingTimeRange fromCode(String? code) {
    if (code == null) return CookingTimeRange.min30to60;
    return CookingTimeRange.values.firstWhere(
      (e) => e.code == code,
      orElse: () => CookingTimeRange.min30to60,
    );
  }
}
