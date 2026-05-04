package com.alvinfungai.providers.data.di

import com.alvinfungai.providers.data.repository.NotificationRepositoryImpl
import com.alvinfungai.providers.data.repository.SupabaseServiceProvidersRepositoryImpl
import com.alvinfungai.providers.domain.repository.NotificationRepository
import com.alvinfungai.providers.domain.repository.ServiceProvidersRepository
import com.alvinfungai.providers.domain.usecase.GetNotificationsUseCase
import com.alvinfungai.providers.domain.usecase.GetServiceProviderByUserIdUseCase
import com.alvinfungai.providers.domain.usecase.GetServiceProviderDetailsUseCase
import com.alvinfungai.providers.domain.usecase.GetServiceProvidersUseCase
import com.alvinfungai.providers.domain.usecase.MarkNotificationAsReadUseCase
import com.alvinfungai.providers.domain.usecase.RegisterServiceProviderUseCase
import com.alvinfungai.providers.domain.usecase.SaveNotificationUseCase
import com.alvinfungai.providers.domain.usecase.UpdateProviderLastActiveUseCase
import com.alvinfungai.providers.domain.usecase.UpdateProviderRatingUseCase
import com.alvinfungai.providers.domain.usecase.UpdateServiceProviderUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.SupabaseClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ServiceProvidersModule {

    @Provides
    fun provideServicesProvidersRepository(
        supabaseClient: SupabaseClient,
        firebaseAuth: FirebaseAuth
    ): ServiceProvidersRepository {
        return SupabaseServiceProvidersRepositoryImpl(supabaseClient, firebaseAuth)
    }

    @Provides
    fun provideNotificationRepository(firestore: FirebaseFirestore): NotificationRepository {
        return NotificationRepositoryImpl(firestore)
    }

    @Provides
    fun provideGetServiceProvidersUseCase(repository: ServiceProvidersRepository): GetServiceProvidersUseCase {
        return GetServiceProvidersUseCase(repository)
    }

    @Provides
    fun provideRegisterServiceProviderUseCase(repository: ServiceProvidersRepository): RegisterServiceProviderUseCase {
        return RegisterServiceProviderUseCase(repository)
    }

    @Provides
    fun provideGetServiceProviderDetailsUseCase(repository: ServiceProvidersRepository): GetServiceProviderDetailsUseCase {
        return GetServiceProviderDetailsUseCase(repository)
    }

    @Provides
    fun provideUpdateServiceProviderUseCase(repository: ServiceProvidersRepository): UpdateServiceProviderUseCase {
        return UpdateServiceProviderUseCase(repository)
    }

    @Provides
    fun provideGetServiceProviderByUserIdUseCase(repository: ServiceProvidersRepository): GetServiceProviderByUserIdUseCase {
        return GetServiceProviderByUserIdUseCase(repository)
    }

    @Provides
    fun provideGetNotificationsUseCase(repository: NotificationRepository): GetNotificationsUseCase {
        return GetNotificationsUseCase(repository)
    }

    @Provides
    fun provideMarkNotificationAsReadUseCase(repository: NotificationRepository): MarkNotificationAsReadUseCase {
        return MarkNotificationAsReadUseCase(repository)
    }

    @Provides
    fun provideUpdateProviderLastActiveUseCase(repository: ServiceProvidersRepository): UpdateProviderLastActiveUseCase {
        return UpdateProviderLastActiveUseCase(repository)
    }

    @Provides
    fun provideUpdateProviderRatingUseCase(repository: ServiceProvidersRepository): UpdateProviderRatingUseCase {
        return UpdateProviderRatingUseCase(repository)
    }


    @Provides
    fun provideSaveNotificationUseCase(repository: NotificationRepository): SaveNotificationUseCase {
        return SaveNotificationUseCase(repository)
    }
}