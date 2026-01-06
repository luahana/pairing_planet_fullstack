import 'package:pairing_planet2_frontend/domain/entities/analytics/app_event.dart';

abstract class AnalyticsRepository {
  Future<void> trackEvent(AppEvent event);
  Future<void> syncPendingEvents();
}
