import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:pairing_planet2_frontend/features/recipe/presentation/widgets/cooking_style_chips.dart';

void main() {
  Widget createTestWidget(Widget child) {
    return ScreenUtilInit(
      designSize: const Size(375, 812),
      minTextAdapt: true,
      builder: (context, _) => MaterialApp(
        home: Scaffold(
          body: child,
        ),
      ),
    );
  }

  group('CookingStyleChips', () {
    testWidgets('should display widget correctly', (tester) async {
      await tester.pumpWidget(createTestWidget(
        CookingStyleChips(
          onStyleSelected: (_) {},
        ),
      ));

      expect(find.byType(CookingStyleChips), findsOneWidget);
    });

    testWidgets('should display multiple style chips', (tester) async {
      await tester.pumpWidget(createTestWidget(
        CookingStyleChips(
          onStyleSelected: (_) {},
        ),
      ));

      // Should have a ListView for horizontal scrolling
      expect(find.byType(ListView), findsOneWidget);
      // Should have multiple GestureDetector for chip taps
      expect(find.byType(GestureDetector), findsWidgets);
    });

    testWidgets('should call onStyleSelected when chip tapped', (tester) async {
      String? selectedStyle;
      await tester.pumpWidget(createTestWidget(
        CookingStyleChips(
          onStyleSelected: (style) {
            selectedStyle = style;
          },
        ),
      ));

      // Tap the first chip (GestureDetector)
      final chips = find.byType(GestureDetector);
      expect(chips, findsWidgets);
      
      await tester.tap(chips.first);
      await tester.pumpAndSettle();

      // Should have called the callback with a value
      expect(selectedStyle, isNotNull);
    });

    testWidgets('should display header text', (tester) async {
      await tester.pumpWidget(createTestWidget(
        CookingStyleChips(
          onStyleSelected: (_) {},
        ),
      ));

      // Should have Text widgets for header and chip labels
      expect(find.byType(Text), findsWidgets);
    });

    testWidgets('should have correct chip styling', (tester) async {
      await tester.pumpWidget(createTestWidget(
        CookingStyleChips(
          onStyleSelected: (_) {},
        ),
      ));

      // Should have Container widgets for chip styling
      expect(find.byType(Container), findsWidgets);
    });

    testWidgets('should be horizontally scrollable', (tester) async {
      await tester.pumpWidget(createTestWidget(
        CookingStyleChips(
          onStyleSelected: (_) {},
        ),
      ));

      final listView = find.byType(ListView);
      expect(listView, findsOneWidget);

      // Verify it's horizontal
      final ListView listViewWidget = tester.widget(listView);
      expect(listViewWidget.scrollDirection, Axis.horizontal);
    });
  });
}
