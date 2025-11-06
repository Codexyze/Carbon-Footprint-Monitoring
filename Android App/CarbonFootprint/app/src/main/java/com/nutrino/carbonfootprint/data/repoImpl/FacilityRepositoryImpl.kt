package com.nutrino.carbonfootprint.data.repoImpl

import com.nutrino.carbonfootprint.constants.Constants
import com.nutrino.carbonfootprint.data.local.UserPrefrence
import com.nutrino.carbonfootprint.data.logs.debugLogs
import com.nutrino.carbonfootprint.data.logs.errorLogs
import com.nutrino.carbonfootprint.data.logs.logHttpError
import com.nutrino.carbonfootprint.data.logs.logHttpRequest
import com.nutrino.carbonfootprint.data.logs.logHttpResponse
import com.nutrino.carbonfootprint.data.logs.logRepositoryCall
import com.nutrino.carbonfootprint.data.logs.logRepositoryResult
import com.nutrino.carbonfootprint.data.remote.facilities.CreateFacilityRequest
import com.nutrino.carbonfootprint.data.remote.facilities.FacilityResponse
import com.nutrino.carbonfootprint.data.state.ResultState
import com.nutrino.carbonfootprint.domain.repository.FacilityRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FacilityRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val userPrefrence: UserPrefrence
) : FacilityRepository {
    override suspend fun getFacilities(): Flow<ResultState<List<FacilityResponse>>> = flow {
        logRepositoryCall("FacilityRepository", "getFacilities")
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.accessToken.first()
            if (token.isNullOrEmpty()) {
                val errorMessage = "User not logged in"
                errorLogs(errorMessage, null, "FacilityRepository")
                logRepositoryResult("FacilityRepository", "getFacilities", false, error = errorMessage)
                emit(ResultState.Error(errorMessage))
                return@flow
            }

            val userId = userPrefrence.userId.first()
            val url = Constants.BASE_URL + Constants.FACILITIES

            // Log request details
            val headers = mapOf("Authorization" to "Bearer $token")
            val parameters = userId?.let { mapOf("user_id" to it) } ?: emptyMap()
            logHttpRequest("GET", url, headers, parameters)

            val httpResponse = httpClient.get(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
                userId?.let { parameter("user_id", it) }
            }

            // Log response details
            val responseBody = httpResponse.bodyAsText()
            logHttpResponse(url, httpResponse.status.value, responseBody, httpResponse.headers["Content-Type"])

            if (httpResponse.status.isSuccess()) {
                val response = httpResponse.body<List<FacilityResponse>>()
                logRepositoryResult("FacilityRepository", "getFacilities", true, "Retrieved ${response.size} facilities")
                emit(ResultState.Success(response))
            } else {
                val errorBody = try {
                    httpResponse.body<String>()
                } catch (e: Exception) {
                    "HTTP ${httpResponse.status.value}"
                }
                val errorMessage = "Server error: ${httpResponse.status.value} - $errorBody"
                logRepositoryResult("FacilityRepository", "getFacilities", false, error = errorMessage)
                emit(ResultState.Error(errorMessage))
            }

        } catch (e: Exception){
            val errorMessage = e.message ?: "Unknown error occurred"
            logHttpError(Constants.BASE_URL + Constants.FACILITIES, e)
            logRepositoryResult("FacilityRepository", "getFacilities", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }

    override suspend fun createFacility(createFacilityRequest: CreateFacilityRequest): Flow<ResultState<FacilityResponse>> = flow {
        logRepositoryCall("FacilityRepository", "createFacility", mapOf("name" to createFacilityRequest.name, "country" to createFacilityRequest.country))
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.accessToken.first()
            if (token.isNullOrEmpty()) {
                val errorMessage = "User not logged in"
                errorLogs(errorMessage, null, "FacilityRepository")
                logRepositoryResult("FacilityRepository", "createFacility", false, error = errorMessage)
                emit(ResultState.Error(errorMessage))
                return@flow
            }

            // Sanitize country to meet API requirement (ISO-2 -> max 2 uppercase chars)
            val sanitizedCountry = createFacilityRequest.country.trim().let { input ->
                when {
                    input.isBlank() -> ""
                    input.equals("Unknown", ignoreCase = true) -> ""
                    input.length == 2 -> input.uppercase()
                    input.length > 2 -> input.take(2).uppercase()
                    else -> input.uppercase()
                }
            }
            val sanitizedRequest = createFacilityRequest.copy(
                country = sanitizedCountry,
                grid_region = createFacilityRequest.grid_region.trim()
            )

            val url = Constants.BASE_URL + Constants.FACILITIES

            // Log request details
            val headers = mapOf("Authorization" to "Bearer $token", "Content-Type" to "application/json")
            logHttpRequest("POST", url, headers, sanitizedRequest)

            val userId = userPrefrence.userId.first()

            val httpResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
                userId?.let { parameter("user_id", it) }
                setBody(sanitizedRequest)
            }

            // Log response details
            val responseBody = httpResponse.bodyAsText()
            logHttpResponse(url, httpResponse.status.value, responseBody, httpResponse.headers["Content-Type"])

            if (httpResponse.status.isSuccess()) {
                val response = httpResponse.body<FacilityResponse>()
                logRepositoryResult("FacilityRepository", "createFacility", true, response)
                emit(ResultState.Success(response))
            } else {
                val errorBody = try {
                    httpResponse.body<String>()
                } catch (e: Exception) {
                    "HTTP ${httpResponse.status.value}"
                }
                val errorMessage = "Server error: ${httpResponse.status.value} - $errorBody"
                logRepositoryResult("FacilityRepository", "createFacility", false, error = errorMessage)
                emit(ResultState.Error(errorMessage))
            }

        } catch (e: Exception){
            val errorMessage = e.message ?: "Unknown error occurred"
            logHttpError(Constants.BASE_URL + Constants.FACILITIES, e)
            logRepositoryResult("FacilityRepository", "createFacility", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }
}
