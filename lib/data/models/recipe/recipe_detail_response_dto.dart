import 'package:json_annotation/json_annotation.dart';
import 'package:pairing_planet2_frontend/data/models/log_post/log_post_summary_dto.dart';
import 'package:pairing_planet2_frontend/domain/entities/recipe/recipe_detail.dart';
import 'recipe_summary_dto.dart';
import 'ingredient_request_dto.dart';
import 'step_request_dto.dart';

part 'recipe_detail_response_dto.g.dart';

@JsonSerializable()
class RecipeDetailResponseDto {
  final String publicId;
  final String title;
  final String description;
  final String culinaryLocale;
  final String? changeCategory;
  final RecipeSummaryDto? rootInfo;
  final RecipeSummaryDto? parentInfo;
  final List<IngredientRequestDto> ingredients;
  final List<StepRequestDto> steps;
  final List<String> imageUrls;
  final List<RecipeSummaryDto> variants;
  final List<LogPostSummaryDto> logs;

  RecipeDetailResponseDto({
    required this.publicId,
    required this.title,
    required this.description,
    required this.culinaryLocale,
    this.changeCategory,
    this.rootInfo,
    this.parentInfo,
    required this.ingredients,
    required this.steps,
    required this.imageUrls,
    required this.variants,
    required this.logs,
  });

  factory RecipeDetailResponseDto.fromJson(Map<String, dynamic> json) =>
      _$RecipeDetailResponseDtoFromJson(json);
  Map<String, dynamic> toJson() => _$RecipeDetailResponseDtoToJson(this);

  RecipeDetail toEntity() => RecipeDetail(
    id: publicId,
    title: title,
    description: description,
    culinaryLocale: culinaryLocale,
    changeCategory: changeCategory,
    rootInfo: rootInfo?.toEntity(),
    parentInfo: parentInfo?.toEntity(),
    ingredients: ingredients.map((e) => e.toEntity()).toList(),
    steps: steps.map((e) => e.toEntity()).toList(),
    imageUrls: imageUrls,
    variants: variants.map((e) => e.toEntity()).toList(),
    logs: logs.map((e) => e.toEntity()).toList(),
  );
}
