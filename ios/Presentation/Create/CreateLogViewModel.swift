import Foundation
import SwiftUI

enum CreateLogState: Equatable {
    case idle, submitting, success(CookingLogDetail), error(String)
}

enum PhotoUploadState: Equatable {
    case idle
    case uploading
    case success(String) // uploadedId
    case failed(String) // error message
}

struct SelectedPhoto: Identifiable, Equatable {
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

@MainActor
final class CreateLogViewModel: ObservableObject {
    @Published private(set) var state: CreateLogState = .idle
    @Published var photos: [SelectedPhoto] = []
    @Published var rating: Int = 0
    @Published var content: String = ""
    @Published var selectedRecipe: RecipeSummary?
    @Published var hashtags: [String] = []
    @Published var isPrivate: Bool = false

    private let logRepository: CookingLogRepositoryProtocol
    private let apiClient: APIClientProtocol
    private let maxPhotos = 3
    let maxContentLength = 2000
    let maxHashtags = 5

    var canSubmit: Bool {
        !photos.isEmpty
        && rating >= 1
        && !content.trimmingCharacters(in: .whitespaces).isEmpty
        && selectedRecipe != nil
        && state != .submitting
        && allPhotosUploaded
    }
    
    var allPhotosUploaded: Bool {
        photos.allSatisfy { $0.isUploaded }
    }
    
    var hasFailedUploads: Bool {
        photos.contains { $0.isFailed }
    }
    var photosRemaining: Int { max(0, maxPhotos - photos.count) }
    var contentRemaining: Int { maxContentLength - content.count }
    var hashtagsRemaining: Int { max(0, maxHashtags - hashtags.count) }

    init(
        recipe: RecipeSummary? = nil,
        logRepository: CookingLogRepositoryProtocol = CookingLogRepository(),
        apiClient: APIClientProtocol = APIClient.shared
    ) {
        self.logRepository = logRepository
        self.apiClient = apiClient
        self.selectedRecipe = recipe
    }

    func addPhoto(_ image: UIImage) {
        guard photos.count < maxPhotos else { return }
        let photoId = UUID().uuidString
        photos.append(SelectedPhoto(id: photoId, image: image, uploadState: .uploading))
        
        // Start upload immediately
        Task {
            await uploadPhoto(id: photoId, image: image)
        }
    }
    
    private func uploadPhoto(id: String, image: UIImage) async {
        guard let index = photos.firstIndex(where: { $0.id == id }) else { return }
        
        guard let jpegData = image.jpegData(compressionQuality: 0.8) else {
            photos[index].uploadState = .failed("Failed to process image")
            return
        }
        
        do {
            let response = try await apiClient.uploadImage(jpegData, type: "LOG_POST")
            // Check if photo still exists (might have been removed)
            if let currentIndex = photos.firstIndex(where: { $0.id == id }) {
                photos[currentIndex].uploadState = .success(response.imagePublicId)
            }
        } catch {
            if let currentIndex = photos.firstIndex(where: { $0.id == id }) {
                photos[currentIndex].uploadState = .failed(error.localizedDescription)
            }
        }
    }
    
    func retryUpload(at index: Int) {
        guard photos.indices.contains(index), photos[index].isFailed else { return }
        let photo = photos[index]
        photos[index].uploadState = .uploading
        
        Task {
            await uploadPhoto(id: photo.id, image: photo.image)
        }
    }

    func removePhoto(at index: Int) {
        guard photos.indices.contains(index) else { return }
        photos.remove(at: index)
    }
    
    func movePhoto(from source: Int, to destination: Int) {
        guard photos.indices.contains(source),
              destination >= 0 && destination < photos.count,
              source != destination else { return }
        let photo = photos.remove(at: source)
        photos.insert(photo, at: destination)
    }

    func selectRecipe(_ recipe: RecipeSummary?) { selectedRecipe = recipe }

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

    func submit() async {
        guard canSubmit else { return }
        state = .submitting

        // Collect already-uploaded image IDs
        let imageIds = photos.compactMap { $0.uploadedId }
        
        let request = CreateLogRequest(
            rating: rating,
            content: content,
            imageIds: imageIds,
            recipeId: selectedRecipe?.id,
            hashtags: hashtags,
            isPrivate: isPrivate
        )
        let result = await logRepository.createLog(request)

        switch result {
        case .success(let log): state = .success(log)
        case .failure(let error): state = .error(error.localizedDescription)
        }
    }

    func reset() {
        state = .idle; photos = []; rating = 0; content = ""; selectedRecipe = nil; hashtags = []; isPrivate = false
    }
}
