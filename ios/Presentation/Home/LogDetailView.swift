import SwiftUI

struct LogDetailView: View {
    let logId: String
    @StateObject private var viewModel: LogDetailViewModel
    @EnvironmentObject private var authManager: AuthManager
    @EnvironmentObject private var appState: AppState
    @Environment(\.dismiss) private var dismiss
    @State private var showDeleteConfirmation = false
    @State private var showEditSheet = false
    @State private var showActionSheet = false

    init(logId: String) {
        self.logId = logId
        self._viewModel = StateObject(wrappedValue: LogDetailViewModel(logId: logId))
    }

    var body: some View {
        VStack(spacing: 0) {
            // Custom header
            HStack {
                Button {
                    dismiss()
                } label: {
                    Image(systemName: "chevron.backward")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundColor(DesignSystem.Colors.primary)
                        .frame(width: 44, alignment: .leading)
                }
                .buttonStyle(.borderless)
                .padding(.leading, DesignSystem.Spacing.md)
                
                Spacer()
                
                HStack(spacing: 0) {
                    // Save button (icon only) - requires auth
                    Button {
                        appState.requireAuth {
                            Task { await viewModel.toggleSave() }
                        }
                    } label: {
                        Image(systemName: viewModel.isSaved ? AppIcon.save : AppIcon.saveOutline)
                            .font(.system(size: 18))
                            .foregroundColor(viewModel.isSaved ? DesignSystem.Colors.bookmark : DesignSystem.Colors.secondaryText)
                            .frame(width: 44, height: 44)
                            .contentShape(Rectangle())
                    }

                    // Share button (icon only)
                    ShareLink(item: URL(string: "https://cookstemma.com/logs/\(logId)")!) {
                        Image(systemName: AppIcon.share)
                            .font(.system(size: 18))
                            .foregroundColor(DesignSystem.Colors.text)
                            .frame(width: 44, height: 44)
                            .contentShape(Rectangle())
                    }

                    // More options menu
                    if let log = viewModel.log {
                        if log.author.id == authManager.currentUser?.id {
                            // Own log - show edit/delete action sheet
                            Button {
                                showActionSheet = true
                            } label: {
                                Image(systemName: "ellipsis")
                                    .font(.system(size: 18, weight: .medium))
                                    .foregroundColor(DesignSystem.Colors.text)
                                    .frame(width: 44, height: 44)
                                    .contentShape(Rectangle())
                            }
                            .confirmationDialog("", isPresented: $showActionSheet, titleVisibility: .hidden) {
                                Button(String(localized: "common.edit")) {
                                    showEditSheet = true
                                }
                                Button(String(localized: "common.delete"), role: .destructive) {
                                    showDeleteConfirmation = true
                                }
                                Button(String(localized: "common.cancel"), role: .cancel) { }
                            }
                        } else {
                            // Other user's log - show block/report
                            BlockReportMenu(
                                targetUserId: log.author.id,
                                targetUsername: log.author.username,
                                onBlock: { Task { await viewModel.blockUser() } },
                                onReport: { reason in Task { await viewModel.reportUser(reason: reason) } }
                            )
                        }
                    }
                }
                .padding(.trailing, DesignSystem.Spacing.xs)
            }
            .padding(.vertical, DesignSystem.Spacing.sm)
            .background(DesignSystem.Colors.background)

            ScrollViewReader { proxy in
                ScrollView {
                    switch viewModel.state {
                    case .idle, .loading:
                        LoadingView()
                    case .loaded:
                        if let log = viewModel.log {
                            logContent(log, scrollProxy: proxy)
                        }
                    case .error(let message):
                        ErrorStateView(message: message) { viewModel.loadLog() }
                    }
                }
                .scrollDismissesKeyboard(.immediately)
                .simultaneousGesture(
                    TapGesture().onEnded {
                        // Dismiss keyboard when tapping anywhere
                        UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
                    }
                )
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .enableSwipeBack()
        .onAppear { if case .idle = viewModel.state { viewModel.loadLog() } }
        .alert(String(localized: "menu.deleteCookingLog"), isPresented: $showDeleteConfirmation) {
            Button(String(localized: "common.cancel"), role: .cancel) { }
            Button(String(localized: "common.delete"), role: .destructive) {
                Task {
                    if await viewModel.deleteLog() {
                        dismiss()
                    }
                }
            }
        } message: {
            Text(String(localized: "menu.deleteCookingLogConfirm"))
        }
        .sheet(isPresented: $showEditSheet) {
            if let log = viewModel.log {
                EditLogView(log: log) {
                    viewModel.loadLog()
                }
            }
        }
    }

    @ViewBuilder
    private func logContent(_ log: CookingLogDetail, scrollProxy: ScrollViewProxy) -> some View {
        VStack(alignment: .leading, spacing: DesignSystem.Spacing.md) {
            // Author header
            HStack {
                NavigationLink(destination: ProfileView(userId: log.author.id)) {
                    HStack {
                        AvatarView(url: log.author.avatarUrl, size: DesignSystem.AvatarSize.sm)
                        VStack(alignment: .leading) {
                            Text(log.author.displayNameOrUsername).font(DesignSystem.Typography.subheadline).fontWeight(.medium)
                            Text(log.createdAt.timeAgo()).font(DesignSystem.Typography.caption).foregroundColor(DesignSystem.Colors.secondaryText)
                        }
                    }
                }
                Spacer()
            }
            .padding(.horizontal)

            // Images
            if !log.images.isEmpty {
                TabView {
                    ForEach(log.images, id: \.url) { image in
                        AsyncImage(url: URL(string: image.url)) { img in
                            img.resizable().scaledToFill()
                        } placeholder: {
                            Rectangle().fill(DesignSystem.Colors.secondaryBackground)
                        }
                    }
                }
                .tabViewStyle(.page)
                .frame(height: 300)
            }

            // Rating
            HStack {
                StarRating(rating: log.rating)
                Spacer()
            }
            .padding(.horizontal)

            // Content
            if let content = log.content, !content.isEmpty {
                Text(content)
                    .font(DesignSystem.Typography.body)
                    .padding(.horizontal)
            }

            // Hashtags
            if !log.hashtags.isEmpty {
                FlowLayout(spacing: DesignSystem.Spacing.xs) {
                    ForEach(log.hashtags, id: \.self) { hashtag in
                        NavigationLink(destination: HashtagView(hashtag: hashtag)) {
                            Text("#\(hashtag)")
                                .font(DesignSystem.Typography.subheadline)
                                .foregroundColor(DesignSystem.Colors.primary)
                        }
                    }
                }
                .padding(.horizontal)
            }

            // Linked recipe
            if let recipe = log.recipe {
                NavigationLink(destination: RecipeDetailView(recipeId: recipe.id)) {
                    HStack {
                        AsyncImage(url: URL(string: recipe.coverImageUrl ?? "")) { img in
                            img.resizable().scaledToFill()
                        } placeholder: {
                            Rectangle().fill(DesignSystem.Colors.secondaryBackground)
                        }
                        .frame(width: 50, height: 50)
                        .cornerRadius(DesignSystem.CornerRadius.xs)
                        .clipped()

                        VStack(alignment: .leading, spacing: 4) {
                            Text(recipe.title).font(DesignSystem.Typography.subheadline).fontWeight(.medium)
                            HStack(spacing: DesignSystem.Spacing.sm) {
                                Text("by @\(recipe.userName)").font(DesignSystem.Typography.caption).foregroundColor(DesignSystem.Colors.secondaryText)
                                // Cooking style badge
                                if let style = recipe.cookingStyle, !style.isEmpty {
                                    HStack(spacing: 2) {
                                        Text(style.flagEmoji)
                                        Text(style.cookingStyleName)
                                    }
                                    .font(DesignSystem.Typography.caption)
                                    .foregroundColor(DesignSystem.Colors.secondaryText)
                                }
                            }
                        }
                        Spacer()
                        Image(systemName: "chevron.right").foregroundColor(DesignSystem.Colors.secondaryText)
                    }
                    .padding()
                    .background(DesignSystem.Colors.secondaryBackground)
                    .cornerRadius(DesignSystem.CornerRadius.sm)
                }
                .padding(.horizontal)
            }

            Divider()

            // Comments section
            CommentsSection(logId: logId, scrollProxy: scrollProxy)
        }
        .padding(.bottom, DesignSystem.Spacing.xl)
    }
}

// MARK: - Comments Section
struct CommentsSection: View {
    let logId: String
    let scrollProxy: ScrollViewProxy
    @StateObject private var viewModel: CommentsViewModel
    @EnvironmentObject private var authManager: AuthManager
    @State private var showBlockConfirmation = false
    @State private var userToBlock: UserSummary?
    @State private var replyingToComment: Comment?
    @FocusState private var isCommentInputFocused: Bool

    init(logId: String, scrollProxy: ScrollViewProxy) {
        self.logId = logId
        self.scrollProxy = scrollProxy
        self._viewModel = StateObject(wrappedValue: CommentsViewModel(logId: logId))
    }

    var body: some View {
        VStack(alignment: .leading, spacing: DesignSystem.Spacing.sm) {
            HStack(spacing: DesignSystem.Spacing.xs) {
                Image(systemName: AppIcon.comment)
                    .font(.system(size: DesignSystem.IconSize.md))
                Text("\(viewModel.comments.count)")
                    .font(DesignSystem.Typography.headline)
            }
            .foregroundColor(DesignSystem.Colors.text)
            .padding(.horizontal)

            ForEach(viewModel.comments) { comment in
                CommentRow(
                    comment: comment,
                    isOwnComment: comment.author.id == authManager.currentUser?.id,
                    onLike: { Task { await viewModel.likeComment(comment) } },
                    onReply: {
                        #if DEBUG
                        print("[Comments] Reply tapped - comment.id: \(comment.id), author: \(comment.author.username)")
                        #endif
                        replyingToComment = comment
                        viewModel.newCommentText = "@\(comment.author.username) "
                        isCommentInputFocused = true
                    },
                    onBlock: {
                        userToBlock = comment.author
                        showBlockConfirmation = true
                    },
                    onReport: { reason in
                        Task { await viewModel.reportUser(comment.author.id, reason: reason) }
                    }
                )
            }

            if viewModel.hasMore {
                Button(String(localized: "comments.loadMore")) { viewModel.loadMore() }
                    .font(DesignSystem.Typography.subheadline)
                    .padding(.horizontal)
            }

            // Reply indicator
            if let replyingTo = replyingToComment {
                HStack {
                    Text("comments.replyingTo \(replyingTo.author.username)")
                        .font(DesignSystem.Typography.caption)
                        .foregroundColor(DesignSystem.Colors.secondaryText)
                    Spacer()
                    Button {
                        replyingToComment = nil
                        viewModel.newCommentText = ""
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(DesignSystem.Colors.secondaryText)
                    }
                }
                .padding(.horizontal)
            }

            // Comment input
            HStack {
                TextField(String(localized: "comments.placeholder"), text: $viewModel.newCommentText)
                    .textFieldStyle(.roundedBorder)
                    .focused($isCommentInputFocused)
                Button {
                    let parentId = replyingToComment?.id  // Capture before Task
                    #if DEBUG
                    print("[Comments] Send tapped - parentId: \(parentId ?? "nil"), replyingToComment: \(replyingToComment?.id ?? "nil")")
                    #endif
                    replyingToComment = nil
                    Task {
                        await viewModel.postComment(parentId: parentId)
                    }
                } label: {
                    Image(systemName: "arrow.up.circle.fill")
                        .font(.title2)
                        .foregroundColor(viewModel.newCommentText.isEmpty ? DesignSystem.Colors.secondaryText : DesignSystem.Colors.primary)
                }
                .disabled(viewModel.newCommentText.isEmpty)
            }
            .padding()
            .id("commentInput")
        }
        .onAppear { viewModel.loadComments() }
        .onChange(of: isCommentInputFocused) { _, focused in
            if focused {
                // Wait for keyboard to fully appear before scrolling
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    withAnimation(.easeOut(duration: 0.3)) {
                        scrollProxy.scrollTo("commentInput", anchor: .top)
                    }
                }
            } else {
                // Clear reply state when keyboard dismissed
                if replyingToComment != nil && viewModel.newCommentText.isEmpty {
                    replyingToComment = nil
                }
            }
        }
        .alert(String(localized: "menu.blockUser"), isPresented: $showBlockConfirmation) {
            Button(String(localized: "menu.block"), role: .destructive) {
                guard let user = userToBlock else { return }
                Task { await viewModel.blockUser(user.id) }
            }
            Button(String(localized: "common.cancel"), role: .cancel) { userToBlock = nil }
        } message: {
            if let user = userToBlock {
                Text(String(localized: "menu.blockConfirmMessage \(user.username)"))
            }
        }
    }
}

struct CommentRow: View {
    let comment: Comment
    let isOwnComment: Bool
    let onLike: () -> Void
    let onReply: () -> Void
    let onBlock: () -> Void
    let onReport: (ReportReason) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: DesignSystem.Spacing.xs) {
            HStack(alignment: .top) {
                AvatarView(url: comment.author.avatarUrl, size: DesignSystem.AvatarSize.xs)
                VStack(alignment: .leading, spacing: 2) {
                    HStack {
                        Text(comment.author.displayNameOrUsername).font(DesignSystem.Typography.caption).fontWeight(.medium)
                        Text(comment.createdAt.timeAgo()).font(DesignSystem.Typography.caption).foregroundColor(DesignSystem.Colors.secondaryText)
                    }
                    Text(comment.content).font(DesignSystem.Typography.body)
                    HStack {
                        Button { onLike() } label: {
                            HStack(spacing: 2) {
                                Image(systemName: comment.isLiked ? "heart.fill" : "heart").font(.caption)
                                Text("\(comment.likeCount)").font(DesignSystem.Typography.caption)
                            }
                            .foregroundColor(comment.isLiked ? .red : DesignSystem.Colors.secondaryText)
                        }
                        Button(String(localized: "comments.reply")) { onReply() }
                            .font(DesignSystem.Typography.caption)
                            .foregroundColor(DesignSystem.Colors.secondaryText)
                    }
                }
                Spacer()
                if !isOwnComment {
                    BlockReportMenu(
                        targetUserId: comment.author.id,
                        targetUsername: comment.author.username,
                        onBlock: onBlock,
                        onReport: onReport
                    )
                    .font(.caption)
                }
            }
            .padding(.horizontal)

            // Replies
            if let replies = comment.replies, !replies.isEmpty {
                ForEach(replies) { reply in
                    HStack(alignment: .top) {
                        AvatarView(url: reply.author.avatarUrl, size: DesignSystem.AvatarSize.xs)
                        VStack(alignment: .leading, spacing: 2) {
                            HStack {
                                Text(reply.author.displayNameOrUsername).font(DesignSystem.Typography.caption).fontWeight(.medium)
                                Text(reply.createdAt.timeAgo()).font(DesignSystem.Typography.caption).foregroundColor(DesignSystem.Colors.secondaryText)
                            }
                            Text(reply.content).font(DesignSystem.Typography.body)
                        }
                    }
                    .padding(.leading, 40)
                    .padding(.horizontal)
                }
            }
        }
    }
}

#Preview { NavigationStack { LogDetailView(logId: "preview") } }
