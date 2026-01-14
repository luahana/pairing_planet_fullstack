import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:pairing_planet2_frontend/core/providers/search_history_provider.dart';
import 'package:pairing_planet2_frontend/core/theme/app_colors.dart';
import 'package:pairing_planet2_frontend/core/widgets/search/recent_search_tile.dart';
import 'package:pairing_planet2_frontend/data/datasources/search/search_local_data_source.dart';

/// Full-screen view showing search history.
/// Displays recent searches with options to select, remove, or clear all.
class SearchHistoryView extends ConsumerWidget {
  /// Current text in the search field for filtering suggestions.
  final String currentQuery;

  /// Called when a search term is selected.
  final ValueChanged<String> onSearchSelected;

  /// Type of search for history management.
  final SearchType searchType;

  const SearchHistoryView({
    super.key,
    required this.currentQuery,
    required this.onSearchSelected,
    required this.searchType,
  });

  StateNotifierProvider<SearchHistoryNotifier, List<String>> get _historyProvider {
    return searchType == SearchType.recipe
        ? recipeSearchHistoryProvider
        : logPostSearchHistoryProvider;
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final history = ref.watch(_historyProvider);

    // Filter history based on current query
    final filteredHistory = currentQuery.isEmpty
        ? history
        : history
            .where((term) => term.toLowerCase().contains(currentQuery.toLowerCase()))
            .toList();

    if (filteredHistory.isEmpty && currentQuery.isEmpty) {
      return _buildEmptyState();
    }

    return Container(
      color: Colors.white,
      child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Header with "Recent Searches" and "Clear All"
        if (filteredHistory.isNotEmpty) ...[
          Padding(
            padding: EdgeInsets.fromLTRB(16.w, 16.h, 16.w, 8.h),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'search.recentSearches'.tr(),
                  style: TextStyle(
                    fontSize: 14.sp,
                    fontWeight: FontWeight.w600,
                    color: Colors.grey[600],
                  ),
                ),
                GestureDetector(
                  onTap: () {
                    ref.read(_historyProvider.notifier).clearAll();
                  },
                  child: Text(
                    'search.clearAll'.tr(),
                    style: TextStyle(
                      fontSize: 13.sp,
                      color: AppColors.primary,
                    ),
                  ),
                ),
              ],
            ),
          ),
          Divider(height: 1.h, color: Colors.grey[200]),
        ],

        // List of recent searches
        Expanded(
          child: filteredHistory.isEmpty
              ? _buildNoMatchState()
              : ListView.separated(
                  padding: EdgeInsets.zero,
                  itemCount: filteredHistory.length,
                  separatorBuilder: (context, index) => Divider(
                    height: 1.h,
                    indent: 48.w,
                    color: Colors.grey[200],
                  ),
                  itemBuilder: (context, index) {
                    final term = filteredHistory[index];
                    return RecentSearchTile(
                      term: term,
                      query: currentQuery.isNotEmpty ? currentQuery : null,
                      onTap: () => onSearchSelected(term),
                      onRemove: () {
                        ref.read(_historyProvider.notifier).removeSearch(term);
                      },
                    );
                  },
                ),
        ),
      ],
      ),
    );
  }

  Widget _buildEmptyState() {
    return Container(
      color: Colors.white,
      child: Center(
        child: Padding(
          padding: EdgeInsets.all(32.r),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                Icons.history,
                size: 64.sp,
                color: Colors.grey[300],
              ),
              SizedBox(height: 16.h),
              Text(
                'search.noRecentSearches'.tr(),
                style: TextStyle(
                  fontSize: 16.sp,
                  color: Colors.grey[500],
                ),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildNoMatchState() {
    return Center(
      child: Padding(
        padding: EdgeInsets.all(32.r),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.search_off,
              size: 48.sp,
              color: Colors.grey[300],
            ),
            SizedBox(height: 16.h),
            Text(
              'search.noResults'.tr(),
              style: TextStyle(
                fontSize: 14.sp,
                color: Colors.grey[500],
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}
