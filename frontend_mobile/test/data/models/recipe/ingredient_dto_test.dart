import 'package:flutter_test/flutter_test.dart';
import 'package:pairing_planet2_frontend/data/models/recipe/ingredient_dto.dart';
import 'package:pairing_planet2_frontend/domain/entities/recipe/ingredient.dart';

void main() {
  group('IngredientDto', () {
    group('toEntity', () {
      test('converts MAIN type to uppercase string', () {
        // Arrange
        final dto = IngredientDto(
          name: 'Flour',
          type: IngredientType.main,
        );

        // Act
        final entity = dto.toEntity();

        // Assert
        expect(entity.type, 'MAIN');
      });

      test('converts SECONDARY type to uppercase string', () {
        // Arrange
        final dto = IngredientDto(
          name: 'Butter',
          type: IngredientType.secondary,
        );

        // Act
        final entity = dto.toEntity();

        // Assert
        expect(entity.type, 'SECONDARY');
      });

      test('converts SEASONING type to uppercase string', () {
        // Arrange
        final dto = IngredientDto(
          name: 'Salt',
          type: IngredientType.seasoning,
        );

        // Act
        final entity = dto.toEntity();

        // Assert
        expect(entity.type, 'SEASONING');
      });

      test('preserves all fields during conversion', () {
        // Arrange
        final dto = IngredientDto(
          name: 'Olive Oil',
          quantity: 2.0,
          unit: MeasurementUnit.tbsp,
          type: IngredientType.seasoning,
        );

        // Act
        final entity = dto.toEntity();

        // Assert
        expect(entity.name, 'Olive Oil');
        expect(entity.quantity, 2.0);
        expect(entity.unit, 'tbsp');
        expect(entity.type, 'SEASONING');
      });
    });

    group('fromEntity', () {
      test('matches uppercase type string to enum', () {
        // Arrange
        final entity = Ingredient(
          name: 'Flour',
          type: 'MAIN',
        );

        // Act
        final dto = IngredientDto.fromEntity(entity);

        // Assert
        expect(dto.type, IngredientType.main);
      });

      test('matches lowercase type string to enum (case-insensitive)', () {
        // Arrange
        final entity = Ingredient(
          name: 'Butter',
          type: 'secondary',
        );

        // Act
        final dto = IngredientDto.fromEntity(entity);

        // Assert
        expect(dto.type, IngredientType.secondary);
      });

      test('matches mixed case type string to enum', () {
        // Arrange
        final entity = Ingredient(
          name: 'Salt',
          type: 'Seasoning',
        );

        // Act
        final dto = IngredientDto.fromEntity(entity);

        // Assert
        expect(dto.type, IngredientType.seasoning);
      });

      test('defaults to main for invalid type', () {
        // Arrange
        final entity = Ingredient(
          name: 'Test',
          type: 'INVALID',
        );

        // Act
        final dto = IngredientDto.fromEntity(entity);

        // Assert
        expect(dto.type, IngredientType.main);
      });

      test('preserves all fields during conversion', () {
        // Arrange
        final entity = Ingredient(
          name: 'Chicken',
          quantity: 500.0,
          unit: 'g',
          type: 'MAIN',
        );

        // Act
        final dto = IngredientDto.fromEntity(entity);

        // Assert
        expect(dto.name, 'Chicken');
        expect(dto.quantity, 500.0);
        expect(dto.unit, MeasurementUnit.g);
        expect(dto.type, IngredientType.main);
      });
    });

    group('round-trip conversion', () {
      test('preserves MAIN type through entity conversion', () {
        // Arrange
        final original = IngredientDto(
          name: 'Chicken',
          type: IngredientType.main,
        );

        // Act
        final entity = original.toEntity();
        final restored = IngredientDto.fromEntity(entity);

        // Assert
        expect(restored.type, IngredientType.main);
        expect(restored.name, original.name);
      });

      test('preserves SECONDARY type through entity conversion', () {
        // Arrange
        final original = IngredientDto(
          name: 'Onion',
          type: IngredientType.secondary,
        );

        // Act
        final entity = original.toEntity();
        final restored = IngredientDto.fromEntity(entity);

        // Assert
        expect(restored.type, IngredientType.secondary);
        expect(restored.name, original.name);
      });

      test('preserves SEASONING type through entity conversion', () {
        // Arrange
        final original = IngredientDto(
          name: 'Pepper',
          type: IngredientType.seasoning,
        );

        // Act
        final entity = original.toEntity();
        final restored = IngredientDto.fromEntity(entity);

        // Assert
        expect(restored.type, IngredientType.seasoning);
        expect(restored.name, original.name);
      });

      test('preserves all fields through round-trip conversion', () {
        // Arrange
        final original = IngredientDto(
          name: 'Sugar',
          quantity: 1.0,
          unit: MeasurementUnit.cup,
          type: IngredientType.secondary,
        );

        // Act
        final entity = original.toEntity();
        final restored = IngredientDto.fromEntity(entity);

        // Assert
        expect(restored.name, original.name);
        expect(restored.quantity, original.quantity);
        expect(restored.unit, original.unit);
        expect(restored.type, original.type);
      });
    });

  });
}
