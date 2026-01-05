import 'package:json_annotation/json_annotation.dart';
import 'package:pairing_planet2_frontend/domain/entities/recipe/recipe_summary.dart';

part 'recipe_summary_dto.g.dart';

@JsonSerializable()
class RecipeSummaryDto {
  final String publicId;
  final String foodName; // 💡 추가
  final String foodMasterPublicId; // 💡 추가
  final String title;
  final String? description;
  final String? culinaryLocale;
  final String? creatorName;
  final String? thumbnail;
  final int? variantCount;
  final String? parentPublicId; // 💡 추가
  final String? rootPublicId;

  RecipeSummaryDto({
    required this.publicId,
    required this.foodName,
    required this.foodMasterPublicId,
    required this.title,
    this.description,
    this.culinaryLocale,
    required this.creatorName,
    this.thumbnail,
    this.variantCount,
    this.parentPublicId,
    this.rootPublicId,
  });

  factory RecipeSummaryDto.fromJson(Map<String, dynamic> json) =>
      _$RecipeSummaryDtoFromJson(json);

  Map<String, dynamic> toJson() => _$RecipeSummaryDtoToJson(this);

  // 💡 DTO를 도메인 엔티티로 변환하는 로직
  RecipeSummary toEntity() => RecipeSummary(
    publicId: publicId,
    foodName: foodName, // 💡 매핑 추가
    foodMasterPublicId: foodMasterPublicId,
    title: title,
    description: description ?? "",
    culinaryLocale: culinaryLocale ?? "",
    thumbnailUrl: thumbnail,
    creatorName: creatorName ?? "익명",
    variantCount: variantCount ?? 0,
    parentPublicId: parentPublicId,
    rootPublicId: rootPublicId,
  );
}
