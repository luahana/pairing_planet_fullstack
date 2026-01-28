import SwiftUI
import PhotosUI

struct EditLogView: View {
    let log: CookingLogDetail
    let onSave: () -> Void

    @Environment(\.dismiss) private var dismiss
    @StateObject private var viewModel: EditLogViewModel
    @State private var hashtagInput = ""
    @State private var showingPhotoSourceSheet = false
    @State private var showingCamera = false
    @State private var showingGallery = false

    private let maxPhotos = 3

    init(log: CookingLogDetail, onSave: @escaping () -> Void) {
        self.log = log
        self.onSave = onSave
        self._viewModel = StateObject(wrappedValue: EditLogViewModel(log: log))
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Custom header
                HStack {
                    Button { dismiss() } label: {
                        Image(systemName: AppIcon.close)
                            .font(.system(size: DesignSystem.IconSize.md, weight: .medium))
                            .foregroundColor(DesignSystem.Colors.text)
                            .frame(width: 44, alignment: .leading)
                    }
                    .buttonStyle(.borderless)
                    .padding(.leading, DesignSystem.Spacing.md)

                    Spacer()

                    Text("Edit Cooking Log")
                        .font(DesignSystem.Typography.headline)
                        .foregroundColor(DesignSystem.Colors.text)

                    Spacer()

                    Button {
                        Task {
                            if await viewModel.save() {
                                onSave()
                                dismiss()
                            }
                        }
                    } label: {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 24))
                            .foregroundColor(viewModel.canSave ? DesignSystem.Colors.primary : DesignSystem.Colors.tertiaryText)
                            .frame(width: 44, alignment: .trailing)
                    }
                    .buttonStyle(.borderless)
                    .disabled(!viewModel.canSave)
                    .padding(.trailing, DesignSystem.Spacing.md)
                }
                .padding(.top, DesignSystem.Spacing.lg)
                .padding(.bottom, DesignSystem.Spacing.sm)
                .background(DesignSystem.Colors.background)

                ScrollView {
                    VStack(spacing: 0) {
                        // Photo Section
                        photoSection
                            .padding(.horizontal, DesignSystem.Spacing.md)
                            .padding(.vertical, DesignSystem.Spacing.lg)

                        Divider()

                        // Rating Section
                        ratingSection
                            .padding(.horizontal, DesignSystem.Spacing.md)
                            .padding(.vertical, DesignSystem.Spacing.lg)

                        Divider()

                        // Description Section
                        descriptionSection
                            .padding(.horizontal, DesignSystem.Spacing.md)
                            .padding(.vertical, DesignSystem.Spacing.md)

                        Divider()

                        // Hashtags Section
                        hashtagsSection
                            .padding(.horizontal, DesignSystem.Spacing.md)
                            .padding(.vertical, DesignSystem.Spacing.md)

                        Divider()

                        // Privacy Toggle
                        privacySection
                            .padding(.horizontal, DesignSystem.Spacing.md)
                            .padding(.vertical, DesignSystem.Spacing.md)
                    }
                }
                .background(DesignSystem.Colors.background)
            }
            .toolbar(.hidden, for: .navigationBar)
        }
        .alert("Error", isPresented: .constant(viewModel.errorMessage != nil)) {
            Button("OK") { viewModel.errorMessage = nil }
        } message: {
            if let error = viewModel.errorMessage {
                Text(error)
            }
        }
        .alert("Add Photo", isPresented: $showingPhotoSourceSheet) {
            Button("Cancel", role: .cancel) { }
            Button("Take Photo") {
                showingCamera = true
            }
            Button("Choose from Library") {
                showingGallery = true
            }
        }
        .sheet(isPresented: $showingCamera) {
            CameraPicker { image in
                if let image = image {
                    viewModel.addNewPhoto(image)
                }
            }
        }
        .sheet(isPresented: $showingGallery) {
            PhotosPickerSheet(maxSelection: maxPhotos - viewModel.totalPhotoCount) { images in
                for image in images {
                    viewModel.addNewPhoto(image)
                }
            }
        }
    }

    // MARK: - Photo Section
    private var photoSection: some View {
        HStack(spacing: DesignSystem.Spacing.sm) {
            ForEach(0..<maxPhotos, id: \.self) { index in
                photoSlot(at: index)
            }
        }
    }

    @ViewBuilder
    private func photoSlot(at index: Int) -> some View {
        let slotSize: CGFloat = (UIScreen.main.bounds.width - DesignSystem.Spacing.md * 2 - DesignSystem.Spacing.sm * 2) / 3

        if index < viewModel.existingPhotos.count {
            // Existing photo from server (already uploaded, show success)
            let existingPhoto = viewModel.existingPhotos[index]
            ZStack {
                AsyncImage(url: URL(string: existingPhoto.url)) { img in
                    img.resizable().scaledToFill()
                } placeholder: {
                    Rectangle().fill(DesignSystem.Colors.tertiaryBackground)
                }
                .frame(width: slotSize, height: slotSize)
                .cornerRadius(DesignSystem.CornerRadius.sm)
                .clipped()
                
                // Success indicator for existing photos
                VStack {
                    Spacer()
                    HStack {
                        Spacer()
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 20))
                            .foregroundColor(.green)
                            .background(Circle().fill(.white).padding(2))
                            .offset(x: -6, y: -6)
                    }
                }

                // Remove button (top right)
                VStack {
                    HStack {
                        Spacer()
                        Button { viewModel.removeExistingPhoto(at: index) } label: {
                            Image(systemName: AppIcon.close)
                                .font(.system(size: 10, weight: .bold))
                                .foregroundColor(.white)
                                .frame(width: 20, height: 20)
                                .background(Color.black.opacity(0.6))
                                .clipShape(Circle())
                        }
                        .offset(x: -4, y: 4)
                    }
                    Spacer()
                }
            }
            .frame(width: slotSize, height: slotSize)
            .draggable("existing:\(existingPhoto.id)") {
                // Drag preview
                AsyncImage(url: URL(string: existingPhoto.url)) { img in
                    img.resizable().scaledToFill()
                } placeholder: {
                    Rectangle().fill(DesignSystem.Colors.tertiaryBackground)
                }
                .frame(width: slotSize * 0.8, height: slotSize * 0.8)
                .cornerRadius(DesignSystem.CornerRadius.sm)
                .clipped()
                .opacity(0.8)
            }
            .dropDestination(for: String.self) { items, _ in
                guard let draggedId = items.first else { return false }
                return handlePhotoDrop(draggedId: draggedId, targetIndex: index)
            }
        } else if index < viewModel.totalPhotoCount {
            // New photo added by user
            let newIndex = index - viewModel.existingPhotos.count
            let photo = viewModel.newPhotos[newIndex]
            ZStack {
                Image(uiImage: photo.image)
                    .resizable()
                    .scaledToFill()
                    .frame(width: slotSize, height: slotSize)
                    .cornerRadius(DesignSystem.CornerRadius.sm)
                    .clipped()
                
                // Upload status overlay
                newPhotoUploadOverlay(for: photo, at: newIndex, size: slotSize)

                // Remove button (top right)
                VStack {
                    HStack {
                        Spacer()
                        Button { viewModel.removeNewPhoto(at: newIndex) } label: {
                            Image(systemName: AppIcon.close)
                                .font(.system(size: 10, weight: .bold))
                                .foregroundColor(.white)
                                .frame(width: 20, height: 20)
                                .background(Color.black.opacity(0.6))
                                .clipShape(Circle())
                        }
                        .offset(x: -4, y: 4)
                    }
                    Spacer()
                }
            }
            .frame(width: slotSize, height: slotSize)
            .draggable("new:\(photo.id)") {
                // Drag preview
                Image(uiImage: photo.image)
                    .resizable()
                    .scaledToFill()
                    .frame(width: slotSize * 0.8, height: slotSize * 0.8)
                    .cornerRadius(DesignSystem.CornerRadius.sm)
                    .clipped()
                    .opacity(0.8)
            }
            .dropDestination(for: String.self) { items, _ in
                guard let draggedId = items.first else { return false }
                return handlePhotoDrop(draggedId: draggedId, targetIndex: index)
            }
        } else {
            // Empty slot
            Button {
                showingPhotoSourceSheet = true
            } label: {
                VStack(spacing: DesignSystem.Spacing.xs) {
                    Image(systemName: "camera.fill")
                        .font(.system(size: DesignSystem.IconSize.lg))
                        .foregroundColor(DesignSystem.Colors.tertiaryText)
                    Text("tap to add")
                        .font(DesignSystem.Typography.caption2)
                        .foregroundColor(DesignSystem.Colors.tertiaryText)
                }
                .frame(width: slotSize, height: slotSize)
                .background(DesignSystem.Colors.secondaryBackground)
                .cornerRadius(DesignSystem.CornerRadius.sm)
            }
        }
    }
    
    private func handlePhotoDrop(draggedId: String, targetIndex: Int) -> Bool {
        // Parse the dragged ID to find source index
        let existingCount = viewModel.existingPhotos.count
        var sourceIndex: Int?
        
        if draggedId.hasPrefix("existing:") {
            let id = String(draggedId.dropFirst("existing:".count))
            if let idx = viewModel.existingPhotos.firstIndex(where: { $0.id == id }) {
                sourceIndex = idx
            }
        } else if draggedId.hasPrefix("new:") {
            let id = String(draggedId.dropFirst("new:".count))
            if let idx = viewModel.newPhotos.firstIndex(where: { $0.id == id }) {
                sourceIndex = existingCount + idx
            }
        }
        
        guard let source = sourceIndex, source != targetIndex else { return false }
        
        withAnimation(.easeInOut(duration: 0.2)) {
            viewModel.movePhoto(from: source, to: targetIndex)
        }
        return true
    }
    
    @ViewBuilder
    private func newPhotoUploadOverlay(for photo: NewPhoto, at index: Int, size: CGFloat) -> some View {
        switch photo.uploadState {
        case .idle:
            EmptyView()
        case .uploading:
            // Loading overlay
            ZStack {
                Color.black.opacity(0.4)
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    .scaleEffect(1.2)
            }
            .frame(width: size, height: size)
            .cornerRadius(DesignSystem.CornerRadius.sm)
        case .success:
            // Success indicator (bottom right)
            VStack {
                Spacer()
                HStack {
                    Spacer()
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 20))
                        .foregroundColor(.green)
                        .background(Circle().fill(.white).padding(2))
                        .offset(x: -6, y: -6)
                }
            }
        case .failed:
            // Failed overlay with retry
            ZStack {
                Color.black.opacity(0.5)
                VStack(spacing: DesignSystem.Spacing.xs) {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .font(.system(size: 24))
                        .foregroundColor(DesignSystem.Colors.error)
                    Button {
                        viewModel.retryUpload(at: index)
                    } label: {
                        Text("Retry")
                            .font(DesignSystem.Typography.caption)
                            .foregroundColor(.white)
                            .padding(.horizontal, DesignSystem.Spacing.sm)
                            .padding(.vertical, DesignSystem.Spacing.xxs)
                            .background(DesignSystem.Colors.error)
                            .cornerRadius(DesignSystem.CornerRadius.xs)
                    }
                }
            }
            .frame(width: size, height: size)
            .cornerRadius(DesignSystem.CornerRadius.sm)
        }
    }

    // MARK: - Rating Section
    private var ratingSection: some View {
        HStack {
            Spacer()
            InteractiveStarRating(rating: $viewModel.rating, size: DesignSystem.IconSize.xl)
            Spacer()
        }
    }

    // MARK: - Description Section
    private var descriptionSection: some View {
        VStack(alignment: .leading, spacing: DesignSystem.Spacing.xs) {
            TextEditor(text: $viewModel.content)
                .frame(minHeight: 100)
                .scrollContentBackground(.hidden)
                .onChange(of: viewModel.content) { _, newValue in
                    if newValue.count > viewModel.maxContentLength {
                        viewModel.content = String(newValue.prefix(viewModel.maxContentLength))
                    }
                }
                .overlay(alignment: .topLeading) {
                    if viewModel.content.isEmpty {
                        Text("Write about your cooking...")
                            .font(DesignSystem.Typography.body)
                            .foregroundColor(DesignSystem.Colors.tertiaryText)
                            .padding(.top, 8)
                            .padding(.leading, 4)
                            .allowsHitTesting(false)
                    }
                }

            HStack {
                Spacer()
                Text("\(viewModel.content.count)/\(viewModel.maxContentLength)")
                    .font(DesignSystem.Typography.caption)
                    .foregroundColor(viewModel.contentRemaining < 100 ? DesignSystem.Colors.error : DesignSystem.Colors.tertiaryText)
            }
        }
    }

    // MARK: - Hashtags Section
    private var hashtagsSection: some View {
        VStack(alignment: .leading, spacing: DesignSystem.Spacing.sm) {
            // Input row
            HStack(spacing: DesignSystem.Spacing.sm) {
                Image(systemName: "number")
                    .foregroundColor(DesignSystem.Colors.primary)

                TextField("Add hashtag", text: $hashtagInput)
                    .textFieldStyle(.plain)
                    .autocapitalization(.none)
                    .disableAutocorrection(true)
                    .submitLabel(.done)
                    .onSubmit {
                        addCurrentHashtag()
                    }

                if viewModel.hashtags.count < viewModel.maxHashtags {
                    Button {
                        addCurrentHashtag()
                    } label: {
                        Image(systemName: "plus.circle.fill")
                            .foregroundColor(hashtagInput.isEmpty ? DesignSystem.Colors.tertiaryText : DesignSystem.Colors.primary)
                    }
                    .disabled(hashtagInput.isEmpty)
                }

                Text("\(viewModel.hashtags.count)/\(viewModel.maxHashtags)")
                    .font(DesignSystem.Typography.caption)
                    .foregroundColor(DesignSystem.Colors.tertiaryText)
            }

            // Hashtag chips
            if !viewModel.hashtags.isEmpty {
                FlowLayout(spacing: DesignSystem.Spacing.xs) {
                    ForEach(Array(viewModel.hashtags.enumerated()), id: \.element) { index, tag in
                        HStack(spacing: DesignSystem.Spacing.xxs) {
                            Text("#\(tag)")
                                .font(DesignSystem.Typography.subheadline)
                                .foregroundColor(DesignSystem.Colors.primary)

                            Button {
                                viewModel.removeHashtag(at: index)
                            } label: {
                                Image(systemName: "xmark")
                                    .font(.system(size: 10, weight: .bold))
                                    .foregroundColor(DesignSystem.Colors.secondaryText)
                            }
                        }
                        .padding(.horizontal, DesignSystem.Spacing.sm)
                        .padding(.vertical, DesignSystem.Spacing.xs)
                        .background(DesignSystem.Colors.primary.opacity(0.1))
                        .cornerRadius(DesignSystem.CornerRadius.full)
                    }
                }
            }
        }
    }

    private func addCurrentHashtag() {
        viewModel.addHashtag(hashtagInput)
        hashtagInput = ""
    }

    // MARK: - Privacy Section
    private var privacySection: some View {
        HStack {
            Image(systemName: viewModel.isPrivate ? "lock.fill" : "lock.open")
                .foregroundColor(DesignSystem.Colors.primary)
            Text(viewModel.isPrivate ? "Private" : "Public")
                .font(DesignSystem.Typography.body)
                .foregroundColor(DesignSystem.Colors.text)
            Spacer()
            Toggle("", isOn: $viewModel.isPrivate)
                .labelsHidden()
        }
    }
}

// MARK: - Existing Photo Info
struct ExistingPhoto: Identifiable, Equatable {
    let id: String
    let url: String
}

// MARK: - New Photo
struct NewPhoto: Identifiable, Equatable {
    let id: String
    let image: UIImage
    var uploadState: PhotoUploadState = .idle
    
    var isUploading: Bool {
        if case .uploading = uploadState { return true }
        return false
    }
    
    var isUploaded: Bool {
        if case .success = uploadState { return true }
        return false
    }
    
    var isFailed: Bool {
        if case .failed = uploadState { return true }
        return false
    }
    
    var uploadedId: String? {
        if case .success(let id) = uploadState { return id }
        return nil
    }
}

// MARK: - Edit Log ViewModel
@MainActor
final class EditLogViewModel: ObservableObject {
    @Published var rating: Int
    @Published var content: String
    @Published var hashtags: [String]
    @Published var isPrivate: Bool
    @Published var existingPhotos: [ExistingPhoto]
    @Published var newPhotos: [NewPhoto] = []
    @Published var isSaving = false
    @Published var errorMessage: String?

    private let logId: String
    private let logRepository: CookingLogRepositoryProtocol
    private let apiClient: APIClientProtocol
    private let maxPhotos = 3

    let maxContentLength = 2000
    let maxHashtags = 5

    var totalPhotoCount: Int { existingPhotos.count + newPhotos.count }

    var canSave: Bool {
        rating >= 1 && totalPhotoCount > 0 && !content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && !isSaving && allNewPhotosUploaded
    }
    
    var allNewPhotosUploaded: Bool {
        newPhotos.allSatisfy { $0.isUploaded }
    }
    
    var hasFailedUploads: Bool {
        newPhotos.contains { $0.isFailed }
    }

    var contentRemaining: Int { maxContentLength - content.count }

    init(
        log: CookingLogDetail,
        logRepository: CookingLogRepositoryProtocol = CookingLogRepository(),
        apiClient: APIClientProtocol = APIClient.shared
    ) {
        self.logId = log.id
        self.rating = log.rating
        self.content = log.content ?? ""
        self.hashtags = log.hashtags
        self.isPrivate = log.isPrivate
        self.logRepository = logRepository
        self.apiClient = apiClient

        // Convert existing images
        self.existingPhotos = log.images.map { ExistingPhoto(id: $0.id, url: $0.url) }
    }

    func addNewPhoto(_ image: UIImage) {
        guard totalPhotoCount < maxPhotos else { return }
        let photoId = UUID().uuidString
        newPhotos.append(NewPhoto(id: photoId, image: image, uploadState: .uploading))
        
        // Start upload immediately
        Task {
            await uploadPhoto(id: photoId, image: image)
        }
    }
    
    private func uploadPhoto(id: String, image: UIImage) async {
        guard let index = newPhotos.firstIndex(where: { $0.id == id }) else { return }
        
        guard let jpegData = image.jpegData(compressionQuality: 0.8) else {
            newPhotos[index].uploadState = .failed("Failed to process image")
            return
        }
        
        do {
            let response = try await apiClient.uploadImage(jpegData, type: "LOG_POST")
            // Check if photo still exists (might have been removed)
            if let currentIndex = newPhotos.firstIndex(where: { $0.id == id }) {
                newPhotos[currentIndex].uploadState = .success(response.imagePublicId)
            }
        } catch {
            if let currentIndex = newPhotos.firstIndex(where: { $0.id == id }) {
                newPhotos[currentIndex].uploadState = .failed(error.localizedDescription)
            }
        }
    }
    
    func retryUpload(at index: Int) {
        guard newPhotos.indices.contains(index), newPhotos[index].isFailed else { return }
        let photo = newPhotos[index]
        newPhotos[index].uploadState = .uploading
        
        Task {
            await uploadPhoto(id: photo.id, image: photo.image)
        }
    }

    func removeExistingPhoto(at index: Int) {
        guard existingPhotos.indices.contains(index) else { return }
        existingPhotos.remove(at: index)
    }

    func removeNewPhoto(at index: Int) {
        guard newPhotos.indices.contains(index) else { return }
        newPhotos.remove(at: index)
    }
    
    /// Move photo from one position to another in the combined photo list
    func movePhoto(from source: Int, to destination: Int) {
        guard source != destination,
              source >= 0 && source < totalPhotoCount,
              destination >= 0 && destination < totalPhotoCount else { return }
        
        let existingCount = existingPhotos.count
        
        // Determine source type and index
        let sourceIsExisting = source < existingCount
        let sourceLocalIndex = sourceIsExisting ? source : source - existingCount
        
        // Determine destination type and index
        let destIsExisting = destination < existingCount
        let destLocalIndex = destIsExisting ? destination : destination - existingCount
        
        if sourceIsExisting && destIsExisting {
            // Both existing: simple swap within existingPhotos
            let photo = existingPhotos.remove(at: sourceLocalIndex)
            existingPhotos.insert(photo, at: destLocalIndex)
        } else if !sourceIsExisting && !destIsExisting {
            // Both new: simple swap within newPhotos
            let photo = newPhotos.remove(at: sourceLocalIndex)
            newPhotos.insert(photo, at: destLocalIndex)
        } else {
            // Cross-array move: we can't easily move between arrays since types differ
            // For simplicity, only allow reordering within same type
            // If needed, we could convert types but existing photos have URLs, new photos have UIImages
        }
    }

    func addHashtag(_ tag: String) {
        let cleaned = tag.trimmingCharacters(in: .whitespaces)
            .lowercased()
            .replacingOccurrences(of: "#", with: "")
        guard !cleaned.isEmpty,
              hashtags.count < maxHashtags,
              !hashtags.contains(cleaned) else { return }
        hashtags.append(cleaned)
    }

    func removeHashtag(at index: Int) {
        guard hashtags.indices.contains(index) else { return }
        hashtags.remove(at: index)
    }

    func save() async -> Bool {
        isSaving = true

        // Collect existing photo IDs and already-uploaded new photo IDs
        var allImageIds = existingPhotos.map { $0.id }
        allImageIds.append(contentsOf: newPhotos.compactMap { $0.uploadedId })

        let request = UpdateLogRequest(
            rating: rating,
            content: content,
            imageIds: allImageIds,
            recipeId: nil,
            hashtags: hashtags,
            isPrivate: isPrivate
        )

        let result = await logRepository.updateLog(id: logId, request)
        isSaving = false

        switch result {
        case .success:
            return true
        case .failure(let error):
            errorMessage = error.localizedDescription
            return false
        }
    }
}
