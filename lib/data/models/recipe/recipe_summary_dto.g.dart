// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'recipe_summary_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecipeSummaryDto _$RecipeSummaryDtoFromJson(Map<String, dynamic> json) =>
    RecipeSummaryDto(
      publicId: json['publicId'] as String,
      title: json['title'] as String,
      culinaryLocale: json['culinaryLocale'] as String,
      creatorName: json['creatorName'] as String?,
      thumbnail: json['thumbnail'] as String?,
    );

Map<String, dynamic> _$RecipeSummaryDtoToJson(RecipeSummaryDto instance) =>
    <String, dynamic>{
      'publicId': instance.publicId,
      'title': instance.title,
      'culinaryLocale': instance.culinaryLocale,
      'creatorName': instance.creatorName,
      'thumbnail': instance.thumbnail,
    };
