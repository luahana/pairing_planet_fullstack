import SwiftUI

/// A reusable menu component for block and report actions.
/// Provides consistent UX across the app with centered alert for block confirmation
/// and confirmation dialog for report reason selection.
struct BlockReportMenu: View {
    let targetUserId: String
    let targetUsername: String
    let onBlock: () -> Void
    let onReport: (ReportReason) -> Void

    @State private var showBlockConfirmation = false
    @State private var showReportSheet = false

    var body: some View {
        Menu {
            Button(role: .destructive) {
                showBlockConfirmation = true
            } label: {
                Label("Block @\(targetUsername)", systemImage: AppIcon.block)
            }

            Button(role: .destructive) {
                showReportSheet = true
            } label: {
                Label("Report", systemImage: AppIcon.report)
            }
        } label: {
            Image(systemName: AppIcon.more)
                .foregroundColor(DesignSystem.Colors.secondaryText)
        }
        .alert("Block @\(targetUsername)?", isPresented: $showBlockConfirmation) {
            Button("Cancel", role: .cancel) { }
            Button("Block", role: .destructive) { onBlock() }
        } message: {
            Text("You won't see their content anymore. They won't be notified.")
        }
        .confirmationDialog("Report", isPresented: $showReportSheet, titleVisibility: .visible) {
            ForEach(ReportReason.allCases, id: \.self) { reason in
                Button(reason.displayText) { onReport(reason) }
            }
            Button("Cancel", role: .cancel) { }
        }
    }
}

/// A variant that includes a share link alongside block/report options.
struct BlockReportShareMenu: View {
    let targetUserId: String
    let targetUsername: String
    let shareURL: URL
    let onBlock: () -> Void
    let onReport: (ReportReason) -> Void

    @State private var showBlockConfirmation = false
    @State private var showReportSheet = false

    var body: some View {
        Menu {
            ShareLink(item: shareURL) {
                Label("Share", systemImage: AppIcon.share)
            }

            Button(role: .destructive) {
                showBlockConfirmation = true
            } label: {
                Label("Block @\(targetUsername)", systemImage: AppIcon.block)
            }

            Button(role: .destructive) {
                showReportSheet = true
            } label: {
                Label("Report", systemImage: AppIcon.report)
            }
        } label: {
            Image(systemName: AppIcon.more)
                .foregroundColor(DesignSystem.Colors.secondaryText)
        }
        .alert("Block @\(targetUsername)?", isPresented: $showBlockConfirmation) {
            Button("Cancel", role: .cancel) { }
            Button("Block", role: .destructive) { onBlock() }
        } message: {
            Text("You won't see their content anymore. They won't be notified.")
        }
        .confirmationDialog("Report", isPresented: $showReportSheet, titleVisibility: .visible) {
            ForEach(ReportReason.allCases, id: \.self) { reason in
                Button(reason.displayText) { onReport(reason) }
            }
            Button("Cancel", role: .cancel) { }
        }
    }
}

#Preview {
    VStack(spacing: 40) {
        HStack {
            Text("Basic Menu:")
            Spacer()
            BlockReportMenu(
                targetUserId: "user123",
                targetUsername: "testuser",
                onBlock: { },
                onReport: { _ in }
            )
        }

        HStack {
            Text("With Share:")
            Spacer()
            BlockReportShareMenu(
                targetUserId: "user123",
                targetUsername: "testuser",
                shareURL: URL(string: "https://cookstemma.com/users/user123")!,
                onBlock: { },
                onReport: { _ in }
            )
        }
    }
    .padding()
}
