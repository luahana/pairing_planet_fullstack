package com.cookstemma.app.di

import com.cookstemma.app.data.api.ApiService
import com.cookstemma.app.data.auth.TokenManager
import com.cookstemma.app.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: ApiService,
        tokenManager: TokenManager
    ): AuthRepository {
        return AuthRepository(apiService, tokenManager)
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(apiService: ApiService): RecipeRepository {
        return RecipeRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideFeedRepository(apiService: ApiService): FeedRepository {
        return FeedRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideUserRepository(apiService: ApiService): UserRepository {
        return UserRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideLogRepository(apiService: ApiService): LogRepository {
        return LogRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideSearchRepository(apiService: ApiService): SearchRepository {
        return SearchRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(apiService: ApiService): NotificationRepository {
        return NotificationRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideCommentRepository(apiService: ApiService): CommentRepository {
        return CommentRepository(apiService)
    }
}
