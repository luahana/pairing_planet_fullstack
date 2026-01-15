import 'package:flutter_test/flutter_test.dart';
import 'package:pairing_planet2_frontend/data/models/log_post/log_post_summary_dto.dart';
import 'package:pairing_planet2_frontend/data/models/sync/sync_queue_item.dart';

void main() {
  group('LogPostSummaryDto', () {
    test('should create DTO with default isPending as false', () {
      // Act
      final dto = LogPostSummaryDto(
        publicId: 'test-id',
        title: 'Test Log',
        outcome: 'SUCCESS',
        thumbnailUrl: 'https://example.com/image.jpg',
        userName: 'testuser',
      );

      // Assert
      expect(dto.publicId, 'test-id');
      expect(dto.title, 'Test Log');
      expect(dto.outcome, 'SUCCESS');
      expect(dto.thumbnailUrl, 'https://example.com/image.jpg');
      expect(dto.userName, 'testuser');
      expect(dto.isPending, isFalse);
    });

    test('should create DTO with isPending true', () {
      // Act
      final dto = LogPostSummaryDto(
        publicId: 'pending-id',
        title: 'Pending Log',
        isPending: true,
      );

      // Assert
      expect(dto.isPending, isTrue);
    });

    test('fromSyncQueueItem should create DTO with isPending true', () {
      // Arrange - content and outcome are required fields
      final syncItem = SyncQueueItem(
        id: 'sync-123',
        localId: 'local-456',
        type: SyncOperationType.createLogPost,
        payload:
            '{"title":"Quick Log","content":"Test content","outcome":"SUCCESS","localPhotoPaths":["/path/to/photo.jpg"],"recipePublicId":"recipe-789"}',
        status: SyncStatus.pending,
        createdAt: DateTime(2024, 1, 15, 10, 30),
        retryCount: 0,
      );

      // Act
      final dto = LogPostSummaryDto.fromSyncQueueItem(syncItem);

      // Assert
      expect(dto.publicId, 'local-456'); // Uses localId when available
      expect(dto.title, 'Quick Log');
      expect(dto.outcome, 'SUCCESS');
      expect(dto.thumbnailUrl, 'file:///path/to/photo.jpg');
      expect(dto.userName, isNull);
      expect(dto.isPending, isTrue);
    });

    test('fromSyncQueueItem should use id when localId is null', () {
      // Arrange - content and outcome are required fields
      final syncItem = SyncQueueItem(
        id: 'sync-123',
        type: SyncOperationType.createLogPost,
        payload: '{"title":"Test","content":"Test content","outcome":"SUCCESS","localPhotoPaths":[]}',
        status: SyncStatus.pending,
        createdAt: DateTime.now(),
        retryCount: 0,
      );

      // Act
      final dto = LogPostSummaryDto.fromSyncQueueItem(syncItem);

      // Assert
      expect(dto.publicId, 'sync-123'); // Falls back to id
    });

    test('fromSyncQueueItem should use default title when null', () {
      // Arrange - content and outcome are required fields
      final syncItem = SyncQueueItem(
        id: 'sync-123',
        type: SyncOperationType.createLogPost,
        payload: '{"content":"Some content","outcome":"PARTIAL","localPhotoPaths":["/path/photo.jpg"]}',
        status: SyncStatus.pending,
        createdAt: DateTime.now(),
        retryCount: 0,
      );

      // Act
      final dto = LogPostSummaryDto.fromSyncQueueItem(syncItem);

      // Assert
      expect(dto.title, 'Quick Log'); // Default title
    });

    test('fromSyncQueueItem should handle empty photo paths', () {
      // Arrange - content and outcome are required fields
      final syncItem = SyncQueueItem(
        id: 'sync-123',
        type: SyncOperationType.createLogPost,
        payload: '{"title":"No Photo Log","content":"Test","outcome":"FAILED","localPhotoPaths":[]}',
        status: SyncStatus.pending,
        createdAt: DateTime.now(),
        retryCount: 0,
      );

      // Act
      final dto = LogPostSummaryDto.fromSyncQueueItem(syncItem);

      // Assert
      expect(dto.thumbnailUrl, isNull);
    });

    test('toEntity should preserve isPending field', () {
      // Arrange
      final dto = LogPostSummaryDto(
        publicId: 'test-id',
        title: 'Test Log',
        outcome: 'PARTIAL',
        thumbnailUrl: 'https://example.com/img.jpg',
        userName: 'user123',
        isPending: true,
      );

      // Act
      final entity = dto.toEntity();

      // Assert
      expect(entity.id, 'test-id');
      expect(entity.title, 'Test Log');
      expect(entity.outcome, 'PARTIAL');
      expect(entity.thumbnailUrl, 'https://example.com/img.jpg');
      expect(entity.userName, 'user123');
      expect(entity.isPending, isTrue);
    });

    test('toEntity should default isPending to false', () {
      // Arrange
      final dto = LogPostSummaryDto(
        publicId: 'test-id',
        title: 'Test',
      );

      // Act
      final entity = dto.toEntity();

      // Assert
      expect(entity.isPending, isFalse);
    });

    test('fromJson should not include isPending field', () {
      // Arrange
      final json = {
        'publicId': 'json-id',
        'title': 'From JSON',
        'outcome': 'FAILED',
        'thumbnailUrl': null,
        'userName': 'jsonuser',
      };

      // Act
      final dto = LogPostSummaryDto.fromJson(json);

      // Assert
      expect(dto.publicId, 'json-id');
      expect(dto.title, 'From JSON');
      expect(dto.isPending, isFalse); // Default value since not in JSON
    });

    test('toJson should not include isPending field', () {
      // Arrange
      final dto = LogPostSummaryDto(
        publicId: 'test-id',
        title: 'Test',
        isPending: true,
      );

      // Act
      final json = dto.toJson();

      // Assert
      expect(json.containsKey('isPending'), isFalse);
      expect(json['publicId'], 'test-id');
      expect(json['title'], 'Test');
    });

    group('foodName and hashtags fields', () {
      test('should create DTO with foodName and hashtags', () {
        // Act
        final dto = LogPostSummaryDto(
          publicId: 'test-id',
          title: 'Test Log',
          outcome: 'SUCCESS',
          foodName: '김치찌개',
          hashtags: ['매운맛', '겨울음식', '한식'],
        );

        // Assert
        expect(dto.foodName, '김치찌개');
        expect(dto.hashtags, ['매운맛', '겨울음식', '한식']);
      });

      test('should handle null foodName and hashtags', () {
        // Act
        final dto = LogPostSummaryDto(
          publicId: 'test-id',
          title: 'Test',
        );

        // Assert
        expect(dto.foodName, isNull);
        expect(dto.hashtags, isNull);
      });

      test('should handle empty hashtags list', () {
        // Act
        final dto = LogPostSummaryDto(
          publicId: 'test-id',
          title: 'Test',
          hashtags: [],
        );

        // Assert
        expect(dto.hashtags, isEmpty);
      });

      test('fromJson should parse foodName and hashtags', () {
        // Arrange
        final json = {
          'publicId': 'json-id',
          'title': 'Test',
          'foodName': 'Kimchi Stew',
          'hashtags': ['spicy', 'korean'],
        };

        // Act
        final dto = LogPostSummaryDto.fromJson(json);

        // Assert
        expect(dto.foodName, 'Kimchi Stew');
        expect(dto.hashtags, ['spicy', 'korean']);
      });

      test('toJson should include foodName and hashtags', () {
        // Arrange
        final dto = LogPostSummaryDto(
          publicId: 'test-id',
          title: 'Test',
          foodName: 'Bibimbap',
          hashtags: ['rice', 'healthy'],
        );

        // Act
        final json = dto.toJson();

        // Assert
        expect(json['foodName'], 'Bibimbap');
        expect(json['hashtags'], ['rice', 'healthy']);
      });

      test('toEntity should preserve foodName and hashtags', () {
        // Arrange
        final dto = LogPostSummaryDto(
          publicId: 'test-id',
          title: 'Test',
          foodName: 'Bulgogi',
          hashtags: ['beef', 'grilled'],
        );

        // Act
        final entity = dto.toEntity();

        // Assert
        expect(entity.foodName, 'Bulgogi');
        expect(entity.hashtags, ['beef', 'grilled']);
      });

      test('fromSyncQueueItem should include hashtags from payload', () {
        // Arrange
        final syncItem = SyncQueueItem(
          id: 'sync-123',
          type: SyncOperationType.createLogPost,
          payload:
              '{"title":"Quick Log","content":"Test","outcome":"SUCCESS","localPhotoPaths":[],"hashtags":["homemade","delicious"]}',
          status: SyncStatus.pending,
          createdAt: DateTime.now(),
          retryCount: 0,
        );

        // Act
        final dto = LogPostSummaryDto.fromSyncQueueItem(syncItem);

        // Assert
        expect(dto.hashtags, ['homemade', 'delicious']);
        expect(dto.foodName, isNull); // Not available from sync queue
      });
    });
  });
}
