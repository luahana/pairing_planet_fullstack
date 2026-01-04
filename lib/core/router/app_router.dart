import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:pairing_planet2_frontend/features/home/screens/main_screen.dart';
import 'package:pairing_planet2_frontend/features/login/screens/login_screen.dart';
import 'package:pairing_planet2_frontend/features/recipe/presentation/screens/recipe_create_screen.dart';
import '../../features/auth/providers/auth_provider.dart';
import '../../features/recipe/presentation/screens/recipe_detail_screen.dart';

/// 💡 Riverpod의 상태 변화를 GoRouter에 전달하기 위한 클래스
/// ChangeNotifier를 상속받아 authStateProvider가 바뀔 때마다 notifyListeners를 호출합니다.
class RouterNotifier extends ChangeNotifier {
  final Ref _ref;

  RouterNotifier(this._ref) {
    // authStateProvider를 감시(listen)하며 상태가 변할 때마다
    // GoRouter에게 리다이렉트 로직을 다시 실행하라고 알립니다.
    _ref.listen(authStateProvider, (previous, next) {
      if (previous?.status != next.status) {
        notifyListeners();
      }
    });
  }
}

/// RouterNotifier를 제공하는 Provider
final routerNotifierProvider = Provider((ref) => RouterNotifier(ref));

final routerProvider = Provider<GoRouter>((ref) {
  // 💡 중요: watch 대신 read를 사용하여 GoRouter 객체가 재생성되는 것을 방지합니다.
  final routerNotifier = ref.read(routerNotifierProvider);

  return GoRouter(
    initialLocation: '/',
    // 💡 Auth 상태 변화에 따라 리다이렉트를 트리거하는 핵심 설정
    refreshListenable: routerNotifier,

    observers: [
      FirebaseAnalyticsObserver(analytics: FirebaseAnalytics.instance),
    ],

    redirect: (context, state) {
      // 💡 redirect 내부에서는 최신 상태를 read로 가져옵니다.
      final authState = ref.read(authStateProvider);

      // 1. 초기 토큰 체크 중일 때는 리다이렉트를 수행하지 않음
      if (authState.status == AuthStatus.initial) return null;

      final isLoggedIn = authState.status == AuthStatus.authenticated;
      final isLoggingIn = state.matchedLocation == '/login';

      // 2. 로그인 안된 상태에서 보호된 페이지 접근 시 로그인 화면으로
      if (!isLoggedIn && !isLoggingIn) return '/login';

      // 3. 로그인 된 상태에서 로그인 화면 접근 시 홈 화면으로
      if (isLoggedIn && isLoggingIn) return '/';

      return null;
    },
    routes: [
      GoRoute(
        path: '/login',
        name: 'login',
        builder: (context, state) => const LoginScreen(),
      ),
      GoRoute(
        path: '/recipe/create',
        name: 'recipe_create',
        builder: (context, state) => const RecipeCreateScreen(),
      ),
      StatefulShellRoute.indexedStack(
        builder: (context, state, navigationShell) {
          return MainScreen(navigationShell: navigationShell);
        },
        branches: [
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/',
                builder: (context, state) => const Center(child: Text('홈 화면')),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/recipe',
                builder: (context, state) =>
                    const Center(child: Text('레시피 화면')),
                routes: [
                  GoRoute(
                    path: 'detail/:id',
                    name: 'recipe_detail',
                    builder: (context, state) {
                      final id = state.pathParameters['id']!;
                      return RecipeDetailScreen(recipeId: id);
                    },
                  ),
                ],
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/search',
                builder: (context, state) => const Center(child: Text('검색 화면')),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/profile',
                builder: (context, state) => const Center(child: Text('마이페이지')),
              ),
            ],
          ),
        ],
      ),
    ],
  );
});
