import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:pairing_planet2_frontend/core/theme/app_colors.dart';
import 'package:pairing_planet2_frontend/features/notification/providers/notification_preferences_provider.dart';

class NotificationSettingsScreen extends ConsumerWidget {
  const NotificationSettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final prefsState = ref.watch(notificationPreferencesProvider);

    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        title: Text('notificationSettings.title'.tr()),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
      ),
      body: prefsState.isLoading
          ? const Center(child: CircularProgressIndicator())
          : ListView(
              padding: EdgeInsets.symmetric(vertical: 16.h),
              children: [
                // Master toggle
                _buildMasterToggle(context, ref, prefsState),

                SizedBox(height: 24.h),

                // Individual notification types
                _buildSectionHeader('notificationSettings.notificationTypes'.tr()),

                _buildNotificationToggle(
                  context: context,
                  icon: Icons.restaurant_menu,
                  title: 'notificationSettings.recipeCookedTitle'.tr(),
                  subtitle: 'notificationSettings.recipeCookedSubtitle'.tr(),
                  value: prefsState.recipeCookedEnabled,
                  enabled: prefsState.allNotificationsEnabled,
                  onChanged: (value) {
                    ref
                        .read(notificationPreferencesProvider.notifier)
                        .setRecipeCookedEnabled(value);
                  },
                ),

                _buildNotificationToggle(
                  context: context,
                  icon: Icons.auto_awesome,
                  title: 'notificationSettings.recipeVariationTitle'.tr(),
                  subtitle: 'notificationSettings.recipeVariationSubtitle'.tr(),
                  value: prefsState.recipeVariationEnabled,
                  enabled: prefsState.allNotificationsEnabled,
                  onChanged: (value) {
                    ref
                        .read(notificationPreferencesProvider.notifier)
                        .setRecipeVariationEnabled(value);
                  },
                ),

                _buildNotificationToggle(
                  context: context,
                  icon: Icons.person_add,
                  title: 'notificationSettings.newFollowerTitle'.tr(),
                  subtitle: 'notificationSettings.newFollowerSubtitle'.tr(),
                  value: prefsState.newFollowerEnabled,
                  enabled: prefsState.allNotificationsEnabled,
                  onChanged: (value) {
                    ref
                        .read(notificationPreferencesProvider.notifier)
                        .setNewFollowerEnabled(value);
                  },
                ),

                SizedBox(height: 32.h),

                // Info section
                _buildInfoSection(context),

                SizedBox(height: 24.h),

                // Reset button
                _buildResetButton(context, ref),

                SizedBox(height: 32.h),
              ],
            ),
    );
  }

  Widget _buildMasterToggle(
    BuildContext context,
    WidgetRef ref,
    NotificationPreferencesState prefsState,
  ) {
    return Container(
      margin: EdgeInsets.symmetric(horizontal: 16.w),
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
      child: SwitchListTile(
        title: Text(
          'notificationSettings.enableAll'.tr(),
          style: TextStyle(
            fontSize: 16.sp,
            fontWeight: FontWeight.w600,
          ),
        ),
        subtitle: Text(
          'notificationSettings.enableAllSubtitle'.tr(),
          style: TextStyle(
            fontSize: 13.sp,
            color: Colors.grey[600],
          ),
        ),
        value: prefsState.allNotificationsEnabled,
        onChanged: (value) {
          ref
              .read(notificationPreferencesProvider.notifier)
              .setAllNotificationsEnabled(value);
        },
        activeTrackColor: AppColors.primary.withValues(alpha: 0.5),
        thumbColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return AppColors.primary;
          }
          return null;
        }),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12.r),
        ),
      ),
    );
  }

  Widget _buildSectionHeader(String title) {
    return Padding(
      padding: EdgeInsets.symmetric(horizontal: 16.w, vertical: 8.h),
      child: Text(
        title,
        style: TextStyle(
          fontSize: 13.sp,
          fontWeight: FontWeight.w600,
          color: Colors.grey[600],
        ),
      ),
    );
  }

  Widget _buildNotificationToggle({
    required BuildContext context,
    required IconData icon,
    required String title,
    required String subtitle,
    required bool value,
    required bool enabled,
    required ValueChanged<bool> onChanged,
  }) {
    return Container(
      margin: EdgeInsets.symmetric(horizontal: 16.w, vertical: 4.h),
      decoration: BoxDecoration(
        color: enabled ? Colors.white : Colors.grey[100],
        borderRadius: BorderRadius.circular(12.r),
        boxShadow: enabled
            ? [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.03),
                  blurRadius: 8,
                  offset: const Offset(0, 2),
                ),
              ]
            : null,
      ),
      child: SwitchListTile(
        secondary: Icon(
          icon,
          color: enabled ? AppColors.textPrimary : Colors.grey[400],
        ),
        title: Text(
          title,
          style: TextStyle(
            fontSize: 15.sp,
            fontWeight: FontWeight.w500,
            color: enabled ? Colors.black87 : Colors.grey[500],
          ),
        ),
        subtitle: Text(
          subtitle,
          style: TextStyle(
            fontSize: 13.sp,
            color: enabled ? Colors.grey[600] : Colors.grey[400],
          ),
        ),
        value: value && enabled,
        onChanged: enabled ? onChanged : null,
        activeTrackColor: AppColors.primary.withValues(alpha: 0.5),
        thumbColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return AppColors.primary;
          }
          return null;
        }),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12.r),
        ),
      ),
    );
  }

  Widget _buildInfoSection(BuildContext context) {
    return Container(
      margin: EdgeInsets.symmetric(horizontal: 16.w),
      padding: EdgeInsets.all(16.r),
      decoration: BoxDecoration(
        color: Colors.blue[50],
        borderRadius: BorderRadius.circular(12.r),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(
            Icons.info_outline,
            size: 20.sp,
            color: Colors.blue[700],
          ),
          SizedBox(width: 12.w),
          Expanded(
            child: Text(
              'notificationSettings.infoText'.tr(),
              style: TextStyle(
                fontSize: 13.sp,
                color: Colors.blue[800],
                height: 1.4,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildResetButton(BuildContext context, WidgetRef ref) {
    return Padding(
      padding: EdgeInsets.symmetric(horizontal: 16.w),
      child: OutlinedButton.icon(
        onPressed: () => _showResetConfirmDialog(context, ref),
        icon: const Icon(Icons.refresh),
        label: Text('notificationSettings.resetToDefault'.tr()),
        style: OutlinedButton.styleFrom(
          foregroundColor: Colors.grey[700],
          side: BorderSide(color: Colors.grey[300]!),
          padding: EdgeInsets.symmetric(vertical: 12.h),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8.r),
          ),
        ),
      ),
    );
  }

  void _showResetConfirmDialog(BuildContext context, WidgetRef ref) {
    showDialog(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: Text('notificationSettings.resetConfirmTitle'.tr()),
        content: Text('notificationSettings.resetConfirmMessage'.tr()),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: Text('common.cancel'.tr()),
          ),
          TextButton(
            onPressed: () {
              Navigator.pop(dialogContext);
              ref
                  .read(notificationPreferencesProvider.notifier)
                  .resetToDefaults();
            },
            child: Text(
              'notificationSettings.reset'.tr(),
              style: TextStyle(color: AppColors.primary),
            ),
          ),
        ],
      ),
    );
  }
}
