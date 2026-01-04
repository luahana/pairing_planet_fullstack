// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingredient_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngredientRequestDto _$IngredientRequestDtoFromJson(
  Map<String, dynamic> json,
) => IngredientRequestDto(
  name: json['name'] as String,
  amount: json['amount'] as String,
  type: $enumDecode(_$IngredientTypeEnumMap, json['type']),
);

Map<String, dynamic> _$IngredientRequestDtoToJson(
  IngredientRequestDto instance,
) => <String, dynamic>{
  'name': instance.name,
  'amount': instance.amount,
  'type': _$IngredientTypeEnumMap[instance.type]!,
};

const _$IngredientTypeEnumMap = {
  IngredientType.MAIN: 'MAIN',
  IngredientType.SUB: 'SUB',
  IngredientType.SEASONING: 'SEASONING',
};
