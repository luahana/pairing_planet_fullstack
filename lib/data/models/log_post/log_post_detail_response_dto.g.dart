// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'log_post_detail_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LogPostDetailResponseDto _$LogPostDetailResponseDtoFromJson(
  Map<String, dynamic> json,
) => LogPostDetailResponseDto(
  publicId: json['publicId'] as String,
  title: json['title'] as String,
  content: json['content'] as String,
  rating: (json['rating'] as num?)?.toInt(),
  imageUrls: (json['imageUrls'] as List<dynamic>)
      .map((e) => e as String)
      .toList(),
  linkedRecipe: RecipeSummaryDto.fromJson(
    json['linkedRecipe'] as Map<String, dynamic>,
  ),
);

Map<String, dynamic> _$LogPostDetailResponseDtoToJson(
  LogPostDetailResponseDto instance,
) => <String, dynamic>{
  'publicId': instance.publicId,
  'title': instance.title,
  'content': instance.content,
  'rating': instance.rating,
  'imageUrls': instance.imageUrls,
  'linkedRecipe': instance.linkedRecipe,
};
