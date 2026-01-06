import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:pairing_planet2_frontend/domain/entities/log_post/log_post_summary.dart';
import 'package:pairing_planet2_frontend/features/log_post/providers/log_post_providers.dart';

class LogPostListState {
  final List<LogPostSummary> items;
  final bool hasNext;

  LogPostListState({
    required this.items,
    required this.hasNext,
  });
}

class LogPostListNotifier extends AsyncNotifier<LogPostListState> {
  int _currentPage = 0;
  bool _hasNext = true;
  bool _isFetchingNext = false;

  @override
  Future<LogPostListState> build() async {
    _currentPage = 0;
    _hasNext = true;
    final items = await _fetchPage(0);
    return LogPostListState(items: items, hasNext: _hasNext);
  }

  Future<List<LogPostSummary>> _fetchPage(int page) async {
    final useCase = ref.read(getLogPostListUseCaseProvider);
    final result = await useCase(page: page, size: 20);

    return result.fold(
      (failure) => throw failure,
      (sliceResponse) {
        _hasNext = sliceResponse.hasNext;
        return sliceResponse.content;
      },
    );
  }

  Future<void> fetchNextPage() async {
    if (_isFetchingNext || !_hasNext) return;

    _isFetchingNext = true;
    try {
      final currentState = state.value;
      if (currentState == null) return;

      _currentPage++;
      final newItems = await _fetchPage(_currentPage);

      final allItems = [...currentState.items, ...newItems];
      final uniqueItems = <String, LogPostSummary>{};
      for (final item in allItems) {
        uniqueItems[item.id] = item;
      }

      state = AsyncValue.data(LogPostListState(
        items: uniqueItems.values.toList(),
        hasNext: _hasNext,
      ));
    } finally {
      _isFetchingNext = false;
    }
  }

  Future<void> refresh() async {
    _currentPage = 0;
    _hasNext = true;
    ref.invalidateSelf();
  }
}

final logPostPaginatedListProvider = AsyncNotifierProvider<LogPostListNotifier, LogPostListState>(() {
  return LogPostListNotifier();
});
