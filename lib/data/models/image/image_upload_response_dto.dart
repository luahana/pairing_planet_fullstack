import 'package:json_annotation/json_annotation.dart';

part 'image_upload_response_dto.g.dart';

@JsonSerializable()
class ImageUploadResponseDto {
  final String imagePublicId; // UUID
  final String imageUrl; // 이미지 접근 URL
  final String originalFilename;

  ImageUploadResponseDto({
    required this.imagePublicId,
    required this.imageUrl,
    required this.originalFilename,
  });

  factory ImageUploadResponseDto.fromJson(Map<String, dynamic> json) =>
      _$ImageUploadResponseDtoFromJson(json);
}
