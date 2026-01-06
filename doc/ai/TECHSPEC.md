# Technical Specification - Pairing Planet Frontend

## Project Overview

**Pairing Planet** is a mobile-only Flutter application that treats recipes as an **evolving knowledge graph** rather than static content. The app documents how recipes survive, evolve, and branch through user experimentation.

### Core Philosophy
> "This app is not a place to consume recipes—it's a system that records the process of recipes staying alive."

### Key Principles
1. **Recipes as Living Knowledge**: Recipes evolve through variations (original → modified versions)
2. **Logs Keep Recipes Alive**: Cooking logs (photos, reviews, ratings) give recipes vitality
3. **Progressive Authentication**: Login is NOT forced—only prompted when users create value
4. **Mobile-Only**: No web version; designed exclusively for mobile experience

## Content Model

### Core Entities

#### Recipe
Represents a dish—either an **original recipe** or a **variation** of an existing recipe.

**Structure**:
- `publicId`: Unique identifier (UUID)
- `title`: Recipe name
- `description`: Recipe overview
- `ingredients[]`: List of ingredients with amounts and types (MAIN, SECONDARY, SEASONING)
- `steps[]`: Cooking instructions with optional step images
- `imagePublicIds[]`: Recipe thumbnail photos
- `parentPublicId`: Reference to parent recipe (if this is a variation; null for originals)
- `rootPublicId`: Reference to original recipe (root of variation tree; null for originals)
- `changeCategory`: Type of modification made (e.g., "ingredient swap", "technique change", "seasoning")

**Variation Tree**:
```
Original Recipe (root)
├── Variation A (parentId → Original, rootId → Original)
│   └── Variation A1 (parentId → A, rootId → Original)
└── Variation B (parentId → Original, rootId → Original)
```

**Business Rules**:
- Original recipes have `parentPublicId = null` and `rootPublicId = null`
- Variations must reference both parent and root for tree navigation
- "Inspired by" relationship is explicitly tracked
- ✅ **Soft delete only**—recipes are never hard-deleted (preservation philosophy)

**Graph Traversal Queries**:
```dart
// Get all variations of a recipe (entire tree)
Future<List<Recipe>> getAllVariations(String recipeId) async {
  return await db.recipes
    .where('rootPublicId', isEqualTo: recipeId)
    .where('deletedAt', isNull: true)
    .get();
}

// Get direct children only
Future<List<Recipe>> getDirectChildren(String recipeId) async {
  return await db.recipes
    .where('parentPublicId', isEqualTo: recipeId)
    .where('deletedAt', isNull: true)
    .get();
}

// Get ancestry path (traverse up to root)
Future<List<Recipe>> getAncestryPath(String recipeId) async {
  List<Recipe> path = [];
  String? currentId = recipeId;

  while (currentId != null) {
    final recipe = await db.recipes.doc(currentId).get();
    path.insert(0, recipe);
    currentId = recipe.parentPublicId;
  }

  return path;
}
```

#### Log Post
A cooking attempt record: photos, review, rating, and commentary.

**Structure**:
- `publicId`: Unique identifier (UUID)
- `recipePublicId`: Reference to the recipe that was cooked (**foreign key**, not embedded)
- `title`: Optional log title
- `content`: Cooking notes, review, commentary
- `rating`: 1-5 stars (integer)
- `imagePublicIds[]`: Photos of the cooking attempt
- `creatorName`: User who created the log
- `createdAt`: Timestamp

**Business Rules**:
- Logs are **separate entities** from recipes (NOT embedded documents)
- Multiple logs can reference the same recipe
- Logs give recipes "vitality"—recipes with many logs rank higher in feed
- ✅ **Soft delete only**—logs are preserved even if parent recipe is deleted

#### Image
Media files associated with recipes or logs, served via CDN.

**Structure**:
- `imagePublicId`: UUID identifier
- `imageUrl`: CDN URL for accessing the image
- `originalFilename`: Original file name
- `type`: "THUMBNAIL" (recipe photos) or "LOG_POST" (log photos)
- `uploadedAt`: Timestamp

**Upload Flow (Current)**:
1. Client selects image (camera/gallery)
2. Client uploads to backend API
3. Backend stores in cloud storage and returns `imagePublicId` + `imageUrl`
4. Client stores publicId reference

**Upload Flow (Planned - Presigned URLs)**:
1. Client requests presigned URL from backend
2. Backend generates presigned URL, returns to client
3. Client uploads directly to CDN
4. Client notifies backend of completion
5. Backend stores metadata

#### Hashtag (Planned Feature)
Weak classification system for discovery and context.

**Structure**:
- `tag`: Hashtag text (e.g., "vegetarian", "quick-meal", "spicy")
- `usageCount`: Number of recipes using this tag
- `trending`: Boolean flag for trending tags

**Purpose**:
- Enable exploration and search
- Provide contextual connections between recipes
- NOT a strict categorization—recipes can have 0-5 hashtags
- User-generated, organic discovery

### Relationships

```
Recipe (Original)
  ↓ (1:N)
Recipe (Variations) ← parentPublicId, rootPublicId
  ↓ (1:N)
LogPost ← recipePublicId
  ↓ (1:N)
Image ← imagePublicIds[]
```

**Key Design Decision**: Recipe and Log are **separate entities** to:
- Avoid write hotspots (logs are more frequently created than recipes)
- Enable independent scaling (read-heavy recipe queries vs write-heavy log creation)
- Preserve content integrity (orphaned logs kept if recipe deleted)

## Authentication & UX Philosophy

### Progressive Authentication Strategy

**Core Principle**: ❌ **Never force login at app launch**

Users can browse, view recipes, and explore the app anonymously. Login is prompted **ONLY when users create value**:

**Login Trigger Points**:
- 💾 **Saving/bookmarking a recipe** (planned feature)
- 📝 **Creating a log post** (review/photo of cooking attempt)
- 🔀 **Creating a recipe variation** (modified version of existing recipe)
- ⭐ **Rating or reviewing** (any user-generated content)

**Implementation**:
- Use **bottom sheet** for login UI (non-intrusive, dismissable)
- Firebase Authentication with Google Sign-In
- Token stored in secure local storage (NOT HTTP-only cookies)
- Auto-refresh with Dio interceptors

**User Flow Example**:
```
User opens app → Browse home feed (NO login required ✅)
                ↓
User finds recipe → View recipe details (NO login required ✅)
                ↓
User wants to save recipe → 🔓 Bottom sheet login prompt
                            → After login: Recipe saved
                ↓
User creates log post → Already authenticated → Direct to creation screen
```

### Authentication State Management

```dart
// Auth state provider (lazy evaluation)
final authStateProvider = StreamProvider((ref) {
  return FirebaseAuth.instance.authStateChanges();
});

// Check auth before protected actions
Future<void> saveRecipe(BuildContext context, WidgetRef ref, String recipeId) async {
  final authState = await ref.read(authStateProvider.future);

  if (authState == null) {
    // User not logged in - show bottom sheet
    final loggedIn = await showLoginBottomSheet(context);
    if (!loggedIn) return; // User cancelled login
  }

  // Proceed with save
  await ref.read(recipeRepositoryProvider).saveRecipe(recipeId);
  ScaffoldMessenger.of(context).showSnackBar(
    const SnackBar(content: Text('레시피가 저장되었습니다!')),
  );
}
```

## Feed Strategy

### Home Feed Composition

The home feed is a **mixed feed** combining multiple content types to showcase recipe evolution and community activity:

**Content Types**:
1. **Popular Recipes**: High-engagement recipes based on ranking algorithm
2. **Recent Variations**: Newly created recipe variations showing how recipes evolve
3. **Photo-Centric Logs**: Recent cooking attempts with appealing photos (visual discovery)

**Feed Distribution** (approximate):
- 50% Popular recipes
- 30% Recent logs (photo-centric)
- 20% Recent variations

### Ranking Algorithm

Recipes are ranked using a **weighted score** combining multiple signals:

```dart
score = (
  w1 * recency_score +
  w2 * log_count +
  w3 * save_count +       // Planned feature
  w4 * variation_count
)
```

**Factor Definitions**:

1. **Recency Score**: Time decay function
   ```dart
   recency_score = e^(-λ * days_since_creation)
   // λ = decay constant (e.g., 0.1 for 10-day half-life)
   ```
   - Recent recipes score higher
   - Exponential decay prevents old content from dominating

2. **Log Count**: Number of cooking attempts logged
   ```dart
   log_count = total number of logs referencing this recipe
   ```
   - Indicates recipe is **actually being cooked** (vitality signal)
   - Recipes with many logs are proven, trustworthy

3. **Save Count** (Planned): Number of users who bookmarked
   ```dart
   save_count = total number of users who saved this recipe
   ```
   - Indicates recipe is worth revisiting
   - Social proof of value

4. **Variation Count**: Number of derivative recipes
   ```dart
   variation_count = count of recipes where rootPublicId = this_recipe_id
   ```
   - Indicates recipe **inspires creativity**
   - High variation count suggests adaptable, interesting base recipe

**Implementation Notes**:
- Backend pre-computes scores periodically (e.g., hourly cron job)
- Scores cached in Redis for fast feed generation
- Feed results cached for 5-10 minutes per user
- Personalization layer can be added later (user preferences, follow graph)

### Feed Performance Requirements

**SLA Targets**:
- **P50 response time**: < 150ms
- **P95 response time**: < 300ms ⚠️ **Critical threshold**
- **P99 response time**: < 500ms

**Optimization Strategies**:
1. **Materialized View**: Pre-computed feed table with scores
2. **Redis Cache**: Hot content cached for instant serving
3. **CDN for Images**: Thumbnail URLs served from CDN, not origin server
4. **Lazy Loading**: Load 20 items per page, prefetch next page
5. **Database Indexing**: Composite indexes on (score DESC, createdAt DESC, deletedAt)

## Architecture

### Clean Architecture Layers

```
lib/
├── core/                 # Shared utilities and configurations
├── domain/              # Business logic layer (entities, repositories, use cases)
├── data/                # Data layer (DTOs, data sources, repository implementations)
├── features/            # Feature modules (presentation layer)
└── shared/              # Shared models and utilities
```

### Layer Responsibilities

#### Domain Layer
- **Entities**: Pure Dart classes representing business objects
  - `CreateRecipeRequest`, `CreateLogPostRequest`
  - `RecipeDetail`, `LogPostDetail`, `LogPostSummary`
  - `SliceResponse<T>` for pagination
- **Repositories**: Abstract interfaces defining data operations
- **Use Cases**: Single-responsibility business logic operations
  - `CreateRecipeUseCase`, `CreateLogPostUseCase`
  - `GetLogPostDetailUseCase`, `GetLogPostListUseCase`
  - `UploadImageUseCase`
- **Rule**: ❌ **NO dependencies on Data or Presentation layers**

#### Data Layer
- **DTOs**: Data Transfer Objects with JSON serialization
  - All DTOs have `fromJson()` and `toJson()` methods
  - DTOs include `fromEntity()` mappers to convert domain entities
  - `toEntity()` methods to convert DTOs back to domain entities
- **Data Sources**:
  - Remote: API calls via Dio
  - Local: Offline caching and persistence
- **Repository Implementations**: Implements domain repository interfaces
- **Network Layer**: Dio-based HTTP client with interceptors

#### Presentation Layer
- **State Management**: Riverpod 2.x
  - `Provider`: For dependencies and repositories
  - `StateNotifierProvider`: For mutable state with business logic
  - `FutureProvider`: For async data fetching
  - `AsyncNotifierProvider`: For complex async state with pagination
- **UI Components**: Flutter widgets organized by feature
- **Routing**: GoRouter for declarative navigation

## Key Patterns

### 1. Provider Pattern
```dart
// ✅ CORRECT: Use ref.read in Provider initialization
final dataSourceProvider = Provider((ref) {
  return DataSource(ref.read(dioProvider));
});

// ❌ WRONG: Don't use ref.watch in Provider initialization
final dataSourceProvider = Provider((ref) {
  return DataSource(ref.watch(dioProvider));
});

// ✅ CORRECT: Use ref.watch in UI widgets
Widget build(BuildContext context, WidgetRef ref) {
  final data = ref.watch(dataProvider);
}
```

### 2. UseCase Pattern
```dart
class CreateLogPostUseCase {
  final LogPostRepository _repository;

  CreateLogPostUseCase(this._repository);

  Future<Either<Failure, LogPostDetail>> execute(
    CreateLogPostRequest request,
  ) async {
    // Validation logic
    if (request.rating < 1 || request.rating > 5) {
      return Left(ValidationFailure('평점은 1-5 사이여야 합니다.'));
    }

    // Delegate to repository
    return await _repository.createLog(request);
  }
}
```

### 3. Entity/DTO Mapping
```dart
// Domain Entity (no JSON annotations)
class CreateRecipeRequest {
  final String title;
  final List<Ingredient> ingredients;
  // ...
}

// Data DTO (with JSON serialization)
@JsonSerializable()
class CreateRecipeRequestDto {
  final String title;
  final List<IngredientDto> ingredients;

  // Mapper from domain entity
  factory CreateRecipeRequestDto.fromEntity(CreateRecipeRequest request) {
    return CreateRecipeRequestDto(
      title: request.title,
      ingredients: request.ingredients.map((e) =>
        IngredientDto.fromEntity(e)
      ).toList(),
    );
  }

  Map<String, dynamic> toJson() => _$CreateRecipeRequestDtoToJson(this);
}
```

### 4. Pagination with AsyncNotifier
```dart
class LogPostListNotifier extends AsyncNotifier<LogPostListState> {
  int _currentPage = 0;
  bool _hasNext = true;

  @override
  Future<LogPostListState> build() async {
    final items = await _fetchPage(0);
    return LogPostListState(items: items, hasNext: _hasNext);
  }

  Future<void> fetchNextPage() async {
    if (!_hasNext) return;
    _currentPage++;
    final newItems = await _fetchPage(_currentPage);
    // Update state with combined items
  }

  Future<void> refresh() async {
    _currentPage = 0;
    ref.invalidateSelf();
  }
}
```

### 5. Error Handling
```dart
// Failure classes for different error types
abstract class Failure {
  final String message;
  Failure(this.message);
}

class ServerFailure extends Failure { /* ... */ }
class ConnectionFailure extends Failure { /* ... */ }
class ValidationFailure extends Failure { /* ... */ }

// Either monad for error handling
Future<Either<Failure, Result>> operation() async {
  try {
    final result = await apiCall();
    return Right(result);
  } catch (e) {
    return Left(ServerFailure(e.toString()));
  }
}
```

## Feature Modules

### Recipe Feature

#### Create Recipe
- Multi-step form with image upload
- Ingredient management (name, amount, type: MAIN/SECONDARY/SEASONING)
- Step-by-step instructions with optional images
- **Variation Mode**: Mark as variation and select parent recipe
- Uses `ImageUploadSection` widget (max 5 images)

#### Recipe Detail
- Display ingredients, steps, and images
- **Variation tree visualization**: Show original → variations hierarchy
- "Inspired by" link to parent recipe (if this is a variation)
- Related logs: Display cooking attempts by other users
- **Actions**: Save (planned), Create variation, Create log

#### Recipe List
- Paginated list with infinite scroll
- Filter by: recency, popularity, has variations
- Search by ingredients, recipe name (planned)

#### Recipe Variations (Graph Structure)

**Implementation**:
```dart
class Recipe {
  final String publicId;
  final String? parentPublicId;  // Immediate parent
  final String? rootPublicId;    // Original recipe
  final String? changeCategory;  // Type of modification
  // ...
}
```

**Visualization Example**:
```
Original Carbonara (root)
├── Vegan Carbonara
│   └── Tofu Carbonara (changeCategory: "protein change")
└── Spicy Carbonara (changeCategory: "seasoning addition")
```

### Log Post Feature

#### Create Log
- Rate recipe (1-5 stars)
- Write review/commentary
- Upload photos (max 3 images)
- Uses `ImageUploadSection` widget

#### Log Detail
- View log with rating, review, and images
- Link to original recipe
- Creator attribution

#### Log List
- Infinite scroll pagination with pull-to-refresh
- Photo-centric card layout
- Tap to view detail

**Provider Structure**:
- `logPostCreationProvider`: StateNotifier for creation
- `logPostDetailProvider`: FutureProvider for detail view
- `logPostPaginatedListProvider`: AsyncNotifier for list with pagination

### Auth Feature
- **Social Login**: Firebase-based Google authentication
- **Token Management**: Auto-refresh with Dio interceptors
- **Session Handling**: Secure token storage in local storage

### Image Upload
- **Common Widget**: `ImageUploadSection` for reusable upload UI
- **Upload Types**: "THUMBNAIL" for recipes, "LOG_POST" for logs
- **Status Tracking**: initial → uploading → success/error
- **Features**: Camera/gallery selection, retry on error, visual feedback

## API Integration

### Backend Communication
- **Base URL**: Configured via environment
- **Dio Interceptors**:
  - Request: Add auth tokens
  - Response: Handle errors, refresh tokens
  - Logging: Development-only request/response logs

### Pagination
- **Backend**: Spring Boot Slice pagination
- **Response Structure**:
  ```json
  {
    "content": [],
    "number": 0,
    "size": 20,
    "first": true,
    "last": false,
    "hasNext": true
  }
  ```

### API Endpoints
```dart
class ApiEndpoints {
  static const String recipes = '/api/recipes';
  static String recipeDetail(String id) => '/api/recipes/$id';
  static const String logPosts = '/api/log-posts';
  static String logPostDetail(String id) => '/api/log-posts/$id';
  static const String imageUpload = '/api/images/upload';
  static const String homeFeed = '/api/home/feed';
}
```

## State Management Details

### Provider Hierarchy
```dart
// Data Source Layer
final dioProvider = Provider((ref) => Dio());
final remoteDataSourceProvider = Provider((ref) =>
  RemoteDataSource(ref.read(dioProvider))
);

// Repository Layer
final repositoryProvider = Provider<Repository>((ref) =>
  RepositoryImpl(ref.read(remoteDataSourceProvider))
);

// UseCase Layer
final useCaseProvider = Provider((ref) =>
  UseCase(ref.read(repositoryProvider))
);

// Presentation Layer
final dataProvider = FutureProvider((ref) async {
  final useCase = ref.read(useCaseProvider);
  final result = await useCase();
  return result.fold((error) => throw error, (data) => data);
});
```

### State Patterns

#### Simple Data Fetching
```dart
final dataProvider = FutureProvider<Data>((ref) async {
  final useCase = ref.read(useCaseProvider);
  final result = await useCase();
  return result.fold((failure) => throw failure, (data) => data);
});
```

#### Mutable State with Business Logic
```dart
final creationProvider = StateNotifierProvider<Notifier, AsyncValue<Data?>>((ref) {
  return Notifier(ref.read(useCaseProvider));
});

class Notifier extends StateNotifier<AsyncValue<Data?>> {
  final UseCase _useCase;

  Notifier(this._useCase) : super(const AsyncValue.data(null));

  Future<void> create(Request request) async {
    state = const AsyncValue.loading();
    final result = await _useCase.execute(request);
    state = result.fold(
      (failure) => AsyncValue.error(failure.message, StackTrace.current),
      (success) => AsyncValue.data(success),
    );
  }
}
```

#### Paginated Lists
```dart
final paginatedProvider = AsyncNotifierProvider<ListNotifier, ListState>(() {
  return ListNotifier();
});

class ListState {
  final List<Item> items;
  final bool hasNext;
  ListState({required this.items, required this.hasNext});
}
```

## Data Architecture & Consistency

### Architectural Style
**Read-Heavy System**: The app prioritizes read performance over write consistency.

**Characteristics**:
- Recipes are browsed far more often than created (100:1 read:write ratio)
- Logs and views dominate traffic
- Feed generation is the most frequent operation

**Scaling Strategy**:
- Recipe and Log are **separate entities** → prevents write hotspots
- Read replicas for feed generation and recipe queries
- Write operations are less frequent (create recipe/log)
- Eventual consistency acceptable for feeds and counters

### Data Preservation Philosophy

**✅ Soft Delete Everywhere**
- ALL deletions are soft deletes (set `deletedAt` timestamp, don't remove row)
- Entities have `deletedAt: DateTime?` field
- Deleted content excluded from queries but preserved in database
- **Rationale**: Content preservation is a core value—recipes and logs are community knowledge

**❌ NO Cascade Deletes**
- If a recipe is deleted, logs remain (orphaned but preserved)
- If a user is deleted, their content remains (anonymized: "Deleted User")
- **Rationale**: Recipes and logs have value independent of creator

**Implementation**:
```dart
// Entity with soft delete
class Recipe {
  final String publicId;
  final DateTime? deletedAt;

  bool get isDeleted => deletedAt != null;
  bool get isActive => deletedAt == null;
}

// Query with soft delete filter
Future<List<Recipe>> getActiveRecipes() {
  return db.recipes
    .where('deletedAt', isNull: true)
    .orderBy('createdAt', descending: true)
    .get();
}

// Soft delete operation
Future<void> deleteRecipe(String publicId) async {
  await db.recipes.doc(publicId).update({
    'deletedAt': FieldValue.serverTimestamp(),
  });
}
```

### Consistency Guarantees

- **Recipe creation**: Strong consistency required (user must see their recipe immediately)
- **Log creation**: Eventual consistency acceptable (may appear in feed after delay)
- **Feed updates**: Eventually consistent (cache invalidation, 5-10 min delay OK)
- **User actions (save, like)**: Optimistic UI updates, eventual backend sync

## Performance Considerations

### Image Optimization & CDN

**✅ CDN is Mandatory** for all images:
- Images served from CDN URLs, NOT backend server
- Automatic image optimization at CDN layer
- Presigned URLs for uploads (planned improvement)

**Image Specs**:
- **Upload quality**: 70% JPEG compression
- **Max file size**: 5MB per image
- **Supported formats**: JPEG, PNG
- **Thumbnail generation**: Multiple sizes for different contexts
  - 300x300 (list thumbnails)
  - 600x600 (detail page thumbnails)
  - 1200x1200 (full-size display)

**CDN Configuration**:
```dart
class ImageConfig {
  static const String cdnBaseUrl = 'https://cdn.pairingplanet.com';

  static String getThumbnail(String imagePublicId, {int size = 300}) {
    return '$cdnBaseUrl/images/$imagePublicId?w=$size&h=$size&fit=cover';
  }

  static String getFullImage(String imagePublicId) {
    return '$cdnBaseUrl/images/$imagePublicId';
  }
}

// Usage in UI
CachedNetworkImage(
  imageUrl: ImageConfig.getThumbnail(recipe.imagePublicIds.first, size: 600),
  placeholder: (context, url) => CircularProgressIndicator(),
  errorWidget: (context, url, error) => Icon(Icons.error),
)
```

**Lazy Loading**:
- Use `CachedNetworkImage` for all image display
- Progressive loading: blur placeholder → low-res → full-res
- Prefetch images for next page during scroll (80% trigger)

### Pagination
- **Page size**: 20 items per request
- **Preload trigger**: 80% scroll position (fetch next page early)
- **Deduplication by ID**: Prevent duplicate items in infinite scroll

### Network
- **Request timeout**: 30 seconds
- **Retry logic**: 3 retries with exponential backoff for failed uploads
- **Offline capability**: Local caching with read replicas

## Code Generation

### build_runner
```bash
dart run build_runner build --delete-conflicting-outputs
```

### Generated Files
- `*.g.dart`: JSON serialization code for `@JsonSerializable()` classes
- All DTOs require code generation

## Testing Strategy

### Unit Tests
- Use Cases: Business logic validation
- Repositories: Data transformation and error handling
- Providers: State management logic

### Widget Tests
- UI components
- User interactions
- State changes

### Integration Tests
- Full user flows
- API integration
- Navigation

## Development Guidelines

### Code Style
- Follow Flutter/Dart style guide
- Use `flutter analyze` before commits (0 errors required)
- No print statements in production code
- Use structured logging (talker)

### Naming Conventions
- Files: snake_case
- Classes: PascalCase
- Variables/methods: camelCase
- Constants: lowerCamelCase (NOT SCREAMING_SNAKE_CASE)

### Git Workflow
- Feature branches from main
- Conventional commits
- AI-assisted commit messages with attribution

### Common Pitfalls to Avoid
1. ❌ Don't import Data DTOs in Domain layer
2. ❌ Don't use `ref.watch` in Provider initialization
3. ❌ Don't access `.first` on potentially empty lists (use `.firstOrNull` or check `.isNotEmpty`)
4. ❌ Don't skip error handling with Either monads
5. ❌ Don't hardcode URLs or sensitive data

## Legal & Copyright Considerations

### Copyright Policy

#### Recipe Text
- ✅ **NOT copyrightable**: Recipes as lists of ingredients and instructions are considered "general cooking methods"
- Users can freely share, modify, and create variations
- No attribution legally required for recipe text
- **Rationale**: Recipes are functional instructions, like software algorithms

#### Photos & Media
- ✅ **User-owned**: Photos uploaded by users remain their intellectual property
- Users grant Pairing Planet a **non-exclusive license** to display and distribute
- Attribution shown (username displayed with log)
- Users can request photo deletion (soft delete in practice)

#### Recipe Variations
- ✅ **"Inspired By" relationship**: Variations explicitly reference parent recipe via `parentPublicId`
- Original creator acknowledged via variation tree
- **No legal obligation**, but ethical design choice (transparency)
- Encourages remix culture and knowledge evolution

### Content Moderation

**Report-Based System**:
- Users can report inappropriate content (spam, abuse, copyright infringement)
- Reports trigger **manual review** by moderation team
- Moderation actions: soft delete, user warning, account suspension
- ❌ **NO automated content filtering** (to avoid false positives)

**Prohibited Content**:
- Illegal activities (drugs, weapons, etc.)
- Hate speech, harassment, discrimination
- Sexually explicit material
- Spam or commercial advertising
- Fraudulent health claims

### Terms of Service (Key Points)

Users agree to:
1. **Grant Pairing Planet license** to display, distribute, and modify their content for platform purposes
2. **Not upload copyrighted images** without permission (own photos or public domain only)
3. **Accept that recipes can be modified** by others (variation system is core feature)
4. **Understand that content may be preserved** even if they delete it (soft delete policy)
5. **Not use the platform for commercial purposes** without explicit agreement

## Planned Features

### Save/Bookmark System
**Purpose**: Allow users to bookmark recipes for later reference and quick access.

**Implementation**:
```dart
// Domain Entity
class SavedRecipe {
  final String userId;
  final String recipePublicId;
  final DateTime savedAt;
}

// Repository Method
Future<Either<Failure, Unit>> saveRecipe(String recipeId) async {
  // Trigger progressive auth if not logged in
  await ensureAuthenticated();

  await repository.saveRecipe(userId, recipeId);
  return Right(unit);
}

// Provider
final savedRecipesProvider = FutureProvider<List<Recipe>>((ref) async {
  final userId = await ref.read(authStateProvider.future).then((user) => user?.uid);
  if (userId == null) return [];

  final savedIds = await repository.getSavedRecipeIds(userId);
  return repository.getRecipesByIds(savedIds);
});
```

**UX**:
- Bookmark icon on recipe cards and detail page
- "My Saved Recipes" section in profile
- Saved recipes sortable by: recently saved, alphabetical, rating
- Contributes to recipe ranking algorithm (save_count factor)

### Hashtag System
**Purpose**: Weak classification for discovery and contextual connections between recipes.

**Implementation**:
```dart
// Domain Entity
class Hashtag {
  final String tag;           // e.g., "vegetarian", "quick-meal", "spicy"
  final int usageCount;       // Number of recipes using this tag
  final bool trending;        // Flag for trending tags (top 20 by recent usage)
}

// Recipe Entity Update
class Recipe {
  // ...
  final List<String> hashtags; // 0-5 hashtags per recipe
}

// Repository Methods
Future<List<Hashtag>> getTrendingHashtags({int limit = 20}) async {
  return await db.hashtags
    .orderBy('recentUsageCount', descending: true)
    .limit(limit)
    .get();
}

Future<List<String>> autocompleteHashtags(String query) async {
  return await db.hashtags
    .where('tag', isGreaterThanOrEqualTo: query)
    .where('tag', isLessThan: query + 'z')
    .orderBy('usageCount', descending: true)
    .limit(10)
    .get()
    .then((list) => list.map((h) => h.tag).toList());
}
```

**Features**:
- **User-generated hashtags**: Free-form input during recipe creation
- **Autocomplete**: Suggest popular hashtags as user types
- **Trending hashtags**: Displayed in explore/discovery section
- **Tap to explore**: Click hashtag to see all recipes with that tag

**UX Considerations**:
- Hashtags are **suggestions, not strict categories**
- Recipes can have **0-5 hashtags** (flexible, not enforced)
- No hashtag validation—organic, user-driven discovery
- Visual design: Chips with # prefix (e.g., #vegetarian, #quick-meal)

## Dependencies

### Core
- `flutter_riverpod: ^2.x` - State management
- `go_router: ^x.x` - Routing
- `dio: ^5.x` - HTTP client
- `dartz: ^0.10.x` - Functional programming (Either monad)

### Code Generation
- `json_serializable: ^x.x`
- `build_runner: ^x.x`

### UI/UX
- `cached_network_image: ^x.x` - Image caching
- `image_picker: ^x.x` - Camera/gallery access

### Firebase
- `firebase_core: ^x.x`
- `firebase_auth: ^x.x`
- `google_sign_in: ^x.x`

## Deployment

### Build Configuration
- Development: `flutter run --debug`
- Production: `flutter build apk --release` / `flutter build ios --release`

### Environment Variables
- API base URL
- Firebase configuration
- Feature flags

## Production-Ready Enhancements

This section documents critical production features for reliability, performance, and scalability.

### 1. Event-Based Architecture (Analytics & ML)

**Philosophy**: Transition from CRUD-centric to event-driven architecture for analytics, ML insights, and future features.

**Event Types**:
```dart
enum EventType {
  // Write events (immediate priority)
  recipeCreated,
  logCreated,
  variationCreated,
  logFailed,

  // Read events (batched priority)
  recipeViewed,
  logViewed,
  recipeSaved,
  recipeShared,
  variationTreeViewed,
  searchPerformed,
  logPhotoUploaded,
}

enum EventPriority {
  immediate,  // Critical write operations - sent immediately
  batched,    // Analytics/metrics - batched every 30-60 seconds
}
```

**Implementation Pattern**: **Outbox Pattern**
- All events queued locally in Isar database first
- Immediate events sent synchronously if network available
- Batched events sent every 30-60 seconds
- Background worker retries failed events
- Optimistic UI updates (always update local state first)

**Event Schema**:
```dart
class AppEvent {
  final String eventId;        // UUID for idempotency
  final EventType eventType;
  final String? userId;        // Null for anonymous users
  final DateTime timestamp;
  final Map<String, dynamic> properties;  // Event-specific data
  final String? recipeId;
  final String? logId;
  final EventPriority priority;
}
```

**Storage**: Isar collection `QueuedEvent` with sync status tracking

**Background Sync**: WorkManager periodic task (every 30 minutes) + manual triggers

**Benefits**:
- ML training data (recipe trends, user behavior)
- Analytics without blocking user actions
- Offline-resilient (events queued until network available)
- Audit trail for debugging

### 2. Offline-First Strategy

**Philosophy**: Kitchen use requires offline access to recipes user cares about, not entire catalog.

**Selective Caching Strategy**:
- ✅ **Cache**: Saved/bookmarked recipes (user explicitly wants offline access)
- ✅ **Cache**: Recipes linked to user's logs (recipes user has cooked)
- ✅ **Cache**: Recently viewed recipes (last 10-20, LRU eviction)
- ❌ **Don't cache**: Entire recipe feed (wasteful, network-on-demand is fine)

**Implementation**:
```dart
enum CacheReason {
  saved,          // User bookmarked
  ownedLog,       // User created log for this recipe
  recentlyViewed, // Recently accessed
}

@collection
class CachedRecipe {
  Id id = Isar.autoIncrement;

  @Index(unique: true)
  late String publicId;

  late String title;
  String? description;
  late List<String> imagePublicIds;
  late String ingredientsJson;  // Serialized JSON
  late String stepsJson;

  @enumerated
  late CacheReason reason;

  late DateTime cachedAt;
  late DateTime lastAccessedAt;
}
```

**Repository Pattern**:
1. Check local Isar cache first
2. If cached → return immediately (offline-first)
3. If not cached and network available → fetch from API + cache
4. If offline and not cached → return `ConnectionFailure`

**Cache Cleanup**:
- Keep all `saved` and `ownedLog` recipes indefinitely
- Keep last 20 `recentlyViewed` (LRU eviction)
- Periodic cleanup task (daily)

### 3. Image Compression & WebP Conversion

**Current Issue**: 5MB JPEG uploads waste bandwidth and CDN storage.

**Solution**: Client-side compression before upload.

**Pipeline**:
1. User selects image
2. Client resizes to max 1200x1200 (maintain aspect ratio)
3. Convert to WebP format (quality: 80%)
4. Generate perceptual hash (8x8 average hash for deduplication)
5. Check if hash exists on backend (global deduplication)
6. If exists → reuse existing `imagePublicId` (no upload needed)
7. If new → upload compressed WebP

**Compression Specs**:
- Max dimension: 1200px (resized with aspect ratio)
- Format: WebP (superior compression vs JPEG)
- Quality: 80%
- Expected reduction: **>80%** (5MB → <1MB)

**Perceptual Hash Algorithm**:
```dart
static String _computePerceptualHash(img.Image image) {
  // 1. Resize to 8x8
  final resized = img.copyResize(image, width: 8, height: 8);

  // 2. Convert to grayscale
  final grayscale = img.grayscale(resized);

  // 3. Compute average pixel value
  final pixels = <int>[];
  for (var y = 0; y < 8; y++) {
    for (var x = 0; x < 8; x++) {
      pixels.add(grayscale.getPixel(x, y).r.toInt());
    }
  }
  final avg = pixels.reduce((a, b) => a + b) ~/ pixels.length;

  // 4. Generate hash (64-bit binary)
  final hash = pixels.map((p) => p > avg ? '1' : '0').join('');
  return hash;
}
```

**Global Image Deduplication**:
- Backend stores hash → `imagePublicId` mapping
- Before upload, client queries: `GET /api/images/find-by-hash?hash={hash}`
- If found → backend returns existing `ImageUploadResponseDto` (no upload)
- If not found → client uploads, backend stores hash

**Benefits**:
- CDN cost savings (same image stored once globally)
- Bandwidth savings for users
- Faster uploads
- Data insight (find recipes using same image → implicit connections)

### 4. Anonymous Content Limits & Migration

**Problem**: Anonymous users can spam content without accountability.

**Solution**: Limit anonymous users to **1 recipe OR 1 log post**.

**Login Trigger Points**:
1. **After creating 1st piece of content** → Soft nudge: "Sign in to save your work to cloud" (dismissable)
2. **Before creating 2nd piece of content** → Hard block: Bottom sheet login (required)

**Implementation**:
```dart
final anonymousUserIdProvider = Provider<String>((ref) {
  // Device-bound anonymous ID: 'anon_${UUID}'
  // Stored in secure storage
});

class AnonymousContentCount {
  final int recipeCount;
  final int logCount;

  bool get canCreateRecipe => recipeCount < 1;
  bool get canCreateLog => logCount < 1;
  bool get hasReachedLimit => !canCreateRecipe || !canCreateLog;
}
```

**Content Migration on Login**:
- When anonymous user logs in, call backend migration API
- Backend reassigns `creatorId` from `anon_{UUID}` to Firebase UID
- Local counters cleared after successful migration

**Benefits**:
- Lets users experience value (create once)
- Prevents data loss (login before investing more)
- Anti-spam (limits anonymous abuse)

### 5. Observability & Monitoring

**Tool**: Sentry for error tracking and performance monitoring.

**Sentry Configuration**:
```dart
await SentryFlutter.init((options) {
  options.dsn = const String.fromEnvironment('SENTRY_DSN');
  options.environment = const String.fromEnvironment('ENV', defaultValue: 'dev');
  options.tracesSampleRate = 0.1;    // 10% of transactions
  options.profilesSampleRate = 0.05;  // 5% profiling
  options.enableAutoPerformanceTracing = true;

  // Filter sensitive data (PII)
  options.beforeSend = (event, hint) {
    event.user = event.user?.copyWith(
      email: null,
      username: null,
    );
    return event;
  };
});
```

**What to Track**:
- ✅ **Crashes**: 100% of unhandled exceptions
- ✅ **API errors**: All Dio errors with endpoint context
- ✅ **Performance**: 10% of transactions (sampling)
- ✅ **User flows**: Breadcrumbs for debugging (navigation, events)
- ❌ **PII**: Filter emails, usernames, tokens

**Dio Integration**:
```dart
dio.interceptors.add(SentryDioClientAdapter());

dio.interceptors.add(InterceptorsWrapper(
  onError: (error, handler) async {
    await Sentry.captureException(
      error,
      stackTrace: error.stackTrace,
      hint: Hint.withMap({
        'endpoint': error.requestOptions.path,
        'method': error.requestOptions.method,
        'status_code': error.response?.statusCode,
      }),
    );
    handler.next(error);
  },
));
```

**Benefits**:
- Real-time crash detection
- Performance regression alerts
- User session replay (understand error context)
- Release health tracking (crash-free sessions)

### 6. Idempotency for Write Operations

**Problem**: Network retries or duplicate requests can create duplicate content.

**Solution**: Add `Idempotency-Key` header to all POST/PUT/PATCH requests.

**Implementation**:
```dart
class IdempotencyInterceptor extends Interceptor {
  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    if (options.method == 'POST' ||
        options.method == 'PUT' ||
        options.method == 'PATCH') {
      options.headers['Idempotency-Key'] = Uuid().v4();
    }
    super.onRequest(options, handler);
  }
}

// Add to Dio setup
dio.interceptors.add(IdempotencyInterceptor());
```

**Backend Behavior**:
- Server stores `Idempotency-Key` → response mapping (24 hours TTL)
- If duplicate key received within 24h → return cached response (no duplicate write)
- After 24h → key expires, new operation allowed

**Benefits**:
- Prevents duplicate recipe/log creation
- Retry-safe API calls
- Consistent user experience (no "duplicate submitted" errors)

### 7. Production Dependencies

**New Dependencies**:
```yaml
dependencies:
  # Offline database
  isar: ^3.1.0+1
  isar_flutter_libs: ^3.1.0+1

  # Image processing
  flutter_image_compress: ^2.1.0
  image: ^4.1.3

  # Background work
  workmanager: ^0.5.1

  # Monitoring
  sentry_flutter: ^7.13.2

  # Utilities
  uuid: ^4.3.3

dev_dependencies:
  isar_generator: ^3.1.0+1
  build_runner: ^2.4.7
```

### 8. Implementation Phases

**Phase 1 (P0)**: Event Tracking Infrastructure
- Event schema & Isar storage
- Outbox pattern repository
- Background sync worker
- Event tracking in UI actions

**Phase 2 (P0)**: Image Compression & Deduplication
- Image processing pipeline (resize, WebP, hash)
- Global deduplication check before upload
- Update `UploadImageUseCase`

**Phase 3 (P0)**: Offline-First Caching
- Isar schema for cached recipes/logs
- Offline-first repository pattern
- Cache management (LRU cleanup)

**Phase 4 (P0)**: Anonymous Content Limits
- Anonymous user tracking
- Content limit guards
- Migration API on login

**Phase 5 (P1)**: Sentry Observability
- Sentry initialization
- Dio error tracking
- Performance monitoring

**Phase 6 (P0)**: Idempotency Keys
- Idempotency interceptor
- Add to Dio setup

### 9. Success Criteria

- ✅ Events tracked with <5% loss rate
- ✅ Image size reduced by >80% (5MB → <1MB)
- ✅ Offline recipe access for saved/logged recipes
- ✅ Anonymous users limited to 1 recipe + 1 log
- ✅ Global image deduplication (no duplicate storage)
- ✅ Sentry capturing 100% of crashes and 10% of transactions
- ✅ Zero duplicate writes (idempotency enforced)

## Future Improvements

### Short-Term
- ✅ Complete save/bookmark system implementation
- ✅ Implement hashtag discovery and search
- ✅ Enhanced variation tree visualization (interactive graph view)
- ✅ Recipe search by ingredients, name, hashtags
- ✅ User profiles with created recipes and logs

### Medium-Term
- 🔔 Push notifications for recipe interactions (someone cooked your recipe, new variation created)
- 👥 Social features: Follow users, activity feed of followed creators
- 💬 Comments on recipes and logs (threaded discussions)
- ⭐ Improved ranking algorithm with personalization (based on user's cooking history)

### Long-Term
- 🤖 ML-based recipe recommendations (collaborative filtering based on cooking history)
- 🌐 Multi-language support (i18n) for global expansion
- 📊 Analytics dashboard for creators (engagement metrics, view counts, variation impact)
- 🎥 Video support for cooking steps (short-form video integration)
- 🛒 Ingredient shopping list integration (export to grocery apps)
