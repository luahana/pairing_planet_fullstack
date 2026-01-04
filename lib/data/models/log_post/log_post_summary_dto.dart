import 'package:json_annotation/json_annotation.dart';
import 'package:pairing_planet2_frontend/domain/entities/log_post/log_post_summary.dart';

part 'log_post_summary_dto.g.dart';

@JsonSerializable()
class LogPostSummaryDto {
  final String publicId;
  final String title;
  final int? rating;
  final String? thumbnail;
  final String? creatorName;

  LogPostSummaryDto({
    required this.publicId,
    required this.title,
    this.rating,
    this.thumbnail,
    this.creatorName,
  });

  factory LogPostSummaryDto.fromJson(Map<String, dynamic> json) =>
      _$LogPostSummaryDtoFromJson(json);
  Map<String, dynamic> toJson() => _$LogPostSummaryDtoToJson(this);

  LogPostSummary toEntity() {
    return LogPostSummary(
      id: publicId,
      title: title,
      rating: rating,
      thumbnail: thumbnail,
      creatorName: creatorName,
    );
  }
}
