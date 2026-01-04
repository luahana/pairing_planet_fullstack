// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trending_tree_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TrendingTreeDto _$TrendingTreeDtoFromJson(Map<String, dynamic> json) =>
    TrendingTreeDto(
      rootRecipeId: json['rootRecipeId'] as String,
      title: json['title'] as String,
      culinaryLocale: json['culinaryLocale'] as String,
      variantCount: (json['variantCount'] as num).toInt(),
      logCount: (json['logCount'] as num).toInt(),
      latestChangeSummary: json['latestChangeSummary'] as String?,
    );

Map<String, dynamic> _$TrendingTreeDtoToJson(TrendingTreeDto instance) =>
    <String, dynamic>{
      'rootRecipeId': instance.rootRecipeId,
      'title': instance.title,
      'culinaryLocale': instance.culinaryLocale,
      'variantCount': instance.variantCount,
      'logCount': instance.logCount,
      'latestChangeSummary': instance.latestChangeSummary,
    };
