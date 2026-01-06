import 'package:json_annotation/json_annotation.dart';
import 'package:pairing_planet2_frontend/domain/entities/recipe/create_recipe_request.dart';
import 'ingredient_dto.dart';
import 'step_dto.dart';

// 💡 중요: 파일명이 'create_recipe_request_dtos.dart'이므로 아래와 같이 정확히 맞춰야 합니다.
part 'create_recipe_request_dtos.g.dart';

@JsonSerializable()
class CreateRecipeRequestDto {
  final String title;
  final String description;
  final String? culinaryLocale;
  final String? food1MasterPublicId;
  final String? newFoodName;
  final List<IngredientDto> ingredients;
  final List<StepDto> steps;
  final List<String> imagePublicIds;
  final String? changeCategory;
  final String? parentPublicId;
  final String? rootPublicId;

  CreateRecipeRequestDto({
    required this.title,
    required this.description,
    this.culinaryLocale,
    this.food1MasterPublicId,
    this.newFoodName,
    required this.ingredients,
    required this.steps,
    required this.imagePublicIds,
    this.changeCategory,
    this.parentPublicId,
    this.rootPublicId,
  }) {
    // 💡 생성자 몸체에서 검증 로직 추가
    if (food1MasterPublicId == null &&
        (newFoodName == null || newFoodName!.trim().isEmpty)) {
      throw ArgumentError(
        'food1MasterPublicId 또는 newFoodName 중 하나는 반드시 입력되어야 합니다.',
      );
    }
  }

  // 💡 클래스 이름과 매칭되는 생성자
  factory CreateRecipeRequestDto.fromJson(Map<String, dynamic> json) =>
      _$CreateRecipeRequestDtoFromJson(json);

  Map<String, dynamic> toJson() => _$CreateRecipeRequestDtoToJson(this);

  factory CreateRecipeRequestDto.fromEntity(CreateRecipeRequest request) {
    return CreateRecipeRequestDto(
      title: request.title,
      description: request.description,
      culinaryLocale: request.culinaryLocale,
      food1MasterPublicId: request.food1MasterPublicId,
      newFoodName: request.newFoodName,
      ingredients: request.ingredients.map((e) => IngredientDto.fromEntity(e)).toList(),
      steps: request.steps.map((e) => StepDto.fromEntity(e)).toList(),
      imagePublicIds: request.imagePublicIds,
      changeCategory: request.changeCategory,
      parentPublicId: request.parentPublicId,
      rootPublicId: request.rootPublicId,
    );
  }
}
