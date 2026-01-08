import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:pairing_planet2_frontend/core/services/toast_service.dart';

/// Bottom sheet for sharing a recipe to various platforms.
class ShareBottomSheet extends StatelessWidget {
  final String recipePublicId;
  final String recipeTitle;

  const ShareBottomSheet({
    super.key,
    required this.recipePublicId,
    required this.recipeTitle,
  });

  /// Show the share bottom sheet.
  static void show(BuildContext context, {
    required String recipePublicId,
    required String recipeTitle,
  }) {
    showModalBottomSheet(
      context: context,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20.r)),
      ),
      builder: (context) => ShareBottomSheet(
        recipePublicId: recipePublicId,
        recipeTitle: recipeTitle,
      ),
    );
  }

  String get shareUrl => 'https://api.pairingplanet.com/share/recipe/$recipePublicId';

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.symmetric(horizontal: 20.w, vertical: 16.h),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
          Center(
            child: Container(
              width: 40.w,
              height: 4.h,
              decoration: BoxDecoration(
                color: Colors.grey[300],
                borderRadius: BorderRadius.circular(2.r),
              ),
            ),
          ),
          SizedBox(height: 16.h),
          Text(
            '공유하기',
            style: TextStyle(
              fontSize: 18.sp,
              fontWeight: FontWeight.bold,
            ),
          ),
          SizedBox(height: 8.h),
          Text(
            recipeTitle,
            style: TextStyle(
              fontSize: 14.sp,
              color: Colors.grey[600],
            ),
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
          SizedBox(height: 20.h),

          // Share options grid
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              _ShareOption(
                icon: Icons.link,
                label: '링크 복사',
                color: Colors.grey[700]!,
                onTap: () => _copyLink(context),
              ),
              _ShareOption(
                icon: Icons.chat_bubble,
                label: '카카오톡',
                color: const Color(0xFFFEE500),
                iconColor: Colors.black,
                onTap: () => _shareToKakao(context),
              ),
              _ShareOption(
                icon: Icons.alternate_email,
                label: 'X (트위터)',
                color: Colors.black,
                onTap: () => _shareToTwitter(context),
              ),
              _ShareOption(
                icon: Icons.more_horiz,
                label: '더보기',
                color: Colors.grey[600]!,
                onTap: () => _shareMore(context),
              ),
            ],
          ),
          SizedBox(height: 20.h),

          // URL preview
          Container(
            padding: EdgeInsets.all(12.r),
            decoration: BoxDecoration(
              color: Colors.grey[100],
              borderRadius: BorderRadius.circular(8.r),
            ),
            child: Row(
              children: [
                Icon(Icons.link, size: 16.sp, color: Colors.grey[600]),
                SizedBox(width: 8.w),
                Expanded(
                  child: Text(
                    shareUrl,
                    style: TextStyle(
                      fontSize: 12.sp,
                      color: Colors.grey[600],
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
                GestureDetector(
                  onTap: () => _copyLink(context),
                  child: Icon(Icons.copy, size: 16.sp, color: Colors.indigo),
                ),
              ],
            ),
          ),
          SizedBox(height: 16.h),
        ],
      ),
    );
  }

  void _copyLink(BuildContext context) {
    Clipboard.setData(ClipboardData(text: shareUrl));
    Navigator.pop(context);
    ToastService.showSuccess('링크가 복사되었습니다');
  }

  void _shareToKakao(BuildContext context) {
    // TODO: Implement KakaoTalk sharing with KakaoSDK
    // For now, copy link and show message
    Clipboard.setData(ClipboardData(text: shareUrl));
    Navigator.pop(context);
    ToastService.showInfo('카카오톡 공유는 준비 중입니다. 링크가 복사되었습니다.');
  }

  void _shareToTwitter(BuildContext context) {
    // TODO: Implement Twitter sharing
    // For now, copy link and show message
    Clipboard.setData(ClipboardData(text: shareUrl));
    Navigator.pop(context);
    ToastService.showInfo('트위터 공유는 준비 중입니다. 링크가 복사되었습니다.');
  }

  void _shareMore(BuildContext context) {
    // TODO: Use share_plus for native share sheet
    Clipboard.setData(ClipboardData(text: shareUrl));
    Navigator.pop(context);
    ToastService.showInfo('링크가 복사되었습니다. 원하는 앱에 붙여넣기 해주세요.');
  }
}

class _ShareOption extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  final Color? iconColor;
  final VoidCallback onTap;

  const _ShareOption({
    required this.icon,
    required this.label,
    required this.color,
    this.iconColor,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Column(
        children: [
          Container(
            width: 56.w,
            height: 56.w,
            decoration: BoxDecoration(
              color: color,
              shape: BoxShape.circle,
            ),
            child: Icon(
              icon,
              color: iconColor ?? Colors.white,
              size: 24.sp,
            ),
          ),
          SizedBox(height: 8.h),
          Text(
            label,
            style: TextStyle(
              fontSize: 12.sp,
              color: Colors.grey[700],
            ),
          ),
        ],
      ),
    );
  }
}
