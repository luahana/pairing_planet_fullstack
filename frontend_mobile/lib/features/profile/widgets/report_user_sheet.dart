import 'dart:async';

import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:pairing_planet2_frontend/core/theme/app_colors.dart';
import 'package:pairing_planet2_frontend/data/models/report/report_reason.dart';
import 'package:pairing_planet2_frontend/features/profile/providers/block_provider.dart';

/// Bottom sheet for reporting a user
class ReportUserSheet extends ConsumerStatefulWidget {
  final String userId;

  const ReportUserSheet({
    super.key,
    required this.userId,
  });

  static Future<void> show(BuildContext context, String userId) {
    return showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.white,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16.r)),
      ),
      builder: (context) => ReportUserSheet(userId: userId),
    );
  }

  @override
  ConsumerState<ReportUserSheet> createState() => _ReportUserSheetState();
}

class _ReportUserSheetState extends ConsumerState<ReportUserSheet> {
  ReportReason? _selectedReason;
  final _descriptionController = TextEditingController();
  Timer? _debounce;

  @override
  void dispose() {
    _descriptionController.dispose();
    _debounce?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final reportState = ref.watch(reportActionProvider(widget.userId));

    return Padding(
      padding: EdgeInsets.only(
        bottom: MediaQuery.of(context).viewInsets.bottom,
      ),
      child: SingleChildScrollView(
        child: Padding(
          padding: EdgeInsets.all(16.r),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Handle bar
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
              // Title
              Text(
                'profile.reportTitle'.tr(),
                style: TextStyle(
                  fontSize: 18.sp,
                  fontWeight: FontWeight.bold,
                ),
              ),
              SizedBox(height: 16.h),
              // Reason selection
              Text(
                'report.selectReason'.tr(),
                style: TextStyle(
                  fontSize: 14.sp,
                  color: Colors.grey[600],
                ),
              ),
              SizedBox(height: 8.h),
              ...ReportReason.values.map((reason) => _buildReasonTile(reason)),
              SizedBox(height: 16.h),
              // Description field
              TextField(
                controller: _descriptionController,
                maxLines: 3,
                maxLength: 1000,
                decoration: InputDecoration(
                  hintText: 'report.descriptionHint'.tr(),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(8.r),
                  ),
                ),
              ),
              SizedBox(height: 16.h),
              // Submit button
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: reportState.isLoading || _selectedReason == null
                      ? null
                      : _onSubmit,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primary,
                    foregroundColor: Colors.white,
                    padding: EdgeInsets.symmetric(vertical: 14.h),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8.r),
                    ),
                  ),
                  child: reportState.isLoading
                      ? SizedBox(
                          width: 20.w,
                          height: 20.h,
                          child: const CircularProgressIndicator(
                            strokeWidth: 2,
                            color: Colors.white,
                          ),
                        )
                      : Text('report.submit'.tr()),
                ),
              ),
              SizedBox(height: 16.h),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildReasonTile(ReportReason reason) {
    final isSelected = _selectedReason == reason;

    return InkWell(
      onTap: () => setState(() => _selectedReason = reason),
      child: Padding(
        padding: EdgeInsets.symmetric(vertical: 8.h),
        child: Row(
          children: [
            Icon(
              isSelected ? Icons.radio_button_checked : Icons.radio_button_off,
              color: isSelected ? AppColors.primary : Colors.grey,
              size: 24.sp,
            ),
            SizedBox(width: 12.w),
            Expanded(
              child: Text(
                _getReasonLabel(reason),
                style: TextStyle(fontSize: 14.sp),
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _getReasonLabel(ReportReason reason) {
    switch (reason) {
      case ReportReason.spam:
        return 'report.reason.spam'.tr();
      case ReportReason.harassment:
        return 'report.reason.harassment'.tr();
      case ReportReason.inappropriateContent:
        return 'report.reason.inappropriateContent'.tr();
      case ReportReason.impersonation:
        return 'report.reason.impersonation'.tr();
      case ReportReason.other:
        return 'report.reason.other'.tr();
    }
  }

  void _onSubmit() {
    // Debounce to prevent double submission
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 300), () async {
      if (_selectedReason == null) return;

      final notifier = ref.read(reportActionProvider(widget.userId).notifier);
      final success = await notifier.report(
        _selectedReason!,
        description: _descriptionController.text.isEmpty
            ? null
            : _descriptionController.text,
      );

      if (mounted) {
        if (success) {
          Navigator.pop(context);
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('profile.reportSubmitted'.tr())),
          );
        } else {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('common.error'.tr())),
          );
        }
      }
    });
  }
}
