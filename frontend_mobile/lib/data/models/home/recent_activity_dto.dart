import 'package:json_annotation/json_annotation.dart';

part 'recent_activity_dto.g.dart';

// Helper function to safely parse foodName which might be String or Map (from stale cache)
String _parseFoodName(dynamic value) {
  if (value == null) return 'Unknown Food';
  if (value is String) return value;
  if (value is Map) {
    if (value.containsKey('ko-KR')) return value['ko-KR']?.toString() ?? 'Unknown Food';
    if (value.containsKey('en-US')) return value['en-US']?.toString() ?? 'Unknown Food';
    return value.values.firstOrNull?.toString() ?? 'Unknown Food';
  }
  return value.toString();
}

@JsonSerializable()
class RecentActivityDto {
  final String logPublicId;
  final String outcome;
  final String? thumbnailUrl;
  final String creatorName;
  final String recipeTitle;
  final String recipePublicId;
  @JsonKey(fromJson: _parseFoodName)
  final String foodName;
  final DateTime? createdAt;

  RecentActivityDto({
    required this.logPublicId,
    required this.outcome,
    this.thumbnailUrl,
    required this.creatorName,
    required this.recipeTitle,
    required this.recipePublicId,
    required this.foodName,
    this.createdAt,
  });

  factory RecentActivityDto.fromJson(Map<String, dynamic> json) =>
      _$RecentActivityDtoFromJson(json);
  Map<String, dynamic> toJson() => _$RecentActivityDtoToJson(this);
}
