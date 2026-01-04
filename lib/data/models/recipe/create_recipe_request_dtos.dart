import 'package:json_annotation/json_annotation.dart';
import 'ingredient_dto.dart';
import 'step_dto.dart';

// 💡 중요: 파일명이 'create_recipe_request_dtos.dart'이므로 아래와 같이 정확히 맞춰야 합니다.
part 'create_recipe_request_dtos.g.dart';

@JsonSerializable()
class CreateRecipeRequestDto {
  final String title;
  final String description;
  final String culinaryLocale;
  final int? food1MasterId; // [추가]
  final List<IngredientDto> ingredients;
  final List<StepDto> steps;
  final List<String> imagePublicIds; // [추가] 대표 사진 UUID 리스트
  final String? changeCategory; // [추가] 변형 시 카테고리
  final String? parentPublicId; // [추가] 부모 레시피 UUID

  CreateRecipeRequestDto({
    required this.title,
    required this.description,
    required this.culinaryLocale,
    this.food1MasterId,
    required this.ingredients,
    required this.steps,
    required this.imagePublicIds,
    this.changeCategory,
    this.parentPublicId,
  });

  // 💡 클래스 이름과 매칭되는 생성자
  factory CreateRecipeRequestDto.fromJson(Map<String, dynamic> json) =>
      _$CreateRecipeRequestDtoFromJson(json);

  Map<String, dynamic> toJson() => _$CreateRecipeRequestDtoToJson(this);
}
