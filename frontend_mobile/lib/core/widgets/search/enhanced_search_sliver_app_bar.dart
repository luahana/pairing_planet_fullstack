import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:pairing_planet2_frontend/core/widgets/search/enhanced_search_field.dart';
import 'package:pairing_planet2_frontend/data/datasources/search/search_local_data_source.dart';

/// A SliverAppBar with integrated search functionality.
/// Provides toggle between title/custom content and search field with
/// debounce, history overlay, and suggestions.
class EnhancedSearchSliverAppBar extends ConsumerStatefulWidget {
  /// Title text shown when not searching.
  /// Ignored if titleWidget is provided.
  final String? title;

  /// Custom widget to show when not searching.
  /// Takes precedence over title.
  final Widget? titleWidget;

  /// Placeholder text for the search field.
  final String hintText;

  /// Called when search query changes (after debounce).
  final ValueChanged<String> onSearch;

  /// Called when search is cleared.
  final VoidCallback? onClear;

  /// Current query value for syncing state.
  final String? currentQuery;

  /// Type of search for history management.
  final SearchType searchType;

  /// Actions to show when not searching.
  final List<Widget>? actions;

  /// Whether the app bar should remain visible at the start of the scroll view.
  final bool pinned;

  /// Whether the app bar should become visible as soon as the user scrolls towards the app bar.
  final bool floating;

  /// Whether the app bar should stretch to fill the over-scroll area.
  final bool stretch;

  /// Called when search mode is toggled.
  final ValueChanged<bool>? onSearchModeChanged;

  /// Background color when inner content is scrolled.
  final Color? backgroundColor;

  /// Foreground color for icons and text.
  final Color? foregroundColor;

  /// Elevation when scrolled under.
  final double? scrolledUnderElevation;

  /// Title spacing (used when titleWidget is provided).
  final double? titleSpacing;

  const EnhancedSearchSliverAppBar({
    super.key,
    this.title,
    this.titleWidget,
    required this.hintText,
    required this.onSearch,
    this.onClear,
    this.currentQuery,
    required this.searchType,
    this.actions,
    this.pinned = true,
    this.floating = false,
    this.stretch = false,
    this.onSearchModeChanged,
    this.backgroundColor,
    this.foregroundColor,
    this.scrolledUnderElevation,
    this.titleSpacing,
  }) : assert(title != null || titleWidget != null,
            'Either title or titleWidget must be provided');

  @override
  ConsumerState<EnhancedSearchSliverAppBar> createState() =>
      _EnhancedSearchSliverAppBarState();
}

class _EnhancedSearchSliverAppBarState
    extends ConsumerState<EnhancedSearchSliverAppBar> {
  final LayerLink _layerLink = LayerLink();
  bool _isSearching = false;

  @override
  void initState() {
    super.initState();
    // If there's an existing query, start in search mode
    if (widget.currentQuery != null && widget.currentQuery!.isNotEmpty) {
      _isSearching = true;
    }
  }

  @override
  void didUpdateWidget(EnhancedSearchSliverAppBar oldWidget) {
    super.didUpdateWidget(oldWidget);
    // Sync search mode with external query changes
    if (widget.currentQuery != oldWidget.currentQuery) {
      if (widget.currentQuery == null || widget.currentQuery!.isEmpty) {
        if (_isSearching) {
          setState(() => _isSearching = false);
          widget.onSearchModeChanged?.call(false);
        }
      }
    }
  }

  void _toggleSearch() {
    setState(() {
      _isSearching = !_isSearching;
      if (!_isSearching) {
        widget.onClear?.call();
      }
    });
    widget.onSearchModeChanged?.call(_isSearching);
  }

  @override
  Widget build(BuildContext context) {
    return SliverAppBar(
      pinned: widget.pinned,
      floating: widget.floating,
      stretch: widget.stretch,
      backgroundColor: widget.backgroundColor ?? Colors.white,
      foregroundColor: widget.foregroundColor ?? Colors.black,
      scrolledUnderElevation: widget.scrolledUnderElevation,
      titleSpacing: widget.titleSpacing ?? (_isSearching ? 0 : null),
      title: CompositedTransformTarget(
        link: _layerLink,
        child: _isSearching
            ? _buildSearchField()
            : (widget.titleWidget ??
                Text(
                  widget.title!,
                  style: const TextStyle(fontWeight: FontWeight.bold),
                )),
      ),
      actions: [
        if (widget.actions != null && !_isSearching) ...widget.actions!,
        IconButton(
          icon: Icon(_isSearching ? Icons.close : Icons.search),
          onPressed: _toggleSearch,
        ),
      ],
    );
  }

  Widget _buildSearchField() {
    return Padding(
      padding: EdgeInsets.only(left: 8.w),
      child: EnhancedSearchField(
        hintText: widget.hintText,
        onSearch: widget.onSearch,
        onClear: widget.onClear,
        currentQuery: widget.currentQuery,
        searchType: widget.searchType,
        autofocus: true,
        layerLink: _layerLink,
        overlayOffset: const Offset(0, kToolbarHeight),
      ),
    );
  }
}
