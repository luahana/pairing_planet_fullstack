import 'package:flutter_test/flutter_test.dart';
import 'package:pairing_planet2_frontend/data/models/user/user_dto.dart';

void main() {
  group('UserDto', () {
    group('fromJson', () {
      test('should parse defaultFoodStyle from JSON', () {
        // Arrange
        final json = {
          'id': 'user-123',
          'username': 'testuser',
          'defaultFoodStyle': 'KR',
          'followerCount': 0,
          'followingCount': 0,
          'recipeCount': 0,
          'logCount': 0,
          'level': 1,
          'levelName': 'beginner',
        };

        // Act
        final dto = UserDto.fromJson(json);

        // Assert
        expect(dto.defaultFoodStyle, 'KR');
      });

      test('should handle null defaultFoodStyle', () {
        // Arrange
        final json = {
          'id': 'user-123',
          'username': 'testuser',
          'defaultFoodStyle': null,
          'followerCount': 0,
          'followingCount': 0,
          'recipeCount': 0,
          'logCount': 0,
          'level': 1,
          'levelName': 'beginner',
        };

        // Act
        final dto = UserDto.fromJson(json);

        // Assert
        expect(dto.defaultFoodStyle, isNull);
      });

      test('should handle missing defaultFoodStyle field', () {
        // Arrange
        final json = {
          'id': 'user-123',
          'username': 'testuser',
          'followerCount': 0,
          'followingCount': 0,
          'recipeCount': 0,
          'logCount': 0,
          'level': 1,
          'levelName': 'beginner',
        };

        // Act
        final dto = UserDto.fromJson(json);

        // Assert
        expect(dto.defaultFoodStyle, isNull);
      });

      test('should parse "other" as defaultFoodStyle', () {
        // Arrange
        final json = {
          'id': 'user-123',
          'username': 'testuser',
          'defaultFoodStyle': 'other',
          'followerCount': 0,
          'followingCount': 0,
          'recipeCount': 0,
          'logCount': 0,
          'level': 1,
          'levelName': 'beginner',
        };

        // Act
        final dto = UserDto.fromJson(json);

        // Assert
        expect(dto.defaultFoodStyle, 'other');
      });

      test('should parse various ISO country codes', () {
        final countryCodes = ['US', 'JP', 'CN', 'IT', 'FR', 'TH', 'IN', 'MX'];

        for (final code in countryCodes) {
          final json = {
            'id': 'user-123',
            'username': 'testuser',
            'defaultFoodStyle': code,
            'followerCount': 0,
            'followingCount': 0,
            'recipeCount': 0,
            'logCount': 0,
            'level': 1,
            'levelName': 'beginner',
          };

          final dto = UserDto.fromJson(json);

          expect(dto.defaultFoodStyle, code);
        }
      });
    });

    group('toJson', () {
      test('should serialize defaultFoodStyle to JSON', () {
        // Arrange
        final dto = UserDto(
          id: 'user-123',
          username: 'testuser',
          defaultFoodStyle: 'KR',
        );

        // Act
        final json = dto.toJson();

        // Assert
        expect(json['defaultFoodStyle'], 'KR');
      });

      test('should serialize null defaultFoodStyle', () {
        // Arrange
        final dto = UserDto(
          id: 'user-123',
          username: 'testuser',
          defaultFoodStyle: null,
        );

        // Act
        final json = dto.toJson();

        // Assert
        expect(json['defaultFoodStyle'], isNull);
      });
    });

    group('round-trip conversion', () {
      test('should preserve defaultFoodStyle through round-trip', () {
        // Arrange
        final original = UserDto(
          id: 'user-123',
          username: 'testuser',
          defaultFoodStyle: 'JP',
        );

        // Act
        final json = original.toJson();
        final restored = UserDto.fromJson(json);

        // Assert
        expect(restored.defaultFoodStyle, original.defaultFoodStyle);
      });
    });
  });
}
