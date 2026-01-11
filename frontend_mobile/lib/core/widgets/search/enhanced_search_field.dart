import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:pairing_planet2_frontend/core/providers/search_history_provider.dart';
import 'package:pairing_planet2_frontend/core/widgets/search/search_suggestions_overlay.dart';
import 'package:pairing_planet2_frontend/data/datasources/search/search_local_data_source.dart';

/// A reusable search field with debounce, suggestions overlay, and history support.
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

  /// External LayerLink for overlay positioning.
  /// If not provided, creates its own LayerLink and wraps in CompositedTransformTarget.
  final LayerLink? layerLink;

  /// Width for the suggestions overlay.
  /// If not provided, uses MediaQuery width.
  final double? overlayWidth;

  /// Offset for the overlay from the field.
  final Offset overlayOffset;

  /// Text style for the input.
  final TextStyle? textStyle;

  /// Hint style for the input.
  final TextStyle? hintStyle;

  /// Called when the text changes (before debounce).
  final ValueChanged<String>? onTextChanged;

  const EnhancedSearchField({
    super.key,
    required this.hintText,
    required this.onSearch,
    this.onClear,
    this.currentQuery,
    required this.searchType,
    this.autofocus = false,
    this.layerLink,
    this.overlayWidth,
    this.overlayOffset = const Offset(0, 48),
    this.textStyle,
    this.hintStyle,
    this.onTextChanged,
  });

  @override
  ConsumerState<EnhancedSearchField> createState() =>
      _EnhancedSearchFieldState();
}

class _EnhancedSearchFieldState extends ConsumerState<EnhancedSearchField> {
  final TextEditingController _controller = TextEditingController();
  final FocusNode _focusNode = FocusNode();
  late final LayerLink _ownedLayerLink;
  Timer? _debounce;
  OverlayEntry? _overlayEntry;

  LayerLink get _layerLink => widget.layerLink ?? _ownedLayerLink;
  bool get _usesExternalLayerLink => widget.layerLink != null;

  StateNotifierProvider<SearchHistoryNotifier, List<String>> get _historyProvider {
    return widget.searchType == SearchType.recipe
        ? recipeSearchHistoryProvider
        : logPostSearchHistoryProvider;
  }

  @override
  void initState() {
    super.initState();
    _ownedLayerLink = LayerLink();

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
        _hideOverlay();
      } else if (widget.currentQuery != _controller.text) {
        _controller.text = widget.currentQuery!;
      }
    }
  }

  @override
  void dispose() {
    _debounce?.cancel();
    _controller.dispose();
    _focusNode.removeListener(_onFocusChange);
    _focusNode.dispose();
    _hideOverlay();
    super.dispose();
  }

  void _onFocusChange() {
    if (_focusNode.hasFocus) {
      _showOverlay();
    } else {
      // Delay hiding to allow tap on suggestions
      Future.delayed(const Duration(milliseconds: 200), () {
        if (!_focusNode.hasFocus && mounted) {
          _hideOverlay();
        }
      });
    }
  }

  void _showOverlay() {
    _hideOverlay(); // Remove any existing overlay first

    final suggestions = ref.read(_historyProvider.notifier).getSuggestions(
          _controller.text,
        );

    if (suggestions.isEmpty && _controller.text.isEmpty) {
      return; // Don't show overlay if no history and no query
    }

    final overlay = Overlay.of(context);
    final width =
        widget.overlayWidth ?? MediaQuery.of(context).size.width - 32.w;

    _overlayEntry = OverlayEntry(
      builder: (context) => Consumer(
        builder: (context, ref, _) {
          final currentSuggestions = ref
              .read(_historyProvider.notifier)
              .getSuggestions(_controller.text);

          return Stack(
            children: [
              // Dismiss area
              Positioned.fill(
                child: GestureDetector(
                  onTap: () {
                    _focusNode.unfocus();
                    _hideOverlay();
                  },
                  behavior: HitTestBehavior.translucent,
                  child: Container(color: Colors.transparent),
                ),
              ),
              // Overlay content
              SearchSuggestionsOverlay(
                layerLink: _layerLink,
                query: _controller.text,
                suggestions: currentSuggestions,
                width: width,
                offset: widget.overlayOffset,
                onSuggestionTap: (term) {
                  _controller.text = term;
                  _controller.selection = TextSelection.fromPosition(
                    TextPosition(offset: term.length),
                  );
                  widget.onSearch(term);
                  ref.read(_historyProvider.notifier).addSearch(term);
                  _hideOverlay();
                  _focusNode.unfocus();
                },
                onRemoveTap: (term) {
                  ref.read(_historyProvider.notifier).removeSearch(term);
                  _updateOverlay();
                },
                onClearAllTap: () {
                  ref.read(_historyProvider.notifier).clearAll();
                  _hideOverlay();
                },
              ),
            ],
          );
        },
      ),
    );

    overlay.insert(_overlayEntry!);
  }

  void _hideOverlay() {
    _overlayEntry?.remove();
    _overlayEntry = null;
  }

  void _updateOverlay() {
    if (_overlayEntry != null) {
      _overlayEntry!.markNeedsBuild();
    }
  }

  void _onSearchChanged(String value) {
    widget.onTextChanged?.call(value);
    _debounce?.cancel();
    _updateOverlay();

    _debounce = Timer(const Duration(milliseconds: 400), () {
      widget.onSearch(value);
    });
  }

  void _onSearchSubmitted(String value) {
    if (value.trim().isNotEmpty) {
      ref.read(_historyProvider.notifier).addSearch(value.trim());
      widget.onSearch(value);
      _hideOverlay();
      _focusNode.unfocus();
    }
  }

  void _clearSearch() {
    _controller.clear();
    widget.onClear?.call();
    _updateOverlay();
    // Re-trigger search with empty string
    widget.onSearch('');
  }

  @override
  Widget build(BuildContext context) {
    final textField = TextField(
      controller: _controller,
      focusNode: _focusNode,
      decoration: InputDecoration(
        hintText: widget.hintText,
        hintStyle: widget.hintStyle ?? TextStyle(color: Colors.grey[400]),
        border: InputBorder.none,
        contentPadding: EdgeInsets.zero,
        suffixIcon: _controller.text.isNotEmpty
            ? IconButton(
                icon: Icon(Icons.clear, size: 20.sp),
                onPressed: _clearSearch,
              )
            : null,
      ),
      style: widget.textStyle ?? TextStyle(fontSize: 16.sp),
      onChanged: _onSearchChanged,
      textInputAction: TextInputAction.search,
      onSubmitted: _onSearchSubmitted,
    );

    // If using external layer link, don't wrap in CompositedTransformTarget
    if (_usesExternalLayerLink) {
      return textField;
    }

    // Otherwise, wrap in our own CompositedTransformTarget
    return CompositedTransformTarget(
      link: _ownedLayerLink,
      child: textField,
    );
  }
}
