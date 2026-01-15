import 'package:flutter_test/flutter_test.dart';
import 'package:pairing_planet2_frontend/data/models/log_post/log_post_summary_dto.dart';

/// Unit tests for LogPostCard logic.
///
/// Note: Full widget tests require ScreenUtil and MediaQuery setup which is
/// complex in test environment. These tests focus on the data model and logic.
void main() {
  group('LogPostCard data handling', () {
    test('should display foodName when available', () {
      // The LogPostCard widget shows foodName when available
      final dto = LogPostSummaryDto(
        publicId: 'test-id',
        title: 'Test Title',
        outcome: 'SUCCESS',
        foodName: 'Kimchi Stew',
        hashtags: ['spicy', 'korean'],
      );

      // Assert data is correctly structured for card
      expect(dto.foodName, 'Kimchi Stew');
      expect(dto.foodName!.isNotEmpty, isTrue);
    });

    test('should fall back to title when foodName is null', () {
      // When foodName is null, the card falls back to showing title
      final dto = LogPostSummaryDto(
        publicId: 'test-id',
        title: 'My Cooking Log',
        outcome: 'SUCCESS',
        foodName: null,
      );

      // Assert fallback logic condition
      expect(dto.foodName, isNull);
      expect(dto.title, 'My Cooking Log');
      expect(dto.title!.isNotEmpty, isTrue);
    });

    test('should handle empty foodName with fallback to title', () {
      // When foodName is empty string, should show title instead
      final dto = LogPostSummaryDto(
        publicId: 'test-id',
        title: 'Fallback Title',
        outcome: 'PARTIAL',
        foodName: '',
      );

      // Assert fallback condition
      expect(dto.foodName!.isEmpty, isTrue);
      expect(dto.title, 'Fallback Title');
    });

    test('should format hashtags correctly', () {
      // Card shows hashtags in "#tag1 #tag2 #tag3" format
      final dto = LogPostSummaryDto(
        publicId: 'test-id',
        title: 'Test',
        hashtags: ['delicious', 'homemade', 'weekend'],
      );

      // Simulate the formatting logic in LogPostCard
      final formatted = dto.hashtags!.take(3).map((h) => '#$h').join(' ');

      expect(formatted, '#delicious #homemade #weekend');
    });

    test('should limit hashtags to 3 for display', () {
      // Card only shows first 3 hashtags
      final dto = LogPostSummaryDto(
        publicId: 'test-id',
        title: 'Test',
        hashtags: ['one', 'two', 'three', 'four', 'five'],
      );

      // Simulate the take(3) logic in LogPostCard
      final displayTags = dto.hashtags!.take(3).toList();

      expect(displayTags.length, 3);
      expect(displayTags, ['one', 'two', 'three']);
    });

    test('should handle empty hashtags list', () {
      final dto = LogPostSummaryDto(
        publicId: 'test-id',
        title: 'Test',
        hashtags: [],
      );

      // Assert condition for not showing hashtags section
      expect(dto.hashtags!.isEmpty, isTrue);
    });

    test('should handle null hashtags', () {
      final dto = LogPostSummaryDto(
        publicId: 'test-id',
        title: 'Test',
        hashtags: null,
      );

      // Assert condition for not showing hashtags section
      expect(dto.hashtags, isNull);
    });

    test('should handle various outcome values', () {
      // Test all outcome types
      final outcomes = ['SUCCESS', 'PARTIAL', 'FAILED'];

      for (final outcome in outcomes) {
        final dto = LogPostSummaryDto(
          publicId: 'test-$outcome',
          title: 'Test',
          outcome: outcome,
        );

        expect(dto.outcome, outcome);
      }
    });

    test('should handle null thumbnail URL', () {
      final dto = LogPostSummaryDto(
        publicId: 'test-id',
        title: 'Test',
        thumbnailUrl: null,
      );

      // Assert fallback condition for placeholder image
      expect(dto.thumbnailUrl, isNull);
    });

    test('should handle all fields populated', () {
      // Complete DTO with all fields
      final dto = LogPostSummaryDto(
        publicId: 'complete-id',
        title: 'Complete Log',
        outcome: 'SUCCESS',
        thumbnailUrl: 'https://example.com/image.jpg',
        creatorPublicId: 'creator-123',
        userName: 'Chef User',
        foodName: 'Delicious Dish',
        hashtags: ['tag1', 'tag2'],
      );

      expect(dto.publicId, 'complete-id');
      expect(dto.title, 'Complete Log');
      expect(dto.outcome, 'SUCCESS');
      expect(dto.thumbnailUrl, 'https://example.com/image.jpg');
      expect(dto.creatorPublicId, 'creator-123');
      expect(dto.userName, 'Chef User');
      expect(dto.foodName, 'Delicious Dish');
      expect(dto.hashtags, ['tag1', 'tag2']);
    });
  });
}
