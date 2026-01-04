import 'package:json_annotation/json_annotation.dart';
import 'package:pairing_planet2_frontend/domain/entities/recipe/recipe_step.dart';

part 'step_dto.g.dart';

@JsonSerializable()
class StepRequestDto {
  final int stepNumber;
  final String description;
  final String? imageUrl;

  StepRequestDto({
    required this.stepNumber,
    required this.description,
    this.imageUrl,
  });

  factory StepRequestDto.fromJson(Map<String, dynamic> json) =>
      _$StepRequestDtoFromJson(json);
  Map<String, dynamic> toJson() => _$StepRequestDtoToJson(this);

  RecipeStep toEntity() => RecipeStep(
    stepNumber: stepNumber,
    description: description,
    imageUrl: imageUrl,
  );
}
