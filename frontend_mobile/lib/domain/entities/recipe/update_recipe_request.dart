import 'package:pairing_planet2_frontend/domain/entities/recipe/ingredient.dart';
import 'package:pairing_planet2_frontend/domain/entities/recipe/recipe_step.dart';

/// Request entity for updating an existing recipe.
/// Only the recipe creator can update, and only if there are no child variants or logs.
class UpdateRecipeRequest {
  final String title;
  final String? description;
  final String? cookingStyle;
  final List<Ingredient> ingredients;
  final List<RecipeStep> steps;
  final List<String> imagePublicIds;
  final List<String>? hashtags;
  final int? servings;
  final String? cookingTimeRange;

  UpdateRecipeRequest({
    required this.title,
    this.description,
    this.cookingStyle,
    required this.ingredients,
    required this.steps,
    required this.imagePublicIds,
    this.hashtags,
    this.servings,
    this.cookingTimeRange,
  });
}
