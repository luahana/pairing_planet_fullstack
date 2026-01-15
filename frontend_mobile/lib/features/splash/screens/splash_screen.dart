import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:go_router/go_router.dart';
import 'package:pairing_planet2_frontend/core/constants/constants.dart';
import 'package:pairing_planet2_frontend/core/theme/app_colors.dart';
import 'package:pairing_planet2_frontend/features/auth/providers/auth_provider.dart';
import 'package:pairing_planet2_frontend/features/splash/providers/splash_provider.dart';

class SplashScreen extends ConsumerStatefulWidget {
  const SplashScreen({super.key});

  @override
  ConsumerState<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends ConsumerState<SplashScreen>
    with SingleTickerProviderStateMixin {
  late AnimationController _fadeController;
  late Animation<double> _fadeAnimation;

  @override
  void initState() {
    super.initState();
    _fadeController = AnimationController(
      duration: const Duration(milliseconds: 800),
      vsync: this,
    );
    _fadeAnimation = CurvedAnimation(
      parent: _fadeController,
      curve: Curves.easeIn,
    );
    _fadeController.forward();
  }

  @override
  void dispose() {
    _fadeController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    // Listen to splash state and navigate when ready
    ref.listen<SplashState>(splashProvider, (previous, next) {
      if (next.status == SplashStatus.ready) {
        _navigateBasedOnAuth(next.authStatus);
      }
    });

    return Scaffold(
      backgroundColor: Colors.white,
      body: FadeTransition(
        opacity: _fadeAnimation,
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // Logo icon
              SvgPicture.asset(
                'assets/images/logo_icon.svg',
                width: 120.w,
                height: 120.w,
              ),
              SizedBox(height: 24.h),
              // App name
              Text(
                'Pairing Planet',
                style: TextStyle(
                  fontSize: 28.sp,
                  fontWeight: FontWeight.w600,
                  color: AppColors.textLogo,
                  letterSpacing: 1.5,
                ),
              ),
              SizedBox(height: 48.h),
              // Loading indicator
              SizedBox(
                width: 24.w,
                height: 24.w,
                child: CircularProgressIndicator(
                  strokeWidth: 2,
                  valueColor: AlwaysStoppedAnimation<Color>(AppColors.primary),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _navigateBasedOnAuth(AuthStatus? authStatus) {
    if (!mounted) return;

    final targetRoute = switch (authStatus) {
      AuthStatus.authenticated => RouteConstants.home,
      AuthStatus.needsAgeVerification => RouteConstants.ageVerification,
      AuthStatus.needsLegalAcceptance => RouteConstants.legalAgreement,
      AuthStatus.needsPhoneVerification => RouteConstants.phoneVerification,
      AuthStatus.guest => RouteConstants.home,
      AuthStatus.unauthenticated => RouteConstants.login,
      AuthStatus.initial => RouteConstants.login, // Fallback
      null => RouteConstants.login, // Fallback
    };

    context.go(targetRoute);
  }
}
