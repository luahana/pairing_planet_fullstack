import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:pairing_planet2_frontend/features/home/screens/main_screen.dart';
import 'package:pairing_planet2_frontend/features/login/screens/login_screen.dart';
import 'package:pairing_planet2_frontend/features/recipe/presentation/screens/recipe_create_screen.dart';
import '../../features/auth/providers/auth_provider.dart';
import '../../features/recipe/presentation/screens/recipe_detail_screen.dart';

final routerProvider = Provider<GoRouter>((ref) {
  final authState = ref.watch(authStateProvider);

  return GoRouter(
    initialLocation: '/',
    redirect: (context, state) {
      final isLoggedIn = authState.status == AuthStatus.authenticated;
      final isLoggingIn = state.matchedLocation == '/login';
      if (!isLoggedIn && !isLoggingIn) return '/login';
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

      // 💡 하단 네비게이션을 위한 StatefulShellRoute 설정
      StatefulShellRoute.indexedStack(
        builder: (context, state, navigationShell) {
          return MainScreen(navigationShell: navigationShell);
        },
        branches: [
          // 1번 탭: 홈
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/',
                builder: (context, state) => const Center(child: Text('홈 화면')),
              ),
            ],
          ),
          // 2번 탭: 레시피
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/recipe',
                builder: (context, state) =>
                    const Center(child: Text('레시피 목록')),
                routes: [
                  // 기존에 있던 상세 페이지를 하위 경로로 이동
                  GoRoute(
                    path: 'detail/:id',
                    name: 'recipe_detail',
                    builder: (context, state) {
                      // URL에서 ':id' 부분을 가져옵니다.
                      final id = state.pathParameters['id']!;

                      // 💡 생성자가 요구하는 정확한 이름인 'recipeId'를 사용하세요.
                      return RecipeDetailScreen(recipeId: id);
                    },
                  ),
                ],
              ),
            ],
          ),
          // 3번 탭: 검색
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/search',
                builder: (context, state) => const Center(child: Text('검색 화면')),
              ),
            ],
          ),
          // 4번 탭: 마이페이지
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
