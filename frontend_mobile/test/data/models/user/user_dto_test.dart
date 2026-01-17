import 'package:flutter_test/flutter_test.dart';
import 'package:pairing_planet2_frontend/data/models/user/user_dto.dart';

void main() {
  group('UserDto', () {
    group('fromJson', () {
      test('should parse defaultCookingStyle from JSON', () {
        // Arrange
        final json = {
          'id': 'user-123',
          'username': 'testuser',
          'defaultCookingStyle': 'KR',
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
        expect(dto.defaultCookingStyle, 'KR');
      });

      test('should handle null defaultCookingStyle', () {
        // Arrange
        final json = {
          'id': 'user-123',
          'username': 'testuser',
          'defaultCookingStyle': null,
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
        expect(dto.defaultCookingStyle, isNull);
      });

      test('should handle missing defaultCookingStyle field', () {
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
        expect(dto.defaultCookingStyle, isNull);
      });

      test('should parse "other" as defaultCookingStyle', () {
        // Arrange
        final json = {
          'id': 'user-123',
          'username': 'testuser',
          'defaultCookingStyle': 'other',
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
        expect(dto.defaultCookingStyle, 'other');
      });

      test('should parse various ISO country codes', () {
        final countryCodes = ['US', 'JP', 'CN', 'IT', 'FR', 'TH', 'IN', 'MX'];

        for (final code in countryCodes) {
          final json = {
            'id': 'user-123',
            'username': 'testuser',
            'defaultCookingStyle': code,
            'followerCount': 0,
            'followingCount': 0,
            'recipeCount': 0,
            'logCount': 0,
            'level': 1,
            'levelName': 'beginner',
          };

          final dto = UserDto.fromJson(json);

          expect(dto.defaultCookingStyle, code);
        }
      });
    });

    group('toJson', () {
      test('should serialize defaultCookingStyle to JSON', () {
        // Arrange
        final dto = UserDto(
          id: 'user-123',
          username: 'testuser',
          defaultCookingStyle: 'KR',
        );

        // Act
        final json = dto.toJson();

        // Assert
        expect(json['defaultCookingStyle'], 'KR');
      });

      test('should serialize null defaultCookingStyle', () {
        // Arrange
        final dto = UserDto(
          id: 'user-123',
          username: 'testuser',
          defaultCookingStyle: null,
        );

        // Act
        final json = dto.toJson();

        // Assert
        expect(json['defaultCookingStyle'], isNull);
      });
    });

    group('round-trip conversion', () {
      test('should preserve defaultCookingStyle through round-trip', () {
        // Arrange
        final original = UserDto(
          id: 'user-123',
          username: 'testuser',
          defaultCookingStyle: 'JP',
        );

        // Act
        final json = original.toJson();
        final restored = UserDto.fromJson(json);

        // Assert
        expect(restored.defaultCookingStyle, original.defaultCookingStyle);
      });
    });
  });
}
