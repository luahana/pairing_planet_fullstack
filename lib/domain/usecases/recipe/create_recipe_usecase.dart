import 'package:dartz/dartz.dart';
import '../../../core/error/failures.dart';
import '../../repositories/recipe_repository.dart';
import '../../../data/models/recipe/create_recipe_request_dtos.dart'; // DTO 경로 확인

class CreateRecipeUseCase {
  final RecipeRepository _repository;

  CreateRecipeUseCase(this._repository);

  /// 레시피 생성을 실행합니다.
  Future<Either<Failure, void>> execute(CreateRecipeRequestDto request) async {
    return await _repository.createRecipe(request);
  }
}
