package com.alvinfungai.cowork.di

import com.alvinfungai.bookings.domain.repository.BookingsRepository
import com.alvinfungai.bookings.domain.usecase.CreateBookingUseCase
import com.alvinfungai.bookings.domain.usecase.GetBookingsForProviderUseCase
import com.alvinfungai.bookings.domain.usecase.GetBookingsForUserUseCase
import com.alvinfungai.bookings.domain.usecase.UpdateBookingStatusUseCase
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
}
