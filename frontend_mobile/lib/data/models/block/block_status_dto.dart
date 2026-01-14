import 'package:json_annotation/json_annotation.dart';

part 'block_status_dto.g.dart';

@JsonSerializable()
class BlockStatusDto {
  final bool isBlocked;
  final bool amBlocked;

  BlockStatusDto({
    required this.isBlocked,
    required this.amBlocked,
  });

  factory BlockStatusDto.fromJson(Map<String, dynamic> json) =>
      _$BlockStatusDtoFromJson(json);
  Map<String, dynamic> toJson() => _$BlockStatusDtoToJson(this);
}
