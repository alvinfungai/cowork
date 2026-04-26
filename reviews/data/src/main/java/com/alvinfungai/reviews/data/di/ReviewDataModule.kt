package com.alvinfungai.reviews.data.di

import com.alvinfungai.reviews.data.ReviewRepositoryImpl
import com.alvinfungai.reviews.domain.ReviewRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReviewDataModule {

    @Provides
    @Singleton
    fun provideReviewRepository(firestore: FirebaseFirestore): ReviewRepository {
        return ReviewRepositoryImpl(firestore)
    }
}
