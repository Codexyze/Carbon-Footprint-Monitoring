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
import com.nutrino.carbonfootprint.data.remote.emissions.EmissionResponse
import com.nutrino.carbonfootprint.data.remote.emissions.RecomputeEmissionsRequest
import com.nutrino.carbonfootprint.data.remote.emissions.RecomputeEmissionsResponse
import com.nutrino.carbonfootprint.data.state.ResultState
import com.nutrino.carbonfootprint.domain.repository.EmissionsRepository
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

class EmissionsRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val userPrefrence: UserPrefrence
) : EmissionsRepository {

    override suspend fun getEmissions(
        from: String?,
        to: String?,
        limit: Int?,
        offset: Int?
    ): Flow<ResultState<List<EmissionResponse>>> = flow {
        logRepositoryCall("EmissionsRepository", "getEmissions", mapOf("from" to from, "to" to to, "limit" to limit, "offset" to offset))
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.acessToken.first()
            val url = Constants.BASE_URL + Constants.EMISSIONS

            // Log request details
            val headers = mapOf("Authorization" to "Bearer $token")
            val parameters = buildMap<String, Any?> {
                from?.let { put("from", it) }
                to?.let { put("to", it) }
                limit?.let { put("limit", it) }
                offset?.let { put("offset", it) }
            }
            logHttpRequest("GET", url, headers, parameters)

            val userId = userPrefrence.userId.first()

            val response = httpClient.get(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
                userId?.let { parameter("user_id", it) }
                from?.let { parameter("from", it) }
                to?.let { parameter("to", it) }
                limit?.let { parameter("limit", it) }
                offset?.let { parameter("offset", it) }
            }

            // Log response details
            val responseBody = response.bodyAsText()
            logHttpResponse(url, response.status.value, responseBody, response.headers["Content-Type"])

            val emissionsResponse = response.body<List<EmissionResponse>>()
            logRepositoryResult("EmissionsRepository", "getEmissions", true, "Retrieved ${emissionsResponse.size} emissions")
            emit(ResultState.Success(emissionsResponse))
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            logHttpError(Constants.BASE_URL + Constants.EMISSIONS, e)
            logRepositoryResult("EmissionsRepository", "getEmissions", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }

    override suspend fun recomputeEmissions(request: RecomputeEmissionsRequest): Flow<ResultState<RecomputeEmissionsResponse>> = flow {
        logRepositoryCall("EmissionsRepository", "recomputeEmissions", mapOf("request" to request))
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.acessToken.first()
            val url = Constants.BASE_URL + Constants.EMISSIONS_RECOMPUTE

            // Log request details
            val headers = mapOf("Authorization" to "Bearer $token", "Content-Type" to "application/json")
            logHttpRequest("POST", url, headers, request)

            val userId = userPrefrence.userId.first()

            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
                userId?.let { parameter("user_id", it) }
                setBody(request)
            }

            // Log response details
            val responseBody = response.bodyAsText()
            logHttpResponse(url, response.status.value, responseBody, response.headers["Content-Type"])

            val recomputeResponse = response.body<RecomputeEmissionsResponse>()
            logRepositoryResult("EmissionsRepository", "recomputeEmissions", true, recomputeResponse)
            emit(ResultState.Success(recomputeResponse))
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            logHttpError(Constants.BASE_URL + Constants.EMISSIONS_RECOMPUTE, e)
            logRepositoryResult("EmissionsRepository", "recomputeEmissions", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }
}
