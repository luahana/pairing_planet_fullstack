import 'package:dartz/dartz.dart';
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:pairing_planet2_frontend/core/error/failures.dart';
import 'package:pairing_planet2_frontend/core/providers/locale_provider.dart';
import 'package:pairing_planet2_frontend/core/services/storage_service.dart';
import 'package:pairing_planet2_frontend/domain/repositories/auth_repository.dart';

class AuthRepositoryImpl implements AuthRepository {
  final Dio _dio;
  final StorageService _storage;
  final Ref _ref;

  AuthRepositoryImpl(this._dio, this._storage, this._ref);

  @override
  Future<Either<Failure, Unit>> socialLogin(String firebaseIdToken) async {
    try {
      final String currentLocale = _ref.read(localeProvider);

      final response = await _dio.post(
        '/auth/social-login', // 백엔드 엔드포인트
        data: {'idToken': firebaseIdToken, 'locale': currentLocale},
      );

      if (response.statusCode == 200) {
        // 성공 시 토큰 저장
        await _storage.saveAccessToken(response.data['accessToken']);
        await _storage.saveRefreshToken(response.data['refreshToken']);
        return const Right(unit);
      }
      return Left(ServerFailure());
    } catch (e) {
      return Left(ServerFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, Unit>> reissueToken() async {
    // ... 토큰 재발급 로직 구현
    return const Right(unit);
  }
}
