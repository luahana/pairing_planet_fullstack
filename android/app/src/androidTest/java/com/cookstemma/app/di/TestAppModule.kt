package com.cookstemma.app.di

import com.cookstemma.app.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

/**
 * Test module that provides mock repositories for E2E tests.
 *
 * This replaces the production RepositoryModule during instrumented tests,
 * allowing tests to run with controlled mock data.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
object TestAppModule {

    @Provides
    @Singleton
    fun provideFeedRepository(): FeedRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideRecipeRepository(): RecipeRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideLogRepository(): LogRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideSearchRepository(): SearchRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideNotificationRepository(): NotificationRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideCommentRepository(): CommentRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository = mockk(relaxed = true)
}
