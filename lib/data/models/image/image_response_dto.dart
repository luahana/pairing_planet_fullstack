import 'package:json_annotation/json_annotation.dart';

part 'image_response_dto.g.dart';

@JsonSerializable()
class ImageResponseDto {
  final String publicId; // UUID
  final String url; // 이미지 전체 경로

  ImageResponseDto({required this.publicId, required this.url});

  factory ImageResponseDto.fromJson(Map<String, dynamic> json) =>
      _$ImageResponseDtoFromJson(json);
  Map<String, dynamic> toJson() => _$ImageResponseDtoToJson(this);
}
