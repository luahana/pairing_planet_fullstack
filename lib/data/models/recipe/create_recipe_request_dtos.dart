import 'package:json_annotation/json_annotation.dart';
import 'ingredient_request_dto.dart'; // 💡 리팩토링: 외부 파일 참조
import 'step_request_dto.dart'; // 💡 리팩토링: 외부 파일 참조

part 'create_recipe_request_dtos.g.dart';

@JsonSerializable()
class CreateRecipeRequestDto {
  final String title;
  final String description;
  final String culinaryLocale;
  final int? food1MasterId;
  final List<IngredientRequestDto> ingredients;
  final List<StepRequestDto> steps;
  final List<String> imageUrls;
  final String? changeCategory;
  final String? parentPublicId;

  CreateRecipeRequestDto({
    required this.title,
    required this.description,
    required this.culinaryLocale,
    this.food1MasterId,
    required this.ingredients,
    required this.steps,
    required this.imageUrls,
    this.changeCategory,
    this.parentPublicId,
  });

  factory CreateRecipeRequestDto.fromJson(Map<String, dynamic> json) =>
      _$CreateRecipeRequestDtoFromJson(json);
  Map<String, dynamic> toJson() => _$CreateRecipeRequestDtoToJson(this);
}
