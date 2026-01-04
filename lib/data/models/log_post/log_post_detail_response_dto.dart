import 'package:json_annotation/json_annotation.dart';
import 'package:pairing_planet2_frontend/data/models/recipe/recipe_summary_dto.dart';

part 'log_post_detail_response_dto.g.dart';

@JsonSerializable()
class LogPostDetailResponseDto {
  final String publicId;
  final String title;
  final String content;
  final int? rating;
  final List<String> imageUrls;
  final RecipeSummaryDto linkedRecipe;

  LogPostDetailResponseDto({
    required this.publicId,
    required this.title,
    required this.content,
    this.rating,
    required this.imageUrls,
    required this.linkedRecipe,
  });

  factory LogPostDetailResponseDto.fromJson(Map<String, dynamic> json) =>
      _$LogPostDetailResponseDtoFromJson(json);

  Map<String, dynamic> toJson() => _$LogPostDetailResponseDtoToJson(this);
}
