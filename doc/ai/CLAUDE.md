# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Setup & Code Generation
```bash
# Install dependencies
flutter pub get

# Generate DTOs and serialization code (REQUIRED after modifying @JsonSerializable classes)
dart run build_runner build --delete-conflicting-outputs

# Watch mode for continuous code generation during development
dart run build_runner watch --delete-conflicting-outputs
```

### Running the App
```bash
# Run on connected device/emulator
flutter run

# Run specific platform
flutter run -d android
flutter run -d ios
```

### Environment Setup
- Create `.env` file in project root with:
  ```
  BASE_URL=http://10.0.2.2:4001/api/v1  # Android emulator localhost
  ENV=dev
  ```

## Architecture Overview

### Clean Architecture Layers

**Data Layer** (`lib/data/`)
- **DataSources**: Remote (Dio) and Local (Hive)
- **Models**: DTOs with `@JsonSerializable` + `.g.dart` generated files
  - All DTOs include `toEntity()` method for domain mapping
- **Repositories**: Implements domain interfaces with offline-first pattern:
  1. Check local cache
  2. Try remote if online
  3. Cache successful responses
  4. Fallback to cache on error

**Domain Layer** (`lib/domain/`)
- **Entities**: Pure Dart classes (no json_annotation)
- **Repositories**: Abstract interfaces
- **UseCases**: Business logic returning `Future<Either<Failure, T>>`

**Presentation Layer** (`lib/features/`)
- Organized by feature (recipe, log_post, auth, etc.)
- **Providers**: Riverpod state management
- **Screens**: UI components

**Core Layer** (`lib/core/`)
- **router**: GoRouter with auth guards
- **network**: Dio setup with interceptors
- **constants**: API endpoints, routes

### State Management: Riverpod

Provider dependency chain:
```dart
dioProvider → dataSourceProvider → repositoryProvider → useCaseProvider → stateProvider
```

**Key patterns:**
- `Provider`: Immutable dependencies (repositories, use cases)
- `StateNotifierProvider`: Mutable state (auth, forms)
- `FutureProvider.family`: Data fetching with parameters
- Use `ref.read()` in provider constructors, `ref.watch()` in UI

### Navigation: GoRouter

- Routes defined in `lib/core/router/app_router.dart`
- Bottom nav uses `StatefulShellRoute.indexedStack`
- Auth guard via `redirect` callback checking `authStateProvider`
- Pass complex objects via `extra` parameter:
  ```dart
  context.push('/recipe/create', extra: recipeDetail);
  ```

### Error Handling

Uses `Either<Failure, T>` pattern (dartz):
- `ServerFailure`, `ConnectionFailure`, `NotFoundFailure`, `UnauthorizedFailure`
- Repositories map DioException to Failure types
- Interceptors handle common errors (401, 500, timeout)

## Project-Specific Patterns

### Pagination: Slice vs Paged

Backend uses Spring's **Slice** for log posts:
```dart
class SliceResponseDto<T> {
  final List<T> content;  // "content" not "items"
  final bool hasNext;
}
```

Custom **Paged** for recipes:
```dart
class PagedResponseDto<T> {
  final List<T> items;
  final bool hasNext;
}
```

### Image Upload Flow

Multi-step process:
1. Pick image with `image_picker`
2. Upload to image service → get `publicId` and `url`
3. Collect `imagePublicIds` in list
4. Submit with entity:
   ```dart
   CreateRecipeRequestDto(
     imagePublicIds: ['img-123', 'img-456'],
     // other fields
   )
   ```

**Pattern**: Images uploaded independently, then linked by publicId.

### Recipe Variation/Lineage System

Recipes have parent-child relationships:
- `rootInfo`: Original recipe at top of tree
- `parentInfo`: Immediate parent (for variants)
- `variants`: List of child recipes
- `changeCategory`: Reason for variation

**Creating a variant:**
```dart
// Navigate with parent recipe
context.push(RouteConstants.recipeCreate, extra: parentRecipe);

// Pre-populate form with parent data
// Submit with lineage
CreateRecipeRequestDto(
  parentPublicId: parentRecipe?.publicId,
  rootPublicId: parentRecipe?.rootInfo?.publicId,
  changeCategory: changeReason,
)
```

### Authentication Flow

**Login (Google OAuth):**
1. Firebase Auth handles OAuth → get ID token
2. Send to backend `POST /auth/login`:
   ```dart
   SocialLoginRequestDto(
     provider: 'GOOGLE',
     idToken: firebaseIdToken,
     locale: 'ko-KR',  // or 'en-US'
   )
   ```
3. Backend returns access + refresh tokens
4. Store in `flutter_secure_storage`

**Token Refresh (Automatic):**
- `AuthInterceptor` catches 401 responses
- Uses separate `_refreshDio` to avoid interceptor loops
- Calls `POST /auth/reissue` with refresh token
- Retries original request with new token
- If refresh fails → clears tokens → redirects to login

### Localization

- Translation files: `assets/translations/ko-KR.json`, `en-US.json`
- Device locale used by default
- Current locale sent in `Accept-Language` header
- Usage: `'recipe.detail'.tr()`

## Key Development Patterns

### Adding New API Endpoints

1. **Define endpoint** in `lib/core/constants/constants.dart`:
   ```dart
   class ApiEndpoints {
     static const String myEndpoint = '/my-endpoint';
   }
   ```

2. **Create DTO** with `@JsonSerializable`:
   ```dart
   @JsonSerializable()
   class MyResponseDto {
     factory MyResponseDto.fromJson(Map<String, dynamic> json) =>
         _$MyResponseDtoFromJson(json);
     MyEntity toEntity() => MyEntity(...);
   }
   ```

3. **Generate code**: `dart run build_runner build --delete-conflicting-outputs`

4. **Add DataSource method** → **Update Repository** → **Create Provider**

### Adding New Routes

**File**: `lib/core/router/app_router.dart`

```dart
// Top-level route
GoRoute(
  path: RouteConstants.myRoute,
  builder: (context, state) => MyScreen(),
),

// Bottom nav route (inside StatefulShellBranch)
StatefulShellBranch(
  routes: [
    GoRoute(
      path: RouteConstants.myFeature,
      builder: (context, state) => MyFeatureScreen(),
    ),
  ],
),
```

Update `RouteConstants` in `lib/core/constants/constants.dart`.

### Code Generation Requirements

**When to regenerate:**
- After adding/modifying `@JsonSerializable` classes
- After changing DTO fields

**ALWAYS commit `.g.dart` files** to version control.

## Important Gotchas

1. **Always run build_runner** after modifying DTOs
2. **AuthInterceptor uses separate Dio instance** to avoid infinite loops
3. **Backend Slice uses `content` field**, not `items`
4. **Entities never import json_annotation** (domain layer purity)
5. **Image uploads happen separately** from entity creation
6. **Recipe variants MUST include rootPublicId** to maintain lineage
7. **Use `ref.read()` in providers** to avoid circular dependencies
8. **Generic DTOs require custom deserializer**:
   ```dart
   SliceResponseDto.fromJson(data, LogPostSummaryDto.fromJson)
   ```

## Dio Interceptor Stack (in order)

1. Talker Logger (request/response logging)
2. Common Headers (Accept-Language from localeProvider)
3. Auth Interceptor (Bearer token, 401 refresh)
4. Retry Interceptor (502/503/504)
5. Toast/Error Handler (user-facing errors)

## Offline-First Repository Pattern

```dart
// 1. Check local cache
final cached = await localDataSource.getData(id);

// 2. Try remote if online
if (await networkInfo.isConnected) {
  try {
    final remote = await remoteDataSource.getData(id);
    await localDataSource.cache(remote);  // Update cache
    return Right(remote.toEntity());
  } catch (e) {
    if (cached != null) return Right(cached.toEntity());  // Fallback
    return Left(ServerFailure());
  }
}

// 3. Return cache if offline
if (cached != null) return Right(cached.toEntity());
return Left(ConnectionFailure());
```
