import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:pairing_planet2_frontend/core/providers/consent_preferences_provider.dart';
import 'package:url_launcher/url_launcher.dart';

/// GDPR-compliant consent banner shown on first app launch.
/// Asks users to accept or reject analytics tracking.
class ConsentBannerWidget extends ConsumerWidget {
  final VoidCallback onConsentGiven;

  const ConsentBannerWidget({
    super.key,
    required this.onConsentGiven,
  });

  static const String _privacyUrl = 'https://pairingplanet.com/privacy';

  Future<void> _launchPrivacyPolicy() async {
    final uri = Uri.parse(_privacyUrl);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Container(
      color: Colors.black54,
      child: Center(
        child: Container(
          margin: EdgeInsets.all(24.r),
          padding: EdgeInsets.all(24.r),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16.r),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.2),
                blurRadius: 20,
                offset: const Offset(0, 10),
              ),
            ],
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Icon(
                    Icons.cookie_outlined,
                    size: 28.sp,
                    color: Colors.orange[700],
                  ),
                  SizedBox(width: 12.w),
                  Expanded(
                    child: Text(
                      'consent.title'.tr(),
                      style: TextStyle(
                        fontSize: 20.sp,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ],
              ),
              SizedBox(height: 16.h),
              Text(
                'consent.description'.tr(),
                style: TextStyle(
                  fontSize: 14.sp,
                  color: Colors.grey[700],
                  height: 1.5,
                ),
              ),
              SizedBox(height: 12.h),
              _buildBulletPoint('consent.bullet1'.tr()),
              _buildBulletPoint('consent.bullet2'.tr()),
              _buildBulletPoint('consent.bullet3'.tr()),
              SizedBox(height: 16.h),
              GestureDetector(
                onTap: _launchPrivacyPolicy,
                child: Text(
                  'consent.learnMore'.tr(),
                  style: TextStyle(
                    fontSize: 14.sp,
                    color: Colors.orange[700],
                    decoration: TextDecoration.underline,
                  ),
                ),
              ),
              SizedBox(height: 24.h),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () async {
                        await ref
                            .read(consentPreferencesProvider.notifier)
                            .rejectAll();
                        onConsentGiven();
                      },
                      style: OutlinedButton.styleFrom(
                        foregroundColor: Colors.grey[700],
                        side: BorderSide(color: Colors.grey[400]!),
                        padding: EdgeInsets.symmetric(vertical: 14.h),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(8.r),
                        ),
                      ),
                      child: Text(
                        'consent.rejectAll'.tr(),
                        style: TextStyle(fontSize: 14.sp),
                      ),
                    ),
                  ),
                  SizedBox(width: 12.w),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: () async {
                        await ref
                            .read(consentPreferencesProvider.notifier)
                            .acceptAll();
                        onConsentGiven();
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.orange[700],
                        foregroundColor: Colors.white,
                        padding: EdgeInsets.symmetric(vertical: 14.h),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(8.r),
                        ),
                      ),
                      child: Text(
                        'consent.acceptAll'.tr(),
                        style: TextStyle(fontSize: 14.sp),
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildBulletPoint(String text) {
    return Padding(
      padding: EdgeInsets.only(bottom: 8.h),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '\u2022 ',
            style: TextStyle(fontSize: 14.sp, color: Colors.grey[700]),
          ),
          Expanded(
            child: Text(
              text,
              style: TextStyle(fontSize: 14.sp, color: Colors.grey[700]),
            ),
          ),
        ],
      ),
    );
  }
}
