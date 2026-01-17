import 'ingredient.dart';
import 'recipe_step.dart';
import 'recipe_summary.dart';
import '../log_post/log_post_summary.dart';
import '../hashtag/hashtag.dart';

class RecipeDetail {
  final String publicId;
  final String foodName;
  final String foodMasterPublicId;
  final String? creatorPublicId;
  final String userName;
  final String title;
  final String? description;
  final String? cookingStyle;
  final String? changeCategory;
  final RecipeSummary? rootInfo;
  final RecipeSummary? parentInfo;
  final List<Ingredient> ingredients;
  final List<RecipeStep> steps;
  final List<String> imageUrls;
  final List<String> imagePublicIds; // For edit mode - to send back to server
  final List<RecipeSummary> variants;
  final List<LogPostSummary> logs;
  final List<Hashtag> hashtags;
  final bool? isSavedByCurrentUser;

  // Living Blueprint: Diff fields for variation tracking
  final Map<String, dynamic>? changeDiff;
  final List<String>? changeCategories;
  final String? changeReason;

  // Servings and cooking time
  final int servings;
  final String cookingTimeRange;

  RecipeDetail({
    required this.publicId,
    required this.foodName,
    required this.foodMasterPublicId,
    this.creatorPublicId,
    required this.userName,
    required this.title,
    required this.description,
    this.cookingStyle,
    this.changeCategory,
    this.rootInfo,
    this.parentInfo,
    required this.ingredients,
    required this.steps,
    required this.imageUrls,
    required this.imagePublicIds,
    required this.variants,
    required this.logs,
    required this.hashtags,
    this.isSavedByCurrentUser,
    this.changeDiff,
    this.changeCategories,
    this.changeReason,
    this.servings = 2,
    this.cookingTimeRange = 'MIN_30_TO_60',
  });

  /// Check if this recipe is a variant (has a parent)
  bool get isVariant => parentInfo != null;

  /// Check if this recipe has any changes from parent
  bool get hasChanges => changeDiff != null && changeDiff!.isNotEmpty;
}
