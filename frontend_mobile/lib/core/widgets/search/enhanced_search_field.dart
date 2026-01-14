import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:pairing_planet2_frontend/core/providers/search_history_provider.dart';
import 'package:pairing_planet2_frontend/data/datasources/search/search_local_data_source.dart';

/// A reusable search field with debounce and history support.
/// Can be used in AppBars, SliverAppBars, BottomSheets, or any other context.
class EnhancedSearchField extends ConsumerStatefulWidget {
  /// Placeholder text for the search field.
  final String hintText;

  /// Called when search query changes (after debounce).
  final ValueChanged<String> onSearch;

  /// Called when search is cleared.
  final VoidCallback? onClear;

  /// Initial/external query value to sync with.
  final String? currentQuery;

  /// Type of search for history management.
  final SearchType searchType;

  /// Whether to auto-focus on mount.
  final bool autofocus;

  /// Text style for the input.
  final TextStyle? textStyle;

  /// Hint style for the input.
  final TextStyle? hintStyle;

  /// Called when the text changes (before debounce).
  final ValueChanged<String>? onTextChanged;

  /// Called when the field gains focus.
  final VoidCallback? onFocus;

  const EnhancedSearchField({
    super.key,
    required this.hintText,
    required this.onSearch,
    this.onClear,
    this.currentQuery,
    required this.searchType,
    this.autofocus = false,
    this.textStyle,
    this.hintStyle,
    this.onTextChanged,
    this.onFocus,
  });

  @override
  ConsumerState<EnhancedSearchField> createState() =>
      _EnhancedSearchFieldState();
}

class _EnhancedSearchFieldState extends ConsumerState<EnhancedSearchField> {
  final TextEditingController _controller = TextEditingController();
  final FocusNode _focusNode = FocusNode();

  StateNotifierProvider<SearchHistoryNotifier, List<String>> get _historyProvider {
    return widget.searchType == SearchType.recipe
        ? recipeSearchHistoryProvider
        : logPostSearchHistoryProvider;
  }

  @override
  void initState() {
    super.initState();

    // Initialize with current query if provided
    if (widget.currentQuery != null && widget.currentQuery!.isNotEmpty) {
      _controller.text = widget.currentQuery!;
    }

    // Listen to focus changes
    _focusNode.addListener(_onFocusChange);

    // Autofocus if requested
    if (widget.autofocus) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _focusNode.requestFocus();
      });
    }
  }

  @override
  void didUpdateWidget(EnhancedSearchField oldWidget) {
    super.didUpdateWidget(oldWidget);
    // Sync controller with external query changes
    if (widget.currentQuery != oldWidget.currentQuery) {
      if (widget.currentQuery == null || widget.currentQuery!.isEmpty) {
        _controller.clear();
      } else if (widget.currentQuery != _controller.text) {
        _controller.text = widget.currentQuery!;
      }
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    _focusNode.removeListener(_onFocusChange);
    _focusNode.dispose();
    super.dispose();
  }

  void _onFocusChange() {
    if (_focusNode.hasFocus) {
      widget.onFocus?.call();
    }
  }

  void _onSearchChanged(String value) {
    widget.onTextChanged?.call(value);
    setState(() {}); // Update UI for clear button visibility
  }

  void _onSearchSubmitted(String value) {
    if (value.trim().isNotEmpty) {
      ref.read(_historyProvider.notifier).addSearch(value.trim());
      widget.onSearch(value);
      _focusNode.unfocus();
    }
  }

  void _clearSearch() {
    _controller.clear();
    widget.onClear?.call();
    // Re-trigger search with empty string
    widget.onSearch('');
  }

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 40.h,
      child: Center(
        child: TextField(
          controller: _controller,
          focusNode: _focusNode,
          textAlignVertical: TextAlignVertical.center,
          decoration: InputDecoration(
            hintText: widget.hintText,
            hintStyle: widget.hintStyle ?? TextStyle(color: Colors.grey[400]),
            border: InputBorder.none,
            isDense: true,
            contentPadding: EdgeInsets.symmetric(horizontal: 0, vertical: 8.h),
            suffixIcon: _controller.text.isNotEmpty
                ? IconButton(
                    icon: Icon(Icons.clear, size: 20.sp),
                    onPressed: _clearSearch,
                    padding: EdgeInsets.zero,
                    constraints: const BoxConstraints(),
                  )
                : null,
            suffixIconConstraints: BoxConstraints(
              minHeight: 40.h,
              minWidth: 40.w,
            ),
          ),
          style: widget.textStyle ?? TextStyle(fontSize: 16.sp),
          onChanged: _onSearchChanged,
          textInputAction: TextInputAction.search,
          onSubmitted: _onSearchSubmitted,
        ),
      ),
    );
  }
}
