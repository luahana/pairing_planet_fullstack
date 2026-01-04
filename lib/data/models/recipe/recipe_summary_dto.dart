import 'package:json_annotation/json_annotation.dart'; // 💡 필수 임포트
import 'package:pairing_planet2_frontend/domain/entities/recipe/recipe_summary.dart';

part 'recipe_summary_dto.g.dart'; // 💡 필수 선언

@JsonSerializable()
class RecipeSummaryDto {
  final String publicId;
  final String title;
  final String culinaryLocale;
  final String? creatorName;
  final String? thumbnail;

  RecipeSummaryDto({
    required this.publicId,
    required this.title,
    required this.culinaryLocale,
    this.creatorName,
    this.thumbnail,
  });

  factory RecipeSummaryDto.fromJson(Map<String, dynamic> json) =>
      _$RecipeSummaryDtoFromJson(json);
  Map<String, dynamic> toJson() => _$RecipeSummaryDtoToJson(this);

  RecipeSummary toEntity() => RecipeSummary(
    id: publicId,
    title: title,
    culinaryLocale: culinaryLocale,
    thumbnail: thumbnail,
  );
}
