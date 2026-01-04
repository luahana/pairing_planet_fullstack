// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'recipe_detail_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecipeDetailResponseDto _$RecipeDetailResponseDtoFromJson(
  Map<String, dynamic> json,
) => RecipeDetailResponseDto(
  publicId: json['publicId'] as String,
  title: json['title'] as String,
  description: json['description'] as String,
  culinaryLocale: json['culinaryLocale'] as String,
  changeCategory: json['changeCategory'] as String?,
  rootInfo: json['rootInfo'] == null
      ? null
      : RecipeSummaryDto.fromJson(json['rootInfo'] as Map<String, dynamic>),
  parentInfo: json['parentInfo'] == null
      ? null
      : RecipeSummaryDto.fromJson(json['parentInfo'] as Map<String, dynamic>),
  ingredients: (json['ingredients'] as List<dynamic>)
      .map((e) => IngredientRequestDto.fromJson(e as Map<String, dynamic>))
      .toList(),
  steps: (json['steps'] as List<dynamic>)
      .map((e) => StepRequestDto.fromJson(e as Map<String, dynamic>))
      .toList(),
  imageUrls: (json['imageUrls'] as List<dynamic>)
      .map((e) => e as String)
      .toList(),
  variants: (json['variants'] as List<dynamic>)
      .map((e) => RecipeSummaryDto.fromJson(e as Map<String, dynamic>))
      .toList(),
  logs: (json['logs'] as List<dynamic>)
      .map((e) => LogPostSummaryDto.fromJson(e as Map<String, dynamic>))
      .toList(),
);

Map<String, dynamic> _$RecipeDetailResponseDtoToJson(
  RecipeDetailResponseDto instance,
) => <String, dynamic>{
  'publicId': instance.publicId,
  'title': instance.title,
  'description': instance.description,
  'culinaryLocale': instance.culinaryLocale,
  'changeCategory': instance.changeCategory,
  'rootInfo': instance.rootInfo,
  'parentInfo': instance.parentInfo,
  'ingredients': instance.ingredients,
  'steps': instance.steps,
  'imageUrls': instance.imageUrls,
  'variants': instance.variants,
  'logs': instance.logs,
};
