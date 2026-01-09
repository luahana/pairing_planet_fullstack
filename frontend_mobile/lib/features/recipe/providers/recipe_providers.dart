import 'dart:async';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:dartz/dartz.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:pairing_planet2_frontend/core/error/failures.dart';
import 'package:pairing_planet2_frontend/core/network/network_info.dart';
import 'package:pairing_planet2_frontend/core/network/network_info_impl.dart';
import 'package:pairing_planet2_frontend/core/providers/analytics_providers.dart';
import 'package:pairing_planet2_frontend/core/providers/recently_viewed_provider.dart';
import 'package:pairing_planet2_frontend/domain/entities/analytics/app_event.dart';
import 'package:pairing_planet2_frontend/domain/entities/common/slice_response.dart';
import 'package:pairing_planet2_frontend/domain/entities/recipe/create_recipe_request.dart';
import 'package:pairing_planet2_frontend/domain/entities/recipe/recipe_draft.dart';
import 'package:pairing_planet2_frontend/domain/entities/recipe/recipe_summary.dart';
import 'package:pairing_planet2_frontend/domain/repositories/analytics_repository.dart';
import 'package:pairing_planet2_frontend/domain/usecases/recipe/create_recipe_usecase.dart';
import 'package:pairing_planet2_frontend/domain/usecases/recipe/get_recipe_detail.dart';
import 'package:pairing_planet2_frontend/data/datasources/recipe/recipe_local_data_source.dart';
import 'package:pairing_planet2_frontend/data/datasources/recipe/recipe_draft_local_data_source.dart';
import 'package:pairing_planet2_frontend/data/models/recipe/recipe_draft_dto.dart';
import 'package:pairing_planet2_frontend/domain/entities/recipe/recipe_detail.dart';
import 'package:uuid/uuid.dart';
import '../../../core/network/dio_provider.dart';
import '../../../data/datasources/recipe/recipe_remote_data_source.dart';
import '../../../data/repositories/recipe_repository_impl.dart';
import '../../../domain/repositories/recipe_repository.dart';

// ----------------------------------------------------------------
// 1. 기초 인프라 (Network & Storage) Providers
// ----------------------------------------------------------------

// 네트워크 연결 상태 확인 도구 (Connectivity 패키지)
final connectivityProvider = Provider<Connectivity>((ref) => Connectivity());

// 클린 아키텍처를 위한 NetworkInfo 인터페이스 구현체 주입
final networkInfoProvider = Provider<NetworkInfo>((ref) {
  final connectivity = ref.read(connectivityProvider);
  return NetworkInfoImpl(connectivity);
});

// 로컬 캐싱을 담당하는 데이터 소스
final recipeLocalDataSourceProvider = Provider<RecipeLocalDataSource>((ref) {
  return RecipeLocalDataSource();
});

// ----------------------------------------------------------------
// 2. Data Layer Providers
// ----------------------------------------------------------------

// 백엔드 API와 직접 통신하는 리모트 데이터 소스
final recipeRemoteDataSourceProvider = Provider<RecipeRemoteDataSource>((ref) {
  final dio = ref.read(dioProvider);
  return RecipeRemoteDataSource(dio);
});

// 리포지토리 구현체: Remote, Local, NetworkInfo를 모두 조합하여 데이터 흐름 제어
final recipeRepositoryProvider = Provider<RecipeRepository>((ref) {
  final remoteDataSource = ref.read(recipeRemoteDataSourceProvider);
  final localDataSource = ref.read(recipeLocalDataSourceProvider);
  final networkInfo = ref.read(networkInfoProvider);

  return RecipeRepositoryImpl(
    remoteDataSource: remoteDataSource,
    localDataSource: localDataSource,
    networkInfo: networkInfo,
  );
});

// ----------------------------------------------------------------
// 3. Domain Layer Providers (UseCase)
// ----------------------------------------------------------------

// 비즈니스 로직을 담당하는 UseCase
final getRecipeDetailUseCaseProvider = Provider<GetRecipeDetailUseCase>((ref) {
  final repository = ref.read(recipeRepositoryProvider);
  return GetRecipeDetailUseCase(repository);
});

// ----------------------------------------------------------------
// 4. Presentation Layer Providers (State)
// ----------------------------------------------------------------

// UI에서 'ref.watch(recipeDetailProvider(id))'로 간단히 데이터를 불러올 때 사용
final recipeDetailProvider = FutureProvider.family<RecipeDetail, String>((
  ref,
  id,
) async {
  final useCase = ref.watch(getRecipeDetailUseCaseProvider);
  final result = await useCase(id);

  // Either 타입을 폴드(fold)하여 실패 시 에러를 던지고, 성공 시 데이터를 반환
  return result.fold((failure) => throw failure.message, (recipe) => recipe);
});

final createRecipeUseCaseProvider = Provider<CreateRecipeUseCase>((ref) {
  final repository = ref.read(recipeRepositoryProvider);
  return CreateRecipeUseCase(repository);
});

final recipesProvider =
    FutureProvider.family<SliceResponse<RecipeSummary>, int>((
      ref,
      page,
    ) async {
      final repository = ref.watch(recipeRepositoryProvider);

      // 💡 리포지토리의 getRecipes 호출
      final result = await repository.getRecipes(page: page, size: 10);

      // Either 타입을 처리하여 성공 시 데이터를 반환하고, 실패 시 에러를 던집니다.
      return result.fold(
        (failure) => throw failure,
        (sliceResponse) => sliceResponse,
      );
    });

// ----------------------------------------------------------------
// 5. Recipe Creation with Analytics
// ----------------------------------------------------------------

final recipeCreationProvider =
    StateNotifierProvider<RecipeCreationNotifier, AsyncValue<String?>>((ref) {
  return RecipeCreationNotifier(
    ref.read(createRecipeUseCaseProvider),
    ref.read(analyticsRepositoryProvider),
  );
});

class RecipeCreationNotifier extends StateNotifier<AsyncValue<String?>> {
  final CreateRecipeUseCase _useCase;
  final AnalyticsRepository _analyticsRepository;

  RecipeCreationNotifier(this._useCase, this._analyticsRepository)
      : super(const AsyncValue.data(null));

  Future<void> createRecipe(CreateRecipeRequest request) async {
    state = const AsyncValue.loading();
    final result = await _useCase.execute(request);

    state = result.fold(
      (failure) {
        // Don't track creation failures (network errors, validation, etc.)
        return AsyncValue.error(failure.message, StackTrace.current);
      },
      (recipeId) {
        // Determine if this is a variation or new recipe
        final isVariation = request.parentPublicId != null;

        // Track recipe creation success event
        _analyticsRepository.trackEvent(AppEvent(
          eventId: const Uuid().v4(),
          eventType: isVariation ? EventType.variationCreated : EventType.recipeCreated,
          timestamp: DateTime.now(),
          priority: EventPriority.immediate,
          recipeId: recipeId,
          properties: {
            'ingredient_count': request.ingredients.length,
            'step_count': request.steps.length,
            'has_images': request.imagePublicIds.isNotEmpty,
            'image_count': request.imagePublicIds.length,
            if (isVariation) 'parent_recipe_id': request.parentPublicId,
            if (isVariation && request.rootPublicId != null)
              'root_recipe_id': request.rootPublicId,
            if (isVariation) 'change_category': request.changeCategory ?? '',
          },
        ));

        return AsyncValue.data(recipeId);
      },
    );
  }
}

// ----------------------------------------------------------------
// 6. Recipe Detail with View Tracking
// ----------------------------------------------------------------

final recipeDetailWithTrackingProvider =
    FutureProvider.family<RecipeDetail, String>((ref, id) async {
  final useCase = ref.watch(getRecipeDetailUseCaseProvider);
  final analyticsRepo = ref.read(analyticsRepositoryProvider);

  final result = await useCase(id);

  return result.fold(
    (failure) => throw failure.message,
    (recipe) {
      // Track recipe view event
      analyticsRepo.trackEvent(AppEvent(
        eventId: const Uuid().v4(),
        eventType: EventType.recipeViewed,
        timestamp: DateTime.now(),
        priority: EventPriority.batched,
        recipeId: recipe.publicId,
        properties: {
          'has_parent': recipe.parentInfo != null,
          'has_root': recipe.rootInfo != null,
          'ingredient_count': recipe.ingredients.length,
          'step_count': recipe.steps.length,
        },
      ));

      // Add to recently viewed recipes for quick log picker
      ref.read(recentlyViewedRecipesProvider.notifier).addRecipe(
            publicId: recipe.publicId,
            title: recipe.title,
            foodName: recipe.foodName,
            thumbnailUrl:
                recipe.imageUrls.isNotEmpty ? recipe.imageUrls.first : null,
          );

      return recipe;
    },
  );
});

// ----------------------------------------------------------------
// 7. Recipe Save/Bookmark (P1)
// ----------------------------------------------------------------

/// 북마크 상태를 관리하는 StateNotifier
class SaveRecipeNotifier extends StateNotifier<AsyncValue<bool>> {
  final RecipeRepository _repository;
  final String _recipeId;

  SaveRecipeNotifier(this._repository, this._recipeId)
      : super(const AsyncValue.data(false));

  /// 초기 저장 상태 설정 (API에서 받은 값으로)
  void setInitialState(bool isSaved) {
    state = AsyncValue.data(isSaved);
  }

  /// 저장/저장취소 토글
  Future<void> toggle() async {
    final currentlySaved = state.value ?? false;
    state = const AsyncValue.loading();

    final result = currentlySaved
        ? await _repository.unsaveRecipe(_recipeId)
        : await _repository.saveRecipe(_recipeId);

    state = result.fold(
      (failure) => AsyncValue.error(failure.message, StackTrace.current),
      (_) => AsyncValue.data(!currentlySaved),
    );
  }
}

/// 북마크 상태 Provider (레시피 ID별로 생성)
final saveRecipeProvider =
    StateNotifierProvider.family<SaveRecipeNotifier, AsyncValue<bool>, String>(
  (ref, recipeId) {
    final repository = ref.read(recipeRepositoryProvider);
    return SaveRecipeNotifier(repository, recipeId);
  },
);

// ----------------------------------------------------------------
// Recipe Delete
// ----------------------------------------------------------------

/// Delete recipe provider (레시피 ID별로 생성)
final deleteRecipeProvider =
    FutureProvider.family<Either<Failure, void>, String>((ref, recipeId) async {
  final repository = ref.read(recipeRepositoryProvider);
  return repository.deleteRecipe(recipeId);
});

// ----------------------------------------------------------------
// 8. Recipe Draft Auto-Save (FEAT-021)
// ----------------------------------------------------------------

/// Draft local data source provider
final recipeDraftLocalDataSourceProvider =
    Provider<RecipeDraftLocalDataSource>((ref) {
  return RecipeDraftLocalDataSource();
});

/// Draft save status enum
enum DraftSaveStatus { idle, saving, saved, error }

/// State class for draft management
class RecipeDraftState {
  final RecipeDraft? draft;
  final DraftSaveStatus saveStatus;
  final DateTime? lastSavedAt;
  final String? error;

  const RecipeDraftState({
    this.draft,
    this.saveStatus = DraftSaveStatus.idle,
    this.lastSavedAt,
    this.error,
  });

  RecipeDraftState copyWith({
    RecipeDraft? draft,
    DraftSaveStatus? saveStatus,
    DateTime? lastSavedAt,
    String? error,
  }) {
    return RecipeDraftState(
      draft: draft ?? this.draft,
      saveStatus: saveStatus ?? this.saveStatus,
      lastSavedAt: lastSavedAt ?? this.lastSavedAt,
      error: error,
    );
  }
}

/// StateNotifier for draft operations
class RecipeDraftNotifier extends StateNotifier<RecipeDraftState> {
  final RecipeDraftLocalDataSource _localDataSource;
  Timer? _autoSaveTimer;
  bool _isSaving = false;
  RecipeDraft? _lastSavedDraft; // Track last saved draft to detect changes

  RecipeDraftNotifier(this._localDataSource) : super(const RecipeDraftState());

  /// Start auto-save timer (30 second interval)
  void startAutoSave(RecipeDraft Function() getCurrentDraft) {
    _autoSaveTimer?.cancel();
    _autoSaveTimer = Timer.periodic(const Duration(seconds: 30), (_) {
      final draft = getCurrentDraft();
      // Only save if content exists AND has changed since last save
      if (draft.hasContent && draft != _lastSavedDraft) {
        saveDraft(draft);
      }
    });
  }

  /// Stop auto-save timer
  void stopAutoSave() {
    _autoSaveTimer?.cancel();
    _autoSaveTimer = null;
  }

  /// Save draft to local storage
  Future<void> saveDraft(RecipeDraft draft) async {
    // Prevent concurrent saves
    if (_isSaving) return;

    // Don't save empty drafts
    if (!draft.hasContent) return;

    _isSaving = true;
    state = state.copyWith(
      draft: draft,
      saveStatus: DraftSaveStatus.saving,
    );

    try {
      await _localDataSource.saveDraft(RecipeDraftDto.fromEntity(draft));
      _lastSavedDraft = draft; // Update last saved draft after successful save
      state = state.copyWith(
        draft: draft,
        saveStatus: DraftSaveStatus.saved,
        lastSavedAt: DateTime.now(),
      );
    } catch (e) {
      state = state.copyWith(
        draft: draft,
        saveStatus: DraftSaveStatus.error,
        error: e.toString(),
      );
    } finally {
      _isSaving = false;
    }
  }

  /// Load draft from local storage
  Future<RecipeDraft?> loadDraft() async {
    final draftDto = await _localDataSource.getDraft();
    if (draftDto != null) {
      final draft = draftDto.toEntity();

      // Don't restore drafts with no meaningful content
      if (!draft.hasContent) {
        await _localDataSource.clearDraft();
        return null;
      }

      _lastSavedDraft = draft; // Track loaded draft as baseline
      state = RecipeDraftState(draft: draft);
      return draft;
    }
    return null;
  }

  /// Check if a draft exists
  Future<bool> hasDraft() async {
    return await _localDataSource.hasDraft();
  }

  /// Clear the draft from local storage
  Future<void> clearDraft() async {
    await _localDataSource.clearDraft();
    _lastSavedDraft = null; // Clear baseline when draft is cleared
    state = const RecipeDraftState();
  }

  /// Reset state to idle (e.g., after status indicator fades)
  void resetStatus() {
    state = state.copyWith(saveStatus: DraftSaveStatus.idle);
  }

  @override
  void dispose() {
    stopAutoSave();
    super.dispose();
  }
}

/// Recipe draft provider
final recipeDraftProvider =
    StateNotifierProvider<RecipeDraftNotifier, RecipeDraftState>((ref) {
  return RecipeDraftNotifier(ref.read(recipeDraftLocalDataSourceProvider));
});
