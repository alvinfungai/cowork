package com.alvinfungai.users.data.di

import com.alvinfungai.users.data.repository.UserRepositoryImpl
import com.alvinfungai.users.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        supabaseClient: SupabaseClient
    ): UserRepository = UserRepositoryImpl(firestore, supabaseClient)
}
