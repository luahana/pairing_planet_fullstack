import 'package:flutter_test/flutter_test.dart';
import 'package:pairing_planet2_frontend/data/models/user/update_profile_request_dto.dart';

void main() {
  group('UpdateProfileRequestDto', () {
    group('toJson', () {
      test('should serialize defaultCookingStyle to JSON', () {
        // Arrange
        final dto = UpdateProfileRequestDto(
          defaultCookingStyle: 'KR',
        );

        // Act
        final json = dto.toJson();

        // Assert
        expect(json['defaultCookingStyle'], 'KR');
      });

      test('should handle null defaultCookingStyle', () {
        // Arrange
        final dto = UpdateProfileRequestDto(
          defaultCookingStyle: null,
        );

        // Act
        final json = dto.toJson();

        // Assert
        expect(json['defaultCookingStyle'], isNull);
      });

      test('should serialize "other" as defaultCookingStyle', () {
        // Arrange
        final dto = UpdateProfileRequestDto(
          defaultCookingStyle: 'other',
        );

        // Act
        final json = dto.toJson();

        // Assert
        expect(json['defaultCookingStyle'], 'other');
      });

      test('should serialize various ISO country codes', () {
        final countryCodes = ['US', 'JP', 'CN', 'IT', 'FR', 'TH', 'IN', 'MX'];

        for (final code in countryCodes) {
          final dto = UpdateProfileRequestDto(
            defaultCookingStyle: code,
          );

          final json = dto.toJson();

          expect(json['defaultCookingStyle'], code);
        }
      });

      test('should include other fields alongside defaultCookingStyle', () {
        // Arrange
        final dto = UpdateProfileRequestDto(
          username: 'newuser',
          locale: 'ko-KR',
          defaultCookingStyle: 'KR',
        );

        // Act
        final json = dto.toJson();

        // Assert
        expect(json['username'], 'newuser');
        expect(json['locale'], 'ko-KR');
        expect(json['defaultCookingStyle'], 'KR');
      });
    });

    group('fromJson', () {
      test('should parse defaultCookingStyle from JSON', () {
        // Arrange
        final json = {
          'defaultCookingStyle': 'JP',
        };

        // Act
        final dto = UpdateProfileRequestDto.fromJson(json);

        // Assert
        expect(dto.defaultCookingStyle, 'JP');
      });
    });
  });
}
