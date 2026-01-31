import Foundation

struct AppNotification: Codable, Identifiable, Equatable {
    let id: String
    let type: NotificationType
    let title: String
    let body: String
    let isRead: Bool
    let logId: String?
    let recipeId: String?
    let senderUsername: String?
    let senderProfileImageUrl: String?
    let thumbnailUrl: String?
    let createdAt: Date
    let data: [String: AnyCodable]?

    // Computed property for backward compatibility
    var actor: UserSummary? {
        guard let username = senderUsername else { return nil }
        return UserSummary(
            id: "", // Sender ID not provided by backend
            username: username,
            displayName: nil,
            avatarUrl: senderProfileImageUrl,
            level: 0,
            isFollowing: nil
        )
    }

    // Computed property to get targetId based on notification type
    var targetId: String? {
        switch type {
        case .logComment, .commentReply, .logLike, .commentLike:
            return logId
        case .recipeCooked, .recipeSaved:
            return recipeId ?? logId
        case .newFollower:
            return nil // Use actor.id for followers
        case .weeklyDigest, .unknown:
            return nil
        }
    }

    // Computed property to determine target type
    var targetType: NotificationTargetType? {
        if logId != nil { return .log }
        if recipeId != nil { return .recipe }
        return nil
    }

    enum CodingKeys: String, CodingKey {
        case id, visitorId = "publicId"
        case type, title, body, isRead
        case logId = "logPostPublicId"
        case recipeId = "recipePublicId"
        case senderUsername, senderProfileImageUrl
        case thumbnailUrl, createdAt, data
    }

    // Memberwise initializer
    init(
        id: String,
        type: NotificationType,
        title: String,
        body: String,
        isRead: Bool,
        logId: String?,
        recipeId: String?,
        senderUsername: String?,
        senderProfileImageUrl: String?,
        thumbnailUrl: String?,
        createdAt: Date,
        data: [String: AnyCodable]? = nil
    ) {
        self.id = id
        self.type = type
        self.title = title
        self.body = body
        self.isRead = isRead
        self.logId = logId
        self.recipeId = recipeId
        self.senderUsername = senderUsername
        self.senderProfileImageUrl = senderProfileImageUrl
        self.thumbnailUrl = thumbnailUrl
        self.createdAt = createdAt
        self.data = data
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        // Try "id" first, then fall back to "publicId"
        if let id = try container.decodeIfPresent(String.self, forKey: .id) {
            self.id = id
        } else {
            self.id = try container.decode(String.self, forKey: .visitorId)
        }
        self.type = try container.decode(NotificationType.self, forKey: .type)
        self.title = try container.decodeIfPresent(String.self, forKey: .title) ?? ""
        self.body = try container.decodeIfPresent(String.self, forKey: .body) ?? ""
        self.isRead = try container.decodeIfPresent(Bool.self, forKey: .isRead) ?? false
        self.logId = try container.decodeIfPresent(String.self, forKey: .logId)
        self.recipeId = try container.decodeIfPresent(String.self, forKey: .recipeId)
        self.senderUsername = try container.decodeIfPresent(String.self, forKey: .senderUsername)
        self.senderProfileImageUrl = try container.decodeIfPresent(String.self, forKey: .senderProfileImageUrl)
        self.thumbnailUrl = try container.decodeIfPresent(String.self, forKey: .thumbnailUrl)
        self.createdAt = try container.decode(Date.self, forKey: .createdAt)
        self.data = try container.decodeIfPresent([String: AnyCodable].self, forKey: .data)
    }

    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(id, forKey: .id)
        try container.encode(type, forKey: .type)
        try container.encode(title, forKey: .title)
        try container.encode(body, forKey: .body)
        try container.encode(isRead, forKey: .isRead)
        try container.encodeIfPresent(logId, forKey: .logId)
        try container.encodeIfPresent(recipeId, forKey: .recipeId)
        try container.encodeIfPresent(senderUsername, forKey: .senderUsername)
        try container.encodeIfPresent(senderProfileImageUrl, forKey: .senderProfileImageUrl)
        try container.encodeIfPresent(thumbnailUrl, forKey: .thumbnailUrl)
        try container.encode(createdAt, forKey: .createdAt)
        try container.encodeIfPresent(data, forKey: .data)
    }
}

enum NotificationType: String, Codable {
    case newFollower = "NEW_FOLLOWER"
    case logComment = "LOG_COMMENT"
    case commentReply = "COMMENT_REPLY"
    case commentLike = "COMMENT_LIKE"
    case recipeCooked = "RECIPE_COOKED"
    case recipeSaved = "RECIPE_SAVED"
    case logLike = "LOG_LIKE"
    case weeklyDigest = "WEEKLY_DIGEST"
    case unknown = "UNKNOWN"

    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        let rawValue = try container.decode(String.self)
        self = NotificationType(rawValue: rawValue) ?? .unknown
    }

    var iconName: String {
        switch self {
        case .newFollower: return "person.badge.plus"
        case .logComment, .commentReply: return "bubble.left"
        case .commentLike, .logLike: return "heart.fill"
        case .recipeCooked: return "frying.pan"
        case .recipeSaved: return "bookmark.fill"
        case .weeklyDigest: return "chart.bar"
        case .unknown: return "bell"
        }
    }
}

enum NotificationTargetType: String, Codable {
    case recipe = "RECIPE"
    case log = "LOG"
    case user = "USER"
    case comment = "COMMENT"
}

// AnyCodable wrapper for handling arbitrary JSON data
struct AnyCodable: Codable, Equatable {
    let value: Any

    init(_ value: Any) {
        self.value = value
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        if let string = try? container.decode(String.self) {
            value = string
        } else if let int = try? container.decode(Int.self) {
            value = int
        } else if let double = try? container.decode(Double.self) {
            value = double
        } else if let bool = try? container.decode(Bool.self) {
            value = bool
        } else if let array = try? container.decode([AnyCodable].self) {
            value = array.map { $0.value }
        } else if let dict = try? container.decode([String: AnyCodable].self) {
            value = dict.mapValues { $0.value }
        } else {
            value = NSNull()
        }
    }

    func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        switch value {
        case let string as String:
            try container.encode(string)
        case let int as Int:
            try container.encode(int)
        case let double as Double:
            try container.encode(double)
        case let bool as Bool:
            try container.encode(bool)
        case let array as [Any]:
            try container.encode(array.map { AnyCodable($0) })
        case let dict as [String: Any]:
            try container.encode(dict.mapValues { AnyCodable($0) })
        default:
            try container.encodeNil()
        }
    }

    static func == (lhs: AnyCodable, rhs: AnyCodable) -> Bool {
        switch (lhs.value, rhs.value) {
        case let (l as String, r as String): return l == r
        case let (l as Int, r as Int): return l == r
        case let (l as Double, r as Double): return l == r
        case let (l as Bool, r as Bool): return l == r
        case (is NSNull, is NSNull): return true
        default: return false
        }
    }
}
