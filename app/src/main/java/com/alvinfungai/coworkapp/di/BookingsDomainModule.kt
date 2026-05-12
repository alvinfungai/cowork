package com.alvinfungai.coworkapp.di

import com.alvinfungai.bookings.domain.repository.BookingsRepository
import com.alvinfungai.bookings.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object BookingsDomainModule {

    @Provides
    fun provideCreateBookingUseCase(bookingsRepository: BookingsRepository) = CreateBookingUseCase(bookingsRepository)

    @Provides
    fun provideGetBookingsForUserUseCase(bookingsRepository: BookingsRepository) = GetBookingsForUserUseCase(bookingsRepository)

    @Provides
    fun provideGetBookingsForProviderUseCase(bookingsRepository: BookingsRepository) = GetBookingsForProviderUseCase(bookingsRepository)

    @Provides
    fun provideUpdateBookingStatusUseCase(bookingsRepository: BookingsRepository) = UpdateBookingStatusUseCase(bookingsRepository)

    @Provides
    fun provideSubmitProofOfWorkUseCase(bookingsRepository: BookingsRepository) = SubmitProofOfWorkUseCase(bookingsRepository)

    @Provides
    fun provideGetProofOfWorkUseCase(bookingsRepository: BookingsRepository) = GetProofOfWorkUseCase(bookingsRepository)

    @Provides
    fun provideGetPendingProofOfWorksUseCase(bookingsRepository: BookingsRepository) = GetPendingProofOfWorksUseCase(bookingsRepository)

    @Provides
    fun provideVerifyProofOfWorkUseCase(bookingsRepository: BookingsRepository) = VerifyProofOfWorkUseCase(bookingsRepository)
}
