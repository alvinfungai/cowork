package com.alvinfungai.app.core

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import java.util.concurrent.TimeUnit

object SupabaseConfig {
    const val URL = BuildConfig.SUPABASE_URL
    const val ANON_KEY = BuildConfig.SUPABASE_ANON_KEY
}

@OptIn(SupabaseInternal::class)
fun createSupabaseClient(
    tokenProvider: (suspend () -> String?)? = null
): SupabaseClient {
    return createSupabaseClient(
        supabaseUrl = SupabaseConfig.URL,
        supabaseKey = SupabaseConfig.ANON_KEY
    ) {
        httpEngine = OkHttp.create {
            config {
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
                retryOnConnectionFailure(true)
            }
        }

        tokenProvider?.let { provider ->
            accessToken = {
                // If the provider returns null (no user), fallback to ANON_KEY for public access
                provider() ?: SupabaseConfig.ANON_KEY
            }
        }

        install(Postgrest)
        install(Storage)
        install(Realtime)

        httpConfig {
            install(HttpTimeout) {
                requestTimeoutMillis = 30000L
                connectTimeoutMillis = 15000L
                socketTimeoutMillis = 30000L
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("KTOR_LOG: $message")
                    }
                }
                level = LogLevel.INFO // Production: INFO level is usually enough
            }
        }
    }
}
