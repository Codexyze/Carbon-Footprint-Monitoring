package com.nutrino.carbonfootprint.data.remote.auth

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SignInResponse(
    @SerialName("user_id")
    val userId: Int? = null,
    @SerialName("access_token")
    val accessToken: String? = null,
    @SerialName("token_type")
    val tokenType: String? = null
)