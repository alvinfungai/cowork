package com.alvinfungai.cowork.di

import com.alvinfungai.users.domain.repository.AuthRepository
import com.alvinfungai.users.domain.repository.UserRepository
import com.alvinfungai.users.domain.usecase.CreateUserProfileUseCase
import com.alvinfungai.users.domain.usecase.DeleteProfileUseCase
import com.alvinfungai.users.domain.usecase.GetCurrentUserUseCase
import com.alvinfungai.users.domain.usecase.GetUserProfileUseCase
import com.alvinfungai.users.domain.usecase.LoginUseCase
import com.alvinfungai.users.domain.usecase.LogoutUseCase
import com.alvinfungai.users.domain.usecase.RegisterUseCase
import com.alvinfungai.users.domain.usecase.UpdateFcmTokenUseCase
import com.alvinfungai.users.domain.usecase.UpdateLastActiveUseCase
import com.alvinfungai.users.domain.usecase.UpdateProfileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UserDomainModule {

    @Provides
    fun provideLoginUseCase(authRepository: AuthRepository) = LoginUseCase(authRepository)

    @Provides
    fun provideRegisterUseCase(authRepository: AuthRepository) = RegisterUseCase(authRepository)

    @Provides
    fun provideLogoutUseCase(authRepository: AuthRepository) = LogoutUseCase(authRepository)

    @Provides
    fun provideGetCurrentUserUseCase(authRepository: AuthRepository): GetCurrentUserUseCase {
        return GetCurrentUserUseCase(authRepository)
    }

    @Provides
    fun provideCreateUserProfileUseCase(userRepository: UserRepository): CreateUserProfileUseCase {
        return CreateUserProfileUseCase(userRepository)
    }

    @Provides
    fun provideGetUserProfileUseCase(userRepository: UserRepository): GetUserProfileUseCase {
        return GetUserProfileUseCase(userRepository)
    }

    @Provides
    fun provideDeleteProfileUseCase(userRepository: UserRepository): DeleteProfileUseCase {
        return DeleteProfileUseCase(userRepository)
    }

    @Provides
    fun provideUpdateFcmTokenUseCase(userRepository: UserRepository): UpdateFcmTokenUseCase {
        return UpdateFcmTokenUseCase(userRepository)
    }

    @Provides
    fun provideUpdateProfileUseCase(userRepository: UserRepository): UpdateProfileUseCase {
        return UpdateProfileUseCase(userRepository)
    }

    @Provides
    fun provideUpdateLastActiveUseCase(userRepository: UserRepository): UpdateLastActiveUseCase {
        return UpdateLastActiveUseCase(userRepository)
    }
}
