import 'package:flutter_test/flutter_test.dart';
import 'package:pairing_planet2_frontend/domain/entities/log_post/log_post_summary.dart';
import 'package:pairing_planet2_frontend/features/log_post/providers/log_post_list_provider.dart';

void main() {
  group('LogPostListState', () {
    test('should create state with required fields', () {
      // Arrange & Act
      final state = LogPostListState(
        items: [],
        hasNext: true,
      );

      // Assert
      expect(state.items, isEmpty);
      expect(state.hasNext, isTrue);
      expect(state.searchQuery, isNull);
      expect(state.filterState, isNull);
      expect(state.recipeId, isNull);
    });

    test('should create state with all fields', () {
      // Arrange
      final logs = [
        LogPostSummary(
          id: 'log-1',
          title: 'Test Log',
          outcome: 'SUCCESS',
          thumbnailUrl: null,
          userName: 'user1',
        ),
      ];

      // Act
      final state = LogPostListState(
        items: logs,
        hasNext: false,
        searchQuery: 'test query',
        recipeId: 'recipe-123',
      );

      // Assert
      expect(state.items, hasLength(1));
      expect(state.hasNext, isFalse);
      expect(state.searchQuery, 'test query');
      expect(state.recipeId, 'recipe-123');
    });

    test('should store recipeId for recipe-filtered logs', () {
      // Arrange & Act
      final state = LogPostListState(
        items: [],
        hasNext: true,
        recipeId: 'my-recipe-id',
      );

      // Assert
      expect(state.recipeId, 'my-recipe-id');
    });
  });

  group('LogPostSummary', () {
    test('should create summary with all fields', () {
      // Arrange & Act
      final summary = LogPostSummary(
        id: 'log-123',
        title: 'My Cooking Log',
        outcome: 'SUCCESS',
        thumbnailUrl: 'https://example.com/thumb.jpg',
        userName: 'chef_user',
      );

      // Assert
      expect(summary.id, 'log-123');
      expect(summary.title, 'My Cooking Log');
      expect(summary.outcome, 'SUCCESS');
      expect(summary.thumbnailUrl, 'https://example.com/thumb.jpg');
      expect(summary.userName, 'chef_user');
    });

    test('should handle null thumbnailUrl', () {
      // Arrange & Act
      final summary = LogPostSummary(
        id: 'log-456',
        title: 'No Photo Log',
        outcome: 'PARTIAL',
        thumbnailUrl: null,
        userName: 'user',
      );

      // Assert
      expect(summary.thumbnailUrl, isNull);
    });

    test('should handle null userName', () {
      // Arrange & Act
      final summary = LogPostSummary(
        id: 'log-789',
        title: 'Anonymous Log',
        outcome: 'FAILED',
        thumbnailUrl: null,
        userName: null,
      );

      // Assert
      expect(summary.userName, isNull);
    });

    test('should support all outcome types', () {
      // Arrange & Act & Assert
      expect(
        LogPostSummary(
          id: '1',
          title: 'Success',
          outcome: 'SUCCESS',
          thumbnailUrl: null,
          userName: null,
        ).outcome,
        'SUCCESS',
      );

      expect(
        LogPostSummary(
          id: '2',
          title: 'Partial',
          outcome: 'PARTIAL',
          thumbnailUrl: null,
          userName: null,
        ).outcome,
        'PARTIAL',
      );

      expect(
        LogPostSummary(
          id: '3',
          title: 'Failed',
          outcome: 'FAILED',
          thumbnailUrl: null,
          userName: null,
        ).outcome,
        'FAILED',
      );
    });
  });

  group('Recipe Logs State Management', () {
    test('should differentiate recipe-filtered state from general state', () {
      // Arrange
      final generalState = LogPostListState(
        items: [],
        hasNext: true,
        searchQuery: 'search',
      );

      final recipeFilteredState = LogPostListState(
        items: [],
        hasNext: true,
        recipeId: 'recipe-123',
      );

      // Assert
      expect(generalState.recipeId, isNull);
      expect(generalState.searchQuery, isNotNull);

      expect(recipeFilteredState.recipeId, isNotNull);
      expect(recipeFilteredState.searchQuery, isNull);
    });

    test('should store multiple items in state', () {
      // Arrange
      final logs = List.generate(
        10,
        (i) => LogPostSummary(
          id: 'log-$i',
          title: 'Log $i',
          outcome: i % 2 == 0 ? 'SUCCESS' : 'PARTIAL',
          thumbnailUrl: null,
          userName: 'user$i',
        ),
      );

      // Act
      final state = LogPostListState(
        items: logs,
        hasNext: true,
        recipeId: 'recipe-123',
      );

      // Assert
      expect(state.items, hasLength(10));
      expect(state.items[0].id, 'log-0');
      expect(state.items[9].id, 'log-9');
    });

    test('hasNext should indicate more pages available', () {
      // Arrange & Act
      final stateWithMore = LogPostListState(
        items: [],
        hasNext: true,
      );

      final stateNoMore = LogPostListState(
        items: [],
        hasNext: false,
      );

      // Assert
      expect(stateWithMore.hasNext, isTrue);
      expect(stateNoMore.hasNext, isFalse);
    });
  });
}
