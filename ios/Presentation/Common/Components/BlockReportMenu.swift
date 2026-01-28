import SwiftUI

/// A reusable menu component for block and report actions.
/// Uses native action sheets (confirmationDialog) for consistent UX.
struct BlockReportMenu: View {
    let targetUserId: String
    let targetUsername: String
    let onBlock: () -> Void
    let onReport: (ReportReason) -> Void

    @State private var showActionSheet = false
    @State private var showBlockConfirmation = false
    @State private var showReportSheet = false

    var body: some View {
        Button {
            showActionSheet = true
        } label: {
            Image(systemName: AppIcon.more)
                .font(.system(size: 18, weight: .medium))
                .foregroundColor(DesignSystem.Colors.secondaryText)
                .frame(width: 44, height: 44)
                .contentShape(Rectangle())
        }
        .confirmationDialog("", isPresented: $showActionSheet, titleVisibility: .hidden) {
            Button("Block @\(targetUsername)", role: .destructive) {
                showBlockConfirmation = true
            }
            Button("Report", role: .destructive) {
                showReportSheet = true
            }
            Button("Cancel", role: .cancel) { }
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

/// A variant that includes a share option alongside block/report options.
struct BlockReportShareMenu: View {
    let targetUserId: String
    let targetUsername: String
    let shareURL: URL
    let onBlock: () -> Void
    let onReport: (ReportReason) -> Void

    @State private var showActionSheet = false
    @State private var showBlockConfirmation = false
    @State private var showReportSheet = false
    @State private var showShareSheet = false

    var body: some View {
        Button {
            showActionSheet = true
        } label: {
            Image(systemName: AppIcon.more)
                .font(.system(size: 18, weight: .medium))
                .foregroundColor(DesignSystem.Colors.secondaryText)
                .frame(width: 44, height: 44)
                .contentShape(Rectangle())
        }
        .confirmationDialog("", isPresented: $showActionSheet, titleVisibility: .hidden) {
            Button("Share") {
                showShareSheet = true
            }
            Button("Block @\(targetUsername)", role: .destructive) {
                showBlockConfirmation = true
            }
            Button("Report", role: .destructive) {
                showReportSheet = true
            }
            Button("Cancel", role: .cancel) { }
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
        .sheet(isPresented: $showShareSheet) {
            ShareSheet(items: [shareURL])
        }
    }
}

/// UIKit share sheet wrapper
struct ShareSheet: UIViewControllerRepresentable {
    let items: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) { }
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
