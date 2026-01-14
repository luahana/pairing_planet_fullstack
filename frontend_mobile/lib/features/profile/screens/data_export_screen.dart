import 'dart:convert';
import 'dart:io';

import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:pairing_planet2_frontend/core/theme/app_colors.dart';
import 'package:pairing_planet2_frontend/core/network/dio_provider.dart';
import 'package:pairing_planet2_frontend/core/constants/constants.dart';
import 'package:pairing_planet2_frontend/core/utils/logger.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart' show ShareParams, SharePlus, XFile;

/// GDPR-compliant data export screen
/// Allows users to download their personal data in JSON format
class DataExportScreen extends ConsumerStatefulWidget {
  const DataExportScreen({super.key});

  @override
  ConsumerState<DataExportScreen> createState() => _DataExportScreenState();
}

class _DataExportScreenState extends ConsumerState<DataExportScreen> {
  bool _isExporting = false;
  String? _error;
  double _progress = 0;
  String _currentStep = '';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        title: Text('dataExport.title'.tr()),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
      ),
      body: ListView(
        padding: EdgeInsets.all(16.r),
        children: [
          // Info card
          _buildInfoCard(),

          SizedBox(height: 24.h),

          // What's included section
          _buildWhatIsIncludedSection(),

          SizedBox(height: 24.h),

          // Export button or progress
          if (_isExporting)
            _buildProgressIndicator()
          else
            _buildExportButton(),

          if (_error != null) ...[
            SizedBox(height: 16.h),
            _buildErrorCard(),
          ],

          SizedBox(height: 32.h),

          // Privacy note
          _buildPrivacyNote(),
        ],
      ),
    );
  }

  Widget _buildInfoCard() {
    return Container(
      padding: EdgeInsets.all(16.r),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12.r),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.download_rounded, color: AppColors.primary, size: 24.sp),
              SizedBox(width: 12.w),
              Expanded(
                child: Text(
                  'dataExport.headline'.tr(),
                  style: TextStyle(
                    fontSize: 18.sp,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
            ],
          ),
          SizedBox(height: 12.h),
          Text(
            'dataExport.description'.tr(),
            style: TextStyle(
              fontSize: 14.sp,
              color: Colors.grey[700],
              height: 1.5,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildWhatIsIncludedSection() {
    return Container(
      padding: EdgeInsets.all(16.r),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12.r),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'dataExport.whatIsIncluded'.tr(),
            style: TextStyle(
              fontSize: 16.sp,
              fontWeight: FontWeight.w600,
            ),
          ),
          SizedBox(height: 16.h),
          _buildIncludedItem(Icons.person_outline, 'dataExport.profileData'.tr()),
          _buildIncludedItem(Icons.restaurant_menu, 'dataExport.recipesData'.tr()),
          _buildIncludedItem(Icons.book_outlined, 'dataExport.logsData'.tr()),
          _buildIncludedItem(Icons.bookmark_border, 'dataExport.savedData'.tr()),
          _buildIncludedItem(Icons.people_outline, 'dataExport.followData'.tr()),
        ],
      ),
    );
  }

  Widget _buildIncludedItem(IconData icon, String text) {
    return Padding(
      padding: EdgeInsets.symmetric(vertical: 8.h),
      child: Row(
        children: [
          Icon(icon, size: 20.sp, color: AppColors.textSecondary),
          SizedBox(width: 12.w),
          Expanded(
            child: Text(
              text,
              style: TextStyle(fontSize: 14.sp),
            ),
          ),
          Icon(Icons.check, size: 18.sp, color: Colors.green),
        ],
      ),
    );
  }

  Widget _buildExportButton() {
    return ElevatedButton.icon(
      onPressed: _exportData,
      icon: const Icon(Icons.download),
      label: Text('dataExport.exportButton'.tr()),
      style: ElevatedButton.styleFrom(
        backgroundColor: AppColors.primary,
        foregroundColor: Colors.white,
        padding: EdgeInsets.symmetric(vertical: 16.h),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12.r),
        ),
        textStyle: TextStyle(
          fontSize: 16.sp,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }

  Widget _buildProgressIndicator() {
    return Container(
      padding: EdgeInsets.all(24.r),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12.r),
      ),
      child: Column(
        children: [
          CircularProgressIndicator(
            value: _progress > 0 ? _progress : null,
            color: AppColors.primary,
          ),
          SizedBox(height: 16.h),
          Text(
            _currentStep,
            style: TextStyle(
              fontSize: 14.sp,
              color: Colors.grey[700],
            ),
            textAlign: TextAlign.center,
          ),
          if (_progress > 0) ...[
            SizedBox(height: 8.h),
            Text(
              '${(_progress * 100).toInt()}%',
              style: TextStyle(
                fontSize: 12.sp,
                color: Colors.grey[500],
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildErrorCard() {
    return Container(
      padding: EdgeInsets.all(12.r),
      decoration: BoxDecoration(
        color: Colors.red[50],
        borderRadius: BorderRadius.circular(8.r),
        border: Border.all(color: Colors.red[200]!),
      ),
      child: Row(
        children: [
          Icon(Icons.error_outline, size: 20.sp, color: Colors.red[700]),
          SizedBox(width: 8.w),
          Expanded(
            child: Text(
              _error!,
              style: TextStyle(fontSize: 13.sp, color: Colors.red[800]),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPrivacyNote() {
    return Container(
      padding: EdgeInsets.all(12.r),
      decoration: BoxDecoration(
        color: Colors.grey[100],
        borderRadius: BorderRadius.circular(8.r),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(Icons.privacy_tip_outlined, size: 18.sp, color: Colors.grey[600]),
          SizedBox(width: 8.w),
          Expanded(
            child: Text(
              'dataExport.privacyNote'.tr(),
              style: TextStyle(
                fontSize: 12.sp,
                color: Colors.grey[700],
                height: 1.4,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _exportData() async {
    setState(() {
      _isExporting = true;
      _error = null;
      _progress = 0;
    });

    try {
      final dio = ref.read(dioProvider);
      final exportData = <String, dynamic>{
        'exportDate': DateTime.now().toIso8601String(),
        'exportVersion': '1.0',
      };

      // Step 1: Fetch profile data
      _updateProgress(0.1, 'dataExport.fetchingProfile'.tr());
      try {
        final profileResponse = await dio.get(ApiEndpoints.myProfile);
        exportData['profile'] = profileResponse.data;
      } catch (e) {
        talker.warning('Failed to fetch profile: $e');
        exportData['profile'] = {'error': 'Failed to fetch profile data'};
      }

      // Step 2: Fetch user's recipes
      _updateProgress(0.3, 'dataExport.fetchingRecipes'.tr());
      try {
        final recipesResponse = await dio.get(
          ApiEndpoints.myRecipes,
          queryParameters: {'size': 1000},
        );
        exportData['recipes'] = recipesResponse.data['content'] ?? [];
      } catch (e) {
        talker.warning('Failed to fetch recipes: $e');
        exportData['recipes'] = {'error': 'Failed to fetch recipes'};
      }

      // Step 3: Fetch user's logs
      _updateProgress(0.5, 'dataExport.fetchingLogs'.tr());
      try {
        final logsResponse = await dio.get(
          ApiEndpoints.myLogs,
          queryParameters: {'size': 1000},
        );
        exportData['logs'] = logsResponse.data['content'] ?? [];
      } catch (e) {
        talker.warning('Failed to fetch logs: $e');
        exportData['logs'] = {'error': 'Failed to fetch cooking logs'};
      }

      // Step 4: Fetch saved recipes
      _updateProgress(0.7, 'dataExport.fetchingSaved'.tr());
      try {
        final savedResponse = await dio.get(
          ApiEndpoints.savedRecipes,
          queryParameters: {'size': 1000},
        );
        exportData['savedRecipes'] = savedResponse.data['content'] ?? [];
      } catch (e) {
        talker.warning('Failed to fetch saved recipes: $e');
        exportData['savedRecipes'] = {'error': 'Failed to fetch saved recipes'};
      }

      // Step 5: Fetch cooking DNA
      _updateProgress(0.8, 'dataExport.fetchingStats'.tr());
      try {
        final dnaResponse = await dio.get(ApiEndpoints.myCookingDna);
        exportData['cookingDna'] = dnaResponse.data;
      } catch (e) {
        talker.warning('Failed to fetch cooking DNA: $e');
        exportData['cookingDna'] = {'error': 'Failed to fetch cooking statistics'};
      }

      // Step 6: Save and share file
      _updateProgress(0.9, 'dataExport.preparingFile'.tr());

      final jsonString = const JsonEncoder.withIndent('  ').convert(exportData);
      final timestamp = DateFormat('yyyyMMdd_HHmmss').format(DateTime.now());
      final fileName = 'pairing_planet_data_$timestamp.json';

      final directory = await getTemporaryDirectory();
      final file = File('${directory.path}/$fileName');
      await file.writeAsString(jsonString);

      _updateProgress(1.0, 'dataExport.complete'.tr());

      if (!mounted) return;

      // Share the file
      await SharePlus.instance.share(
        ShareParams(
          files: [XFile(file.path)],
          subject: 'dataExport.emailSubject'.tr(),
        ),
      );

    } catch (e) {
      talker.error('Data export failed: $e');
      if (mounted) {
        setState(() {
          _error = 'dataExport.exportFailed'.tr();
        });
      }
    } finally {
      if (mounted) {
        setState(() {
          _isExporting = false;
          _progress = 0;
        });
      }
    }
  }

  void _updateProgress(double progress, String step) {
    if (mounted) {
      setState(() {
        _progress = progress;
        _currentStep = step;
      });
    }
  }
}
