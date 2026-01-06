class SliceResponse<T> {
  final List<T> content;
  final int number;
  final int size;
  final bool first;
  final bool last;
  final bool hasNext;

  SliceResponse({
    required this.content,
    required this.number,
    required this.size,
    required this.first,
    required this.last,
    required this.hasNext,
  });
}
