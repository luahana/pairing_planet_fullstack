import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:pairing_planet2_frontend/core/services/social_auth_service.dart';
import 'package:pairing_planet2_frontend/core/utils/logger.dart';
import 'package:pairing_planet2_frontend/features/auth/providers/auth_provider.dart'; // 본인의 경로에 맞게 수정

class LoginScreen extends ConsumerWidget {
  const LoginScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Pairing Planet 로그인'),
        centerTitle: true,
      ),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(20.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Text(
                '환영합니다!',
                style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 40),

              // 💡 구글 로그인 버튼
              SizedBox(
                width: double.infinity,
                height: 50,
                child: ElevatedButton.icon(
                  icon: const Icon(Icons.login),
                  label: const Text('Google 계정으로 시작하기'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.white,
                    foregroundColor: Colors.black,
                    side: const BorderSide(color: Colors.grey),
                  ),
                  onPressed: () async {
                    // 1. 파이어베이스 인증 수행
                    final String? firebaseIdToken = await ref
                        .read(socialAuthServiceProvider)
                        .signInWithGoogle();

                    if (firebaseIdToken != null) {
                      // 2. 💡 Domain 레이어의 Repository를 통해 백엔드 인증 수행
                      final result = await ref
                          .read(authRepositoryProvider)
                          .socialLogin(firebaseIdToken);

                      if (!context.mounted) return;

                      result.fold(
                        (failure) {
                          // 실패 처리
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(content: Text('서버 인증 실패: $failure')),
                          );
                        },
                        (_) {
                          // 3. 백엔드 인증 및 토큰 저장 성공 시에만 상태 업데이트
                          ref.read(authStateProvider.notifier).loginSuccess();
                          talker.info("인증 성공: 홈 화면으로 리다이렉트");
                        },
                      );
                    }
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
