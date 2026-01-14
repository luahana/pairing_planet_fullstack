enum ReportReason {
  spam('SPAM'),
  harassment('HARASSMENT'),
  inappropriateContent('INAPPROPRIATE_CONTENT'),
  impersonation('IMPERSONATION'),
  other('OTHER');

  final String value;
  const ReportReason(this.value);

  static ReportReason fromString(String value) {
    return ReportReason.values.firstWhere(
      (e) => e.value == value,
      orElse: () => ReportReason.other,
    );
  }
}
