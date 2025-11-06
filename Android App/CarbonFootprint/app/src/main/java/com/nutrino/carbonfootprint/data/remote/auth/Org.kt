package com.nutrino.carbonfootprint.data.remote.auth


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Org(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("plan")
    val plan: String = ""
)