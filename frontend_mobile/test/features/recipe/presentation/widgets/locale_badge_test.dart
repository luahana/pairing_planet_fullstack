import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:pairing_planet2_frontend/features/recipe/presentation/widgets/locale_badge.dart';

void main() {
  Widget createTestWidget(Widget child) {
    return ScreenUtilInit(
      designSize: const Size(375, 812),
      minTextAdapt: true,
      builder: (context, _) => MaterialApp(
        home: Scaffold(
          body: Center(
            child: UnconstrainedBox(child: child),
          ),
        ),
      ),
    );
  }

  group('LocaleBadge', () {
    group('with valid country code', () {
      testWidgets('should display widget for KR', (tester) async {
        await tester.pumpWidget(createTestWidget(
          const LocaleBadge(localeCode: 'KR'),
        ));

        expect(find.byType(LocaleBadge), findsOneWidget);
        // The badge should be visible (not SizedBox.shrink)
        expect(find.byType(Container), findsOneWidget);
      });

      testWidgets('should display widget for US', (tester) async {
        await tester.pumpWidget(createTestWidget(
          const LocaleBadge(localeCode: 'US'),
        ));

        expect(find.byType(LocaleBadge), findsOneWidget);
        expect(find.byType(Container), findsOneWidget);
      });

      testWidgets('should display widget for JP', (tester) async {
        await tester.pumpWidget(createTestWidget(
          const LocaleBadge(localeCode: 'JP'),
        ));

        expect(find.byType(LocaleBadge), findsOneWidget);
        expect(find.byType(Container), findsOneWidget);
      });
    });

    group('with legacy locale codes', () {
      testWidgets('should handle ko-KR legacy code', (tester) async {
        await tester.pumpWidget(createTestWidget(
          const LocaleBadge(localeCode: 'ko-KR'),
        ));

        expect(find.byType(LocaleBadge), findsOneWidget);
        // Should normalize and display correctly
        expect(find.byType(Container), findsOneWidget);
      });

      testWidgets('should handle en-US legacy code', (tester) async {
        await tester.pumpWidget(createTestWidget(
          const LocaleBadge(localeCode: 'en-US'),
        ));

        expect(find.byType(LocaleBadge), findsOneWidget);
        expect(find.byType(Container), findsOneWidget);
      });

      testWidgets('should handle ja-JP legacy code', (tester) async {
        await tester.pumpWidget(createTestWidget(
          const LocaleBadge(localeCode: 'ja-JP'),
        ));

        expect(find.byType(LocaleBadge), findsOneWidget);
        expect(find.byType(Container), findsOneWidget);
      });
    });

    group('with "other" code', () {
      testWidgets('should display globe emoji container', (tester) async {
        await tester.pumpWidget(createTestWidget(
          const LocaleBadge(localeCode: 'other'),
        ));

        expect(find.byType(LocaleBadge), findsOneWidget);
        expect(find.byType(Container), findsOneWidget);
        // The globe emoji should be present
        expect(find.text('🌍'), findsOneWidget);
      });
    });

    group('with null/empty code', () {
      testWidgets('should return SizedBox.shrink for null', (tester) async {
        await tester.pumpWidget(createTestWidget(
          const LocaleBadge(localeCode: null),
        ));

        expect(find.byType(LocaleBadge), findsOneWidget);
        expect(find.byType(SizedBox), findsWidgets);
      });

      testWidgets('should return SizedBox.shrink for empty string', (tester) async {
        await tester.pumpWidget(createTestWidget(
          const LocaleBadge(localeCode: ''),
        ));

        expect(find.byType(LocaleBadge), findsOneWidget);
        expect(find.byType(SizedBox), findsWidgets);
      });
    });

    group('showLabel parameter', () {
      testWidgets('should show label when showLabel is true', (tester) async {
        await tester.pumpWidget(createTestWidget(
          const LocaleBadge(localeCode: 'KR', showLabel: true),
        ));

        // Label should be visible (multiple Text widgets)
        expect(find.byType(Text), findsWidgets);
      });

      testWidgets('should hide label when showLabel is false', (tester) async {
        await tester.pumpWidget(createTestWidget(
          const LocaleBadge(localeCode: 'KR', showLabel: false),
        ));

        // Only flag emoji text should be present
        expect(find.byType(Text), findsOneWidget);
      });
    });
  });

  group('LocaleBadgeLarge', () {
    group('with valid country code', () {
      testWidgets('should display widget for KR', (tester) async {
        await tester.pumpWidget(createTestWidget(
          const LocaleBadgeLarge(localeCode: 'KR'),
        ));

        expect(find.byType(LocaleBadgeLarge), findsOneWidget);
        expect(find.byType(Container), findsOneWidget);
      });
    });

    group('with "other" code', () {
      testWidgets('should display globe emoji', (tester) async {
        await tester.pumpWidget(createTestWidget(
          const LocaleBadgeLarge(localeCode: 'other'),
        ));

        expect(find.byType(LocaleBadgeLarge), findsOneWidget);
        expect(find.text('🌍'), findsOneWidget);
      });
    });

    group('with null/empty code', () {
      testWidgets('should return SizedBox.shrink for null', (tester) async {
        await tester.pumpWidget(createTestWidget(
          const LocaleBadgeLarge(localeCode: null),
        ));

        expect(find.byType(LocaleBadgeLarge), findsOneWidget);
        expect(find.byType(SizedBox), findsWidgets);
      });
    });
  });

  group('LocaleBadgeStyled', () {
    group('with valid country code', () {
      testWidgets('should display widget for KR', (tester) async {
        await tester.pumpWidget(createTestWidget(
          const LocaleBadgeStyled(localeCode: 'KR'),
        ));

        expect(find.byType(LocaleBadgeStyled), findsOneWidget);
        expect(find.byType(Container), findsOneWidget);
      });
    });

    group('with "other" code', () {
      testWidgets('should display globe emoji', (tester) async {
        await tester.pumpWidget(createTestWidget(
          const LocaleBadgeStyled(localeCode: 'other'),
        ));

        expect(find.byType(LocaleBadgeStyled), findsOneWidget);
        expect(find.text('🌍'), findsOneWidget);
      });
    });

    group('with null/empty code', () {
      testWidgets('should return SizedBox.shrink for null', (tester) async {
        await tester.pumpWidget(createTestWidget(
          const LocaleBadgeStyled(localeCode: null),
        ));

        expect(find.byType(LocaleBadgeStyled), findsOneWidget);
        expect(find.byType(SizedBox), findsWidgets);
      });
    });
  });
}
