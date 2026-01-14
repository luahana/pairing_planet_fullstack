import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:pairing_planet2_frontend/core/widgets/search/enhanced_search_field.dart';
import 'package:pairing_planet2_frontend/data/datasources/search/search_local_data_source.dart';

/// An enhanced search AppBar with suggestions overlay and history support.
/// Uses EnhancedSearchField for core search functionality.
class EnhancedSearchAppBar extends ConsumerStatefulWidget
    implements PreferredSizeWidget {
  final String title;
  final String hintText;
  final ValueChanged<String> onSearch;
  final VoidCallback? onClear;
  final String? currentQuery;
  final SearchType searchType;
  final List<Widget>? actions;

  const EnhancedSearchAppBar({
    super.key,
    required this.title,
    required this.hintText,
    required this.onSearch,
    this.onClear,
    this.currentQuery,
    required this.searchType,
    this.actions,
  });

  @override
  Size get preferredSize => const Size.fromHeight(kToolbarHeight);

  @override
  ConsumerState<EnhancedSearchAppBar> createState() =>
      _EnhancedSearchAppBarState();
}

class _EnhancedSearchAppBarState extends ConsumerState<EnhancedSearchAppBar> {
  bool _showSearchField = false;

  @override
  void initState() {
    super.initState();
    // If there's an existing query, show the search field
    if (widget.currentQuery != null && widget.currentQuery!.isNotEmpty) {
      _showSearchField = true;
    }
  }

  @override
  void didUpdateWidget(EnhancedSearchAppBar oldWidget) {
    super.didUpdateWidget(oldWidget);
    // Sync search mode with external query changes
    if (widget.currentQuery != oldWidget.currentQuery) {
      if (widget.currentQuery == null || widget.currentQuery!.isEmpty) {
        if (_showSearchField) {
          setState(() => _showSearchField = false);
        }
      }
    }
  }

  void _toggleSearch() {
    setState(() {
      _showSearchField = !_showSearchField;
      if (!_showSearchField) {
        widget.onClear?.call();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return AppBar(
      backgroundColor: Colors.white,
      foregroundColor: Colors.black,
      elevation: 0,
      title: _showSearchField
          ? EnhancedSearchField(
              hintText: widget.hintText,
              onSearch: widget.onSearch,
              onClear: widget.onClear,
              currentQuery: widget.currentQuery,
              searchType: widget.searchType,
              autofocus: true,
            )
          : Text(
              widget.title,
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
      actions: [
        if (widget.actions != null && !_showSearchField) ...widget.actions!,
        IconButton(
          icon: Icon(_showSearchField ? Icons.close : Icons.search),
          onPressed: _toggleSearch,
        ),
      ],
    );
  }
}
