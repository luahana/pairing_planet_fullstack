import 'package:dartz/dartz.dart';
import 'package:dio/dio.dart';
import 'package:pairing_planet2_frontend/core/constants/constants.dart';
import 'package:pairing_planet2_frontend/core/error/failures.dart';
import 'package:pairing_planet2_frontend/data/models/recipe/create_recipe_request_dtos.dart';
import 'package:pairing_planet2_frontend/domain/entities/common/slice_response.dart';
import 'package:pairing_planet2_frontend/domain/entities/recipe/create_recipe_request.dart';
import 'package:pairing_planet2_frontend/domain/entities/recipe/recipe_detail.dart';
import 'package:pairing_planet2_frontend/domain/entities/recipe/recipe_summary.dart';
import '../../core/network/network_info.dart'; // 네트워크 상태 확인용 (추가 필요)
import '../datasources/recipe/recipe_local_data_source.dart'; // 로컬 데이터 소스 (추가 필요)
import '../../domain/repositories/recipe_repository.dart';
import '../datasources/recipe/recipe_remote_data_source.dart';

class RecipeRepositoryImpl implements RecipeRepository {
  final RecipeRemoteDataSource remoteDataSource;
  final RecipeLocalDataSource localDataSource; // 추가
  final NetworkInfo networkInfo; // 추가

  RecipeRepositoryImpl({
    required this.remoteDataSource,
    required this.localDataSource,
    required this.networkInfo,
  });

  @override
  Future<Either<Failure, String>> createRecipe(
    CreateRecipeRequest recipe,
  ) async {
    // 1. 네트워크 연결 상태 확인
    if (await networkInfo.isConnected) {
      try {
        // 2. Domain entity를 DTO로 변환
        final dto = CreateRecipeRequestDto.fromEntity(recipe);

        // 3. 서버에 레시피 생성 요청
        final newPublicId = await remoteDataSource.createRecipe(dto);

        // 💡 Unit 대신 받은 ID를 반환하여 스크린에서 사용할 수 있게 함
        return Right(newPublicId);
      } on DioException catch (e) {
        // 4. 에러 발생 시 기존 헬퍼 메서드로 Failure 매핑
        return Left(_mapDioExceptionToFailure(e));
      } catch (e) {
        return Left(UnknownFailure(e.toString()));
      }
    } else {
      // 5. 오프라인 상태 에러 반환
      return Left(ConnectionFailure('네트워크 연결이 없어 레시피를 등록할 수 없습니다.'));
    }
  }

  @override
  Future<Either<Failure, RecipeDetail>> getRecipeDetail(String id) async {
    // 1. 로컬 캐시 데이터 먼저 확인
    final localData = await localDataSource.getLastRecipeDetail(id);

    // 2. 네트워크 연결 상태 확인
    if (await networkInfo.isConnected) {
      try {
        // 3. 온라인인 경우 서버에서 최신 데이터 요청
        final remoteDto = await remoteDataSource.getRecipeDetail(id);

        // 4. 서버 데이터 성공 시 로컬 캐시에 저장/갱신
        await localDataSource.cacheRecipeDetail(remoteDto);

        return Right(remoteDto.toEntity());
      } on DioException catch (e) {
        // 서버 에러가 났지만 로컬 데이터가 있다면 사용자에게 이전 데이터를 보여줌 (Fallback)
        if (localData != null) return Right(localData.toEntity());
        return Left(_mapDioExceptionToFailure(e));
      } catch (e) {
        if (localData != null) return Right(localData.toEntity());
        return Left(UnknownFailure(e.toString()));
      }
    } else {
      // 5. 오프라인인 경우 로컬 캐시 데이터 반환
      if (localData != null) {
        return Right(localData.toEntity());
      } else {
        // 캐시도 없고 오프라인인 경우 에러 반환
        return Left(ConnectionFailure('오프라인 상태이며 저장된 데이터가 없습니다.'));
      }
    }
  }

  @override
  Future<Either<Failure, SliceResponse<RecipeSummary>>> getRecipes({
    required int page,
    int size = 10,
    String? query,
    String? cuisineFilter,
    String? typeFilter,
    String? sortBy,
  }) async {
    // Check if any filters are active
    final hasFilters = (query != null && query.isNotEmpty) ||
        cuisineFilter != null ||
        typeFilter != null ||
        (sortBy != null && sortBy != 'recent');

    // Filter/search mode: always fetch from network, no caching
    if (hasFilters) {
      try {
        final sliceDto = await remoteDataSource.getRecipes(
          page: page,
          size: size,
          query: query,
          cuisineFilter: cuisineFilter,
          typeFilter: typeFilter,
          sortBy: sortBy,
        );
        return Right(sliceDto.toEntity((dto) => dto.toEntity()));
      } on DioException catch (e) {
        return Left(_mapDioExceptionToFailure(e));
      } catch (e) {
        return Left(UnknownFailure(e.toString()));
      }
    }

    // Only cache first page (page 0) when not searching/filtering
    if (page == 0) {
      return _getRecipesWithCache(size: size);
    }

    // Pages > 0: network-only (no caching for pagination)
    try {
      final sliceDto = await remoteDataSource.getRecipes(page: page, size: size);
      return Right(sliceDto.toEntity((dto) => dto.toEntity()));
    } on DioException catch (e) {
      return Left(_mapDioExceptionToFailure(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  /// Cache-first strategy for the first page of recipes.
  Future<Either<Failure, SliceResponse<RecipeSummary>>> _getRecipesWithCache({
    int size = 10,
  }) async {
    // 1. Check for cached data
    final cached = await localDataSource.getCachedRecipeList();

    // 2. Check network connectivity
    if (await networkInfo.isConnected) {
      try {
        // 3. Fetch fresh data from server
        final sliceDto = await remoteDataSource.getRecipes(page: 0, size: size);

        // 4. Cache the first page on success
        await localDataSource.cacheRecipeList(sliceDto.content);

        // 5. Return fresh data
        return Right(sliceDto.toEntity((dto) => dto.toEntity()));
      } on DioException catch (e) {
        // Network error: fallback to cache if available
        if (cached != null) {
          return Right(SliceResponse(
            content: cached.data.map((d) => d.toEntity()).toList(),
            number: 0,
            size: cached.data.length,
            first: true,
            last: false,
            hasNext: true, // Assume there's more (can't verify offline)
            isFromCache: true,
            cachedAt: cached.cachedAt,
          ));
        }
        return Left(_mapDioExceptionToFailure(e));
      } catch (e) {
        if (cached != null) {
          return Right(SliceResponse(
            content: cached.data.map((d) => d.toEntity()).toList(),
            number: 0,
            size: cached.data.length,
            first: true,
            last: false,
            hasNext: true,
            isFromCache: true,
            cachedAt: cached.cachedAt,
          ));
        }
        return Left(UnknownFailure(e.toString()));
      }
    } else {
      // Offline: use cached data
      if (cached != null) {
        return Right(SliceResponse(
          content: cached.data.map((d) => d.toEntity()).toList(),
          number: 0,
          size: cached.data.length,
          first: true,
          last: false,
          hasNext: false, // Can't paginate offline
          isFromCache: true,
          cachedAt: cached.cachedAt,
        ));
      }
      return Left(ConnectionFailure('오프라인 상태이며 저장된 데이터가 없습니다.'));
    }
  }

  // P1: 레시피 저장 (북마크)
  @override
  Future<Either<Failure, void>> saveRecipe(String publicId) async {
    if (await networkInfo.isConnected) {
      try {
        await remoteDataSource.saveRecipe(publicId);
        return const Right(null);
      } on DioException catch (e) {
        return Left(_mapDioExceptionToFailure(e));
      } catch (e) {
        return Left(UnknownFailure(e.toString()));
      }
    } else {
      return Left(ConnectionFailure('네트워크 연결이 없습니다.'));
    }
  }

  // P1: 레시피 저장 취소
  @override
  Future<Either<Failure, void>> unsaveRecipe(String publicId) async {
    if (await networkInfo.isConnected) {
      try {
        await remoteDataSource.unsaveRecipe(publicId);
        return const Right(null);
      } on DioException catch (e) {
        return Left(_mapDioExceptionToFailure(e));
      } catch (e) {
        return Left(UnknownFailure(e.toString()));
      }
    } else {
      return Left(ConnectionFailure('네트워크 연결이 없습니다.'));
    }
  }

  // 레시피 수정
  @override
  Future<Either<Failure, RecipeDetail>> updateRecipe(
    String publicId,
    Map<String, dynamic> data,
  ) async {
    if (await networkInfo.isConnected) {
      try {
        final dto = await remoteDataSource.updateRecipe(publicId, data);
        // 캐시 갱신
        await localDataSource.cacheRecipeDetail(dto);
        return Right(dto.toEntity());
      } on DioException catch (e) {
        return Left(_mapDioExceptionToFailure(e));
      } catch (e) {
        return Left(UnknownFailure(e.toString()));
      }
    } else {
      return Left(ConnectionFailure('네트워크 연결이 없습니다.'));
    }
  }

  // 레시피 삭제
  @override
  Future<Either<Failure, void>> deleteRecipe(String publicId) async {
    if (await networkInfo.isConnected) {
      try {
        await remoteDataSource.deleteRecipe(publicId);
        // 캐시에서 삭제
        await localDataSource.deleteRecipeDetailCache(publicId);
        return const Right(null);
      } on DioException catch (e) {
        return Left(_mapDioExceptionToFailure(e));
      } catch (e) {
        return Left(UnknownFailure(e.toString()));
      }
    } else {
      return Left(ConnectionFailure('네트워크 연결이 없습니다.'));
    }
  }

  Failure _mapDioExceptionToFailure(DioException e) {
    switch (e.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.receiveTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.connectionError:
        return ConnectionFailure();

      case DioExceptionType.badResponse:
        final statusCode = e.response?.statusCode;
        if (statusCode == HttpStatus.notFound) return NotFoundFailure();
        if (statusCode == HttpStatus.unauthorized) return UnauthorizedFailure();
        if (statusCode == HttpStatus.serverError) return ServerFailure();
        return ServerFailure('에러 코드: $statusCode');

      default:
        return UnknownFailure();
    }
  }
}
