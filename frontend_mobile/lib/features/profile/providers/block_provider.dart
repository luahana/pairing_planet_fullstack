import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:pairing_planet2_frontend/core/network/dio_provider.dart';
import 'package:pairing_planet2_frontend/data/datasources/block/block_remote_data_source.dart';
import 'package:pairing_planet2_frontend/data/models/block/block_status_dto.dart';
import 'package:pairing_planet2_frontend/data/models/block/blocked_user_dto.dart';
import 'package:pairing_planet2_frontend/data/models/report/report_reason.dart';

/// Provider for BlockRemoteDataSource
final blockRemoteDataSourceProvider = Provider<BlockRemoteDataSource>((ref) {
  return BlockRemoteDataSource(ref.read(dioProvider));
});

/// Block status provider - checks block relationship between users
final blockStatusProvider =
    FutureProvider.family<BlockStatusDto, String>((ref, userId) async {
  final dataSource = ref.read(blockRemoteDataSourceProvider);
  return dataSource.getBlockStatus(userId);
});

/// Block action state
class BlockActionState {
  final bool isLoading;
  final bool isBlocked;
  final bool amBlocked;
  final String? error;

  BlockActionState({
    this.isLoading = false,
    this.isBlocked = false,
    this.amBlocked = false,
    this.error,
  });

  BlockActionState copyWith({
    bool? isLoading,
    bool? isBlocked,
    bool? amBlocked,
    String? error,
  }) {
    return BlockActionState(
      isLoading: isLoading ?? this.isLoading,
      isBlocked: isBlocked ?? this.isBlocked,
      amBlocked: amBlocked ?? this.amBlocked,
      error: error,
    );
  }
}

/// Block action notifier - handles block/unblock with optimistic updates
class BlockActionNotifier extends StateNotifier<BlockActionState> {
  final BlockRemoteDataSource _dataSource;
  final String _userId;

  BlockActionNotifier({
    required BlockRemoteDataSource dataSource,
    required String userId,
    required bool initialBlockedState,
    required bool initialAmBlockedState,
  })  : _dataSource = dataSource,
        _userId = userId,
        super(BlockActionState(
          isBlocked: initialBlockedState,
          amBlocked: initialAmBlockedState,
        ));

  Future<bool> toggleBlock() async {
    if (state.isLoading) return false;

    final wasBlocked = state.isBlocked;

    // Optimistic update
    state = state.copyWith(
      isLoading: true,
      isBlocked: !wasBlocked,
      error: null,
    );

    try {
      if (wasBlocked) {
        await _dataSource.unblockUser(_userId);
      } else {
        await _dataSource.blockUser(_userId);
      }
      state = state.copyWith(isLoading: false);
      return true;
    } catch (e) {
      // Revert on error
      state = state.copyWith(
        isLoading: false,
        isBlocked: wasBlocked,
        error: e.toString(),
      );
      return false;
    }
  }
}

/// Provider for block action notifier
final blockActionProvider = StateNotifierProvider.family<BlockActionNotifier,
    BlockActionState, ({String userId, bool isBlocked, bool amBlocked})>(
  (ref, params) {
    return BlockActionNotifier(
      dataSource: ref.read(blockRemoteDataSourceProvider),
      userId: params.userId,
      initialBlockedState: params.isBlocked,
      initialAmBlockedState: params.amBlocked,
    );
  },
);

/// Report action state
class ReportActionState {
  final bool isLoading;
  final bool isReported;
  final String? error;

  ReportActionState({
    this.isLoading = false,
    this.isReported = false,
    this.error,
  });

  ReportActionState copyWith({
    bool? isLoading,
    bool? isReported,
    String? error,
  }) {
    return ReportActionState(
      isLoading: isLoading ?? this.isLoading,
      isReported: isReported ?? this.isReported,
      error: error,
    );
  }
}

/// Report action notifier
class ReportActionNotifier extends StateNotifier<ReportActionState> {
  final BlockRemoteDataSource _dataSource;
  final String _userId;

  ReportActionNotifier({
    required BlockRemoteDataSource dataSource,
    required String userId,
  })  : _dataSource = dataSource,
        _userId = userId,
        super(ReportActionState());

  Future<bool> report(ReportReason reason, {String? description}) async {
    if (state.isLoading || state.isReported) return false;

    state = state.copyWith(isLoading: true, error: null);

    try {
      await _dataSource.reportUser(_userId, reason, description: description);
      state = state.copyWith(isLoading: false, isReported: true);
      return true;
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: e.toString(),
      );
      return false;
    }
  }
}

/// Provider for report action notifier
final reportActionProvider =
    StateNotifierProvider.family<ReportActionNotifier, ReportActionState, String>(
  (ref, userId) {
    return ReportActionNotifier(
      dataSource: ref.read(blockRemoteDataSourceProvider),
      userId: userId,
    );
  },
);

/// Blocked users list state
class BlockedUsersListState {
  final List<BlockedUserDto> items;
  final bool hasNext;
  final int currentPage;
  final bool isLoading;
  final String? error;

  BlockedUsersListState({
    this.items = const [],
    this.hasNext = true,
    this.currentPage = 0,
    this.isLoading = false,
    this.error,
  });

  BlockedUsersListState copyWith({
    List<BlockedUserDto>? items,
    bool? hasNext,
    int? currentPage,
    bool? isLoading,
    String? error,
  }) {
    return BlockedUsersListState(
      items: items ?? this.items,
      hasNext: hasNext ?? this.hasNext,
      currentPage: currentPage ?? this.currentPage,
      isLoading: isLoading ?? this.isLoading,
      error: error,
    );
  }
}

/// Blocked users list notifier
class BlockedUsersListNotifier extends StateNotifier<BlockedUsersListState> {
  final BlockRemoteDataSource _dataSource;

  BlockedUsersListNotifier({
    required BlockRemoteDataSource dataSource,
  })  : _dataSource = dataSource,
        super(BlockedUsersListState(isLoading: true)) {
    _init();
  }

  Future<void> _init() async {
    await _fetchPage(0);
  }

  Future<void> _fetchPage(int page) async {
    if (state.isLoading && page > 0) return;

    state = state.copyWith(isLoading: true, error: null);

    try {
      final response = await _dataSource.getBlockedUsers(page: page);

      if (page == 0) {
        state = state.copyWith(
          items: response.content,
          hasNext: response.hasNext,
          currentPage: page,
          isLoading: false,
        );
      } else {
        state = state.copyWith(
          items: [...state.items, ...response.content],
          hasNext: response.hasNext,
          currentPage: page,
          isLoading: false,
        );
      }
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: e.toString(),
      );
    }
  }

  Future<void> fetchNextPage() async {
    if (!state.hasNext || state.isLoading) return;
    await _fetchPage(state.currentPage + 1);
  }

  Future<void> refresh() async {
    state = BlockedUsersListState(isLoading: true);
    await _fetchPage(0);
  }

  void removeBlockedUser(String userId) {
    final updatedItems =
        state.items.where((item) => item.publicId != userId).toList();
    state = state.copyWith(items: updatedItems);
  }
}

/// Provider for blocked users list
final blockedUsersListProvider =
    StateNotifierProvider<BlockedUsersListNotifier, BlockedUsersListState>(
  (ref) {
    return BlockedUsersListNotifier(
      dataSource: ref.read(blockRemoteDataSourceProvider),
    );
  },
);
