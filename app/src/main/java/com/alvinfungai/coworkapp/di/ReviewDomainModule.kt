package com.alvinfungai.coworkapp.di

import com.alvinfungai.reviews.domain.AddReviewUseCase
import com.alvinfungai.reviews.domain.GetReviewsForProviderUseCase
import com.alvinfungai.reviews.domain.ReviewRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ReviewDomainModule {

    @Provides
    fun provideAddReviewUseCase(repository: ReviewRepository): AddReviewUseCase {
        return AddReviewUseCase(repository)
    }

    @Provides
    fun provideGetReviewsForProviderUseCase(repository: ReviewRepository): GetReviewsForProviderUseCase {
        return GetReviewsForProviderUseCase(repository)
    }
}
