// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'blocked_user_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BlockedUserDto _$BlockedUserDtoFromJson(Map<String, dynamic> json) =>
    BlockedUserDto(
      publicId: json['publicId'] as String,
      username: json['username'] as String,
      profileImageUrl: json['profileImageUrl'] as String?,
      blockedAt: json['blockedAt'] as String?,
    );

Map<String, dynamic> _$BlockedUserDtoToJson(BlockedUserDto instance) =>
    <String, dynamic>{
      'publicId': instance.publicId,
      'username': instance.username,
      'profileImageUrl': instance.profileImageUrl,
      'blockedAt': instance.blockedAt,
    };

BlockedUsersListResponse _$BlockedUsersListResponseFromJson(
        Map<String, dynamic> json) =>
    BlockedUsersListResponse(
      content: (json['content'] as List<dynamic>)
          .map((e) => BlockedUserDto.fromJson(e as Map<String, dynamic>))
          .toList(),
      hasNext: json['hasNext'] as bool,
      page: (json['page'] as num).toInt(),
      size: (json['size'] as num).toInt(),
      totalElements: (json['totalElements'] as num).toInt(),
    );

Map<String, dynamic> _$BlockedUsersListResponseToJson(
        BlockedUsersListResponse instance) =>
    <String, dynamic>{
      'content': instance.content,
      'hasNext': instance.hasNext,
      'page': instance.page,
      'size': instance.size,
      'totalElements': instance.totalElements,
    };
