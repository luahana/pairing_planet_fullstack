import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:go_router/go_router.dart';
import 'package:pairing_planet2_frontend/core/constants/constants.dart';
import 'package:pairing_planet2_frontend/features/auth/providers/auth_provider.dart';

/// Age verification screen for COPPA/GDPR compliance.
/// Users must confirm they are 13+ before proceeding with signup.
class AgeVerificationScreen extends ConsumerStatefulWidget {
  const AgeVerificationScreen({super.key});

  @override
  ConsumerState<AgeVerificationScreen> createState() =>
      _AgeVerificationScreenState();
}

class _AgeVerificationScreenState extends ConsumerState<AgeVerificationScreen> {
  bool _isConfirmed = false;
  bool _isSubmitting = false;

  Future<void> _onContinue() async {
    if (_isSubmitting || !_isConfirmed) return;

    setState(() => _isSubmitting = true);

    await ref.read(authStateProvider.notifier).confirmAgeVerification();

    if (!mounted) return;

    // Navigation will be handled by router redirect
    final authState = ref.read(authStateProvider);
    if (authState.status == AuthStatus.needsLegalAcceptance) {
      context.go(RouteConstants.legalAgreement);
    } else if (authState.status == AuthStatus.authenticated) {
      context.go(RouteConstants.home);
    }
  }

  void _onDecline() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('ageVerification.underageTitle'.tr()),
        content: Text('ageVerification.underageMessage'.tr()),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.pop(context);
              ref.read(authStateProvider.notifier).logout();
              context.go(RouteConstants.login);
            },
            child: Text('common.confirm'.tr()),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('ageVerification.title'.tr()),
        centerTitle: true,
        automaticallyImplyLeading: false,
      ),
      body: SafeArea(
        child: Column(
          children: [
            Expanded(
              child: SingleChildScrollView(
                padding: EdgeInsets.all(24.r),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    SizedBox(height: 32.h),
                    Icon(
                      Icons.verified_user_outlined,
                      size: 80.sp,
                      color: Colors.orange[700],
                    ),
                    SizedBox(height: 24.h),
                    Text(
                      'ageVerification.headline'.tr(),
                      style: TextStyle(
                        fontSize: 24.sp,
                        fontWeight: FontWeight.bold,
                      ),
                      textAlign: TextAlign.center,
                    ),
                    SizedBox(height: 16.h),
                    Text(
                      'ageVerification.description'.tr(),
                      style: TextStyle(
                        fontSize: 14.sp,
                        color: Colors.grey[600],
                        height: 1.5,
                      ),
                      textAlign: TextAlign.center,
                    ),
                    SizedBox(height: 32.h),
                    _buildAgeCard(),
                    SizedBox(height: 24.h),
                    _buildCheckbox(),
                  ],
                ),
              ),
            ),
            _buildBottomButtons(),
          ],
        ),
      ),
    );
  }

  Widget _buildAgeCard() {
    return Container(
      width: double.infinity,
      padding: EdgeInsets.all(20.r),
      decoration: BoxDecoration(
        color: Colors.orange[50],
        borderRadius: BorderRadius.circular(16.r),
        border: Border.all(color: Colors.orange[200]!),
      ),
      child: Column(
        children: [
          Text(
            '13+',
            style: TextStyle(
              fontSize: 48.sp,
              fontWeight: FontWeight.bold,
              color: Colors.orange[700],
            ),
          ),
          SizedBox(height: 8.h),
          Text(
            'ageVerification.minimumAge'.tr(),
            style: TextStyle(
              fontSize: 16.sp,
              fontWeight: FontWeight.w500,
              color: Colors.orange[800],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCheckbox() {
    return InkWell(
      onTap: () => setState(() => _isConfirmed = !_isConfirmed),
      borderRadius: BorderRadius.circular(8.r),
      child: Padding(
        padding: EdgeInsets.symmetric(vertical: 8.h),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              width: 24.w,
              height: 24.h,
              child: Checkbox(
                value: _isConfirmed,
                onChanged: (value) =>
                    setState(() => _isConfirmed = value ?? false),
                activeColor: Colors.orange[700],
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(4.r),
                ),
              ),
            ),
            SizedBox(width: 12.w),
            Expanded(
              child: Text(
                'ageVerification.confirmText'.tr(),
                style: TextStyle(
                  fontSize: 14.sp,
                  color: Colors.black87,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildBottomButtons() {
    return Container(
      padding: EdgeInsets.all(20.r),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 10,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          SizedBox(
            width: double.infinity,
            height: 50.h,
            child: ElevatedButton(
              onPressed: _isConfirmed && !_isSubmitting ? _onContinue : null,
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.orange[700],
                foregroundColor: Colors.white,
                disabledBackgroundColor: Colors.grey[300],
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12.r),
                ),
              ),
              child: _isSubmitting
                  ? SizedBox(
                      width: 24.w,
                      height: 24.h,
                      child: const CircularProgressIndicator(
                        strokeWidth: 2,
                        valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                      ),
                    )
                  : Text(
                      'common.continue'.tr(),
                      style: TextStyle(
                        fontSize: 16.sp,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
            ),
          ),
          SizedBox(height: 12.h),
          TextButton(
            onPressed: _onDecline,
            child: Text(
              'ageVerification.notOldEnough'.tr(),
              style: TextStyle(
                fontSize: 14.sp,
                color: Colors.grey[600],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
