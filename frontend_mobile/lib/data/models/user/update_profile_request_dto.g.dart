// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'update_profile_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UpdateProfileRequestDto _$UpdateProfileRequestDtoFromJson(
        Map<String, dynamic> json) =>
    UpdateProfileRequestDto(
      username: json['username'] as String?,
      profileImagePublicId: json['profileImagePublicId'] as String?,
      gender: json['gender'] as String?,
      birthDate: json['birthDate'] as String?,
      preferredDietaryId: json['preferredDietaryId'] as String?,
      marketingAgreed: json['marketingAgreed'] as bool?,
      locale: json['locale'] as String?,
      defaultFoodStyle: json['defaultFoodStyle'] as String?,
      bio: json['bio'] as String?,
      youtubeUrl: json['youtubeUrl'] as String?,
      instagramHandle: json['instagramHandle'] as String?,
    );

Map<String, dynamic> _$UpdateProfileRequestDtoToJson(
        UpdateProfileRequestDto instance) =>
    <String, dynamic>{
      'username': instance.username,
      'profileImagePublicId': instance.profileImagePublicId,
      'gender': instance.gender,
      'birthDate': instance.birthDate,
      'preferredDietaryId': instance.preferredDietaryId,
      'marketingAgreed': instance.marketingAgreed,
      'locale': instance.locale,
      'defaultFoodStyle': instance.defaultFoodStyle,
      'bio': instance.bio,
      'youtubeUrl': instance.youtubeUrl,
      'instagramHandle': instance.instagramHandle,
    };
