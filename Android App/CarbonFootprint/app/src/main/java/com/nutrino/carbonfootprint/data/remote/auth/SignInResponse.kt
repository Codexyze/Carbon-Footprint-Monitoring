package com.nutrino.carbonfootprint.data.remote.auth

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SignInResponse(
    @SerialName("user_id")
    val userId: Int
)