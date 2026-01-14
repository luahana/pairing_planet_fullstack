// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'block_status_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BlockStatusDto _$BlockStatusDtoFromJson(Map<String, dynamic> json) =>
    BlockStatusDto(
      isBlocked: json['isBlocked'] as bool,
      amBlocked: json['amBlocked'] as bool,
    );

Map<String, dynamic> _$BlockStatusDtoToJson(BlockStatusDto instance) =>
    <String, dynamic>{
      'isBlocked': instance.isBlocked,
      'amBlocked': instance.amBlocked,
    };
