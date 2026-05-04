package com.alvinfungai.coworkapp.di

import com.alvinfungai.app.core.createSupabaseClient
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(firebaseAuth: FirebaseAuth): SupabaseClient {
        return createSupabaseClient(
            tokenProvider = {
                firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
            }
        )
    }
}
