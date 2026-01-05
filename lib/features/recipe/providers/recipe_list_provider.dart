import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:pairing_planet2_frontend/features/recipe/providers/recipe_providers.dart';
import '../../../domain/entities/recipe/recipe_summary.dart';

// 💡 데이터와 다음 페이지 유무를 함께 관리하기 위한 상태 클래스 추가
class RecipeListState {
  final List<RecipeSummary> items;
  final bool hasNext;

  RecipeListState({required this.items, required this.hasNext});
}

class RecipeListNotifier extends AsyncNotifier<RecipeListState> {
  int _currentPage = 0;
  bool _hasNext = true;
  bool _isFetchingNext = false;

  @override
  Future<RecipeListState> build() async {
    // 💡 초기화 로직
    _currentPage = 0;
    _hasNext = true;
    _isFetchingNext = false;

    final items = await _fetchRecipes(page: _currentPage);
    // 💡 초기 상태에 현재 리스트와 hasNext 정보를 함께 담아 반환합니다.
    return RecipeListState(items: items, hasNext: _hasNext);
  }

  Future<List<RecipeSummary>> _fetchRecipes({required int page}) async {
    final repository = ref.read(recipeRepositoryProvider);
    final result = await repository.getRecipes(page: page, size: 10);

    return result.fold((failure) => throw failure, (pagedResponse) {
      _hasNext = pagedResponse.hasNext; // 💡 서버 응답에서 다음 페이지 유무 확인
      return pagedResponse.items;
    });
  }

  /// 다음 페이지 로드
  Future<void> fetchNextPage() async {
    // 💡 이미 데이터를 가져오는 중이거나 다음 페이지가 없으면 중단합니다.
    if (_isFetchingNext || !_hasNext) return;

    _isFetchingNext = true;
    final nextPage = _currentPage + 1;

    final result = await ref
        .read(recipeRepositoryProvider)
        .getRecipes(page: nextPage, size: 10);

    result.fold(
      (failure) {
        _isFetchingNext = false;
      },
      (pagedResponse) {
        _currentPage = nextPage;
        _hasNext = pagedResponse.hasNext;
        _isFetchingNext = false;

        // 💡 기존 리스트에 새 데이터를 붙이고, 최신 hasNext 상태를 업데이트합니다.
        final previousState = state.value;
        final previousItems = previousState?.items ?? [];

        state = AsyncValue.data(
          RecipeListState(
            items: [...previousItems, ...pagedResponse.items],
            hasNext: _hasNext,
          ),
        );
      },
    );
  }
}

final recipeListProvider =
    AsyncNotifierProvider<RecipeListNotifier, RecipeListState>(
      RecipeListNotifier.new,
    );
