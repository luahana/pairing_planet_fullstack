// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'step_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StepRequestDto _$StepRequestDtoFromJson(Map<String, dynamic> json) =>
    StepRequestDto(
      stepNumber: (json['stepNumber'] as num).toInt(),
      description: json['description'] as String,
      imageUrl: json['imageUrl'] as String?,
    );

Map<String, dynamic> _$StepRequestDtoToJson(StepRequestDto instance) =>
    <String, dynamic>{
      'stepNumber': instance.stepNumber,
      'description': instance.description,
      'imageUrl': instance.imageUrl,
    };
