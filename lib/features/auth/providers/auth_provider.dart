import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:pairing_planet2_frontend/core/network/dio_provider.dart';
import 'package:pairing_planet2_frontend/core/services/storage_service.dart';
import 'package:pairing_planet2_frontend/data/repositories/auth_repository_impl.dart';
import 'package:pairing_planet2_frontend/domain/repositories/auth_repository.dart';

enum AuthStatus { authenticated, unauthenticated, initial }

class AuthState {
  final AuthStatus status;
  final String? errorMessage;

  AuthState({required this.status, this.errorMessage});
}

class AuthNotifier extends StateNotifier<AuthState> {
  final StorageService _storage;

  AuthNotifier(this._storage) : super(AuthState(status: AuthStatus.initial)) {
    checkAuthStatus();
  }

  void loginSuccess() {
    if (!mounted) return;

    state = AuthState(status: AuthStatus.authenticated);
  }

  Future<void> checkAuthStatus() async {
    final token = await _storage.getAccessToken();
    if (token != null) {
      state = AuthState(status: AuthStatus.authenticated);
    } else {
      state = AuthState(status: AuthStatus.unauthenticated);
    }
  }

  void logout() async {
    await _storage.clearTokens();
    state = AuthState(status: AuthStatus.unauthenticated);
  }
}

// 💡 전역 프로바이더 등록
final authStateProvider = StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  return AuthNotifier(StorageService());
});

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  // dioProvider를 감시하여 Dio 인스턴스를 가져옵니다.
  final dio = ref.watch(dioProvider);
  // storageServiceProvider를 통해 저장소를 가져옵니다.
  final storage = ref.watch(storageServiceProvider);

  return AuthRepositoryImpl(dio, storage, ref);
});
