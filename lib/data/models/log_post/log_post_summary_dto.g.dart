// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'log_post_summary_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LogPostSummaryDto _$LogPostSummaryDtoFromJson(Map<String, dynamic> json) =>
    LogPostSummaryDto(
      publicId: json['publicId'] as String,
      title: json['title'] as String?,
      rating: (json['rating'] as num?)?.toInt(),
      thumbnailUrl: json['thumbnailUrl'] as String?,
      creatorName: json['creatorName'] as String?,
    );

Map<String, dynamic> _$LogPostSummaryDtoToJson(LogPostSummaryDto instance) =>
    <String, dynamic>{
      'publicId': instance.publicId,
      'title': instance.title,
      'rating': instance.rating,
      'thumbnailUrl': instance.thumbnailUrl,
      'creatorName': instance.creatorName,
    };
