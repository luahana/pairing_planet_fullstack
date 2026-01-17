import 'package:json_annotation/json_annotation.dart';

part 'update_profile_request_dto.g.dart';

@JsonSerializable()
class UpdateProfileRequestDto {
  final String? username;
  final String? profileImagePublicId;
  final String? gender;  // MALE, FEMALE, OTHER
  final String? birthDate;  // yyyy-MM-dd format
  final String? preferredDietaryId;
  final bool? marketingAgreed;
  final String? locale;  // ko-KR, en-US
  final String? defaultCookingStyle;  // ISO country code (e.g., "KR", "US")
  final String? bio;  // User bio/description (max 150 chars)
  final String? youtubeUrl;  // YouTube channel URL
  final String? instagramHandle;  // Instagram handle

  UpdateProfileRequestDto({
    this.username,
    this.profileImagePublicId,
    this.gender,
    this.birthDate,
    this.preferredDietaryId,
    this.marketingAgreed,
    this.locale,
    this.defaultCookingStyle,
    this.bio,
    this.youtubeUrl,
    this.instagramHandle,
  });

  factory UpdateProfileRequestDto.fromJson(Map<String, dynamic> json) =>
      _$UpdateProfileRequestDtoFromJson(json);
  Map<String, dynamic> toJson() => _$UpdateProfileRequestDtoToJson(this);
}
