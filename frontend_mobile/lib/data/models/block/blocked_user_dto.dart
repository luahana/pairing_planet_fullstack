import 'package:json_annotation/json_annotation.dart';

part 'blocked_user_dto.g.dart';

@JsonSerializable()
class BlockedUserDto {
  final String publicId;
  final String username;
  final String? profileImageUrl;
  final String? blockedAt;

  BlockedUserDto({
    required this.publicId,
    required this.username,
    this.profileImageUrl,
    this.blockedAt,
  });

  factory BlockedUserDto.fromJson(Map<String, dynamic> json) =>
      _$BlockedUserDtoFromJson(json);
  Map<String, dynamic> toJson() => _$BlockedUserDtoToJson(this);
}

@JsonSerializable()
class BlockedUsersListResponse {
  final List<BlockedUserDto> content;
  final bool hasNext;
  final int page;
  final int size;
  final int totalElements;

  BlockedUsersListResponse({
    required this.content,
    required this.hasNext,
    required this.page,
    required this.size,
    required this.totalElements,
  });

  factory BlockedUsersListResponse.fromJson(Map<String, dynamic> json) =>
      _$BlockedUsersListResponseFromJson(json);
  Map<String, dynamic> toJson() => _$BlockedUsersListResponseToJson(this);
}
