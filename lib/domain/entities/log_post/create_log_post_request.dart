class CreateLogPostRequest {
  final String? title;
  final String content;
  final int rating;
  final String recipePublicId;
  final List<String> imagePublicIds;

  CreateLogPostRequest({
    this.title,
    required this.content,
    required this.rating,
    required this.recipePublicId,
    required this.imagePublicIds,
  });
}
