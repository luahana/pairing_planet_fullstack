import 'package:json_annotation/json_annotation.dart';
import '../../../domain/entities/autocomplete/autocomplete_result.dart';

part 'autocomplete_dto.g.dart';

@JsonSerializable()
class AutocompleteDto {
  final int id; // Java Long -> int
  final String name; //
  final String type; // "FOOD" or "CATEGORY"
  final double score; // 유사도 점수

  AutocompleteDto({
    required this.id,
    required this.name,
    required this.type,
    required this.score,
  });

  factory AutocompleteDto.fromJson(Map<String, dynamic> json) =>
      _$AutocompleteDtoFromJson(json);

  // 💡 도메인 엔티티로 변환
  AutocompleteResult toEntity() =>
      AutocompleteResult(id: id, name: name, type: type);
}
