import 'package:json_annotation/json_annotation.dart';

part 'trending_tree_dto.g.dart';

// Helper function to safely parse foodName which might be String or Map (from stale cache)
String? _parseFoodName(dynamic value) {
  if (value == null) return null;
  if (value is String) return value;
  if (value is Map) {
    if (value.containsKey('ko-KR')) return value['ko-KR']?.toString();
    if (value.containsKey('en-US')) return value['en-US']?.toString();
    return value.values.firstOrNull?.toString();
  }
  return value.toString();
}

@JsonSerializable()
class TrendingTreeDto {
  final String rootRecipeId;
  final String title;
  @JsonKey(fromJson: _parseFoodName)
  final String? foodName;
  final String culinaryLocale;
  final String? thumbnail;
  final int variantCount;
  final int logCount;
  final String? latestChangeSummary;

  TrendingTreeDto({
    required this.rootRecipeId,
    required this.title,
    this.foodName,
    required this.culinaryLocale,
    this.thumbnail,
    required this.variantCount,
    required this.logCount,
    this.latestChangeSummary,
  });

  factory TrendingTreeDto.fromJson(Map<String, dynamic> json) =>
      _$TrendingTreeDtoFromJson(json);
  Map<String, dynamic> toJson() => _$TrendingTreeDtoToJson(this);
}
