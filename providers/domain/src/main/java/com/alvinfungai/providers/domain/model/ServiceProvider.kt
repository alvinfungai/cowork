package com.alvinfungai.providers.domain.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ServiceProvider @OptIn(ExperimentalSerializationApi::class) constructor(
    /**
     * The primary key in Supabase (uuid). 
     * Nullable so that Supabase can auto-generate it on insert.
     */
    @SerialName("id") 
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val id: String? = null,

    /**
     * The foreign key to the user. 
     * Note: If your Supabase 'user_id' column is type 'uuid', 
     * but you are passing a Firebase UID string, this will cause a 400 error.
     */
    @SerialName("user_id") 
    val userId: String = "",

    @SerialName("name") val name: String = "",
    @SerialName("profession") val profession: String = "",
    @SerialName("category") val category: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("hourly_rate") val hourlyRate: Double = 0.0,
    @SerialName("service_radius_km") val serviceRadiusKm: Double = 0.0,
    @SerialName("rating_avg") @EncodeDefault(EncodeDefault.Mode.ALWAYS) val ratingAvg: Double = 0.0,
    @SerialName("rating_count") @EncodeDefault(EncodeDefault.Mode.ALWAYS) val ratingCount: Int = 0,
    @SerialName("services") val services: List<String> = emptyList(),
    @SerialName("latitude") val latitude: Double = 0.0,
    @SerialName("longitude") val longitude: Double = 0.0,
    @SerialName("last_active_at") val lastActiveAt: Long? = null,
)
