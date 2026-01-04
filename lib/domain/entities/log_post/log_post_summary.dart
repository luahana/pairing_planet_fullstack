class LogPostSummary {
  final String id;
  final String title;
  final int? rating;
  final String? thumbnailUrl;
  final String? creatorName;

  LogPostSummary({
    required this.id,
    required this.title,
    this.rating,
    this.thumbnailUrl,
    this.creatorName,
  });
}
