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
import com.nutrino.carbonfootprint.domain.repository.FacilitiesRepository
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class FacilitiesRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val userPrefrence: UserPrefrence
) : FacilitiesRepository {

    override suspend fun createFacility(facilityRequest: CreateFacilityRequest): Flow<ResultState<FacilityResponse>> = flow {
        logRepositoryCall("FacilitiesRepository", "createFacility", mapOf("name" to facilityRequest.name, "country" to facilityRequest.country))
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.acessToken.first()
            val userId = userPrefrence.userId.first()
            val url = Constants.BASE_URL + Constants.FACILITIES

            // Log request details
            val headers = mapOf("Authorization" to "Bearer $token", "Content-Type" to "application/json")
            logHttpRequest("POST", url, headers, facilityRequest)

            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
                userId?.let { parameter("user_id", it) }
                setBody(facilityRequest)
            }

            // Log response details
            val responseBody = response.bodyAsText()
            logHttpResponse(url, response.status.value, responseBody, response.headers["Content-Type"])

            val facilityResponse = response.body<FacilityResponse>()
            logRepositoryResult("FacilitiesRepository", "createFacility", true, facilityResponse)
            emit(ResultState.Success(facilityResponse))
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            logHttpError(Constants.BASE_URL + Constants.FACILITIES, e)
            logRepositoryResult("FacilitiesRepository", "createFacility", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }

    override suspend fun getFacilities(): Flow<ResultState<List<FacilityResponse>>> = flow {
        logRepositoryCall("FacilitiesRepository", "getFacilities")
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.acessToken.first()
            val userId = userPrefrence.userId.first()
            val url = Constants.BASE_URL + Constants.FACILITIES

            // Log request details
            val headers = mapOf("Authorization" to "Bearer $token")
            val parameters = userId?.let { mapOf("user_id" to it) } ?: emptyMap()
            logHttpRequest("GET", url, headers, parameters)

            val response = httpClient.get(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
                userId?.let { parameter("user_id", it) }
            }

            // Log response details
            val responseBody = response.bodyAsText()
            logHttpResponse(url, response.status.value, responseBody, response.headers["Content-Type"])

            val facilitiesResponse = response.body<List<FacilityResponse>>()
            logRepositoryResult("FacilitiesRepository", "getFacilities", true, "Retrieved ${facilitiesResponse.size} facilities")
            emit(ResultState.Success(facilitiesResponse))
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            logHttpError(Constants.BASE_URL + Constants.FACILITIES, e)
            logRepositoryResult("FacilitiesRepository", "getFacilities", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }
}
