package com.alvinfungai.bookings.data.di

import com.alvinfungai.bookings.data.repository.BookingsRepositoryImpl
import com.alvinfungai.bookings.domain.repository.BookingsRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BookingsDataModule {

    @Provides
    @Singleton
    fun provideBookingsRepository(firestore: FirebaseFirestore): BookingsRepository {
        return BookingsRepositoryImpl(firestore)
    }
}
