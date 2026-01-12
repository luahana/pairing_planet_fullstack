import 'package:flutter_test/flutter_test.dart';
import 'package:pairing_planet2_frontend/data/models/user/update_profile_request_dto.dart';

void main() {
  group('UpdateProfileRequestDto', () {
    group('toJson', () {
      test('should serialize defaultFoodStyle to JSON', () {
        // Arrange
        final dto = UpdateProfileRequestDto(
          defaultFoodStyle: 'KR',
        );

        // Act
        final json = dto.toJson();

        // Assert
        expect(json['defaultFoodStyle'], 'KR');
      });

      test('should handle null defaultFoodStyle', () {
        // Arrange
        final dto = UpdateProfileRequestDto(
          defaultFoodStyle: null,
        );

        // Act
        final json = dto.toJson();

        // Assert
        expect(json['defaultFoodStyle'], isNull);
      });

      test('should serialize "other" as defaultFoodStyle', () {
        // Arrange
        final dto = UpdateProfileRequestDto(
          defaultFoodStyle: 'other',
        );

        // Act
        final json = dto.toJson();

        // Assert
        expect(json['defaultFoodStyle'], 'other');
      });

      test('should serialize various ISO country codes', () {
        final countryCodes = ['US', 'JP', 'CN', 'IT', 'FR', 'TH', 'IN', 'MX'];

        for (final code in countryCodes) {
          final dto = UpdateProfileRequestDto(
            defaultFoodStyle: code,
          );

          final json = dto.toJson();

          expect(json['defaultFoodStyle'], code);
        }
      });

      test('should include other fields alongside defaultFoodStyle', () {
        // Arrange
        final dto = UpdateProfileRequestDto(
          username: 'newuser',
          locale: 'ko-KR',
          defaultFoodStyle: 'KR',
        );

        // Act
        final json = dto.toJson();

        // Assert
        expect(json['username'], 'newuser');
        expect(json['locale'], 'ko-KR');
        expect(json['defaultFoodStyle'], 'KR');
      });
    });

    group('fromJson', () {
      test('should parse defaultFoodStyle from JSON', () {
        // Arrange
        final json = {
          'defaultFoodStyle': 'JP',
        };

        // Act
        final dto = UpdateProfileRequestDto.fromJson(json);

        // Assert
        expect(dto.defaultFoodStyle, 'JP');
      });
    });
  });
}
