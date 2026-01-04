import 'package:json_annotation/json_annotation.dart';

part 'trending_tree_dto.g.dart';

@JsonSerializable()
class TrendingTreeDto {
  final String rootRecipeId;
  final String title;
  final String culinaryLocale;
  final int variantCount;
  final int logCount;
  final String? latestChangeSummary;

  TrendingTreeDto({
    required this.rootRecipeId,
    required this.title,
    required this.culinaryLocale,
    required this.variantCount,
    required this.logCount,
    this.latestChangeSummary,
  });

  factory TrendingTreeDto.fromJson(Map<String, dynamic> json) =>
      _$TrendingTreeDtoFromJson(json);
  Map<String, dynamic> toJson() => _$TrendingTreeDtoToJson(this);
}
