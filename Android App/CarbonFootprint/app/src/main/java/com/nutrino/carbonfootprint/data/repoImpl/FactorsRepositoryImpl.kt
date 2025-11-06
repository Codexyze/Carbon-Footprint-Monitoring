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
import com.nutrino.carbonfootprint.data.remote.factors.CreateFactorRequest
import com.nutrino.carbonfootprint.data.remote.factors.FactorResponse
import com.nutrino.carbonfootprint.data.remote.factors.PreviewFactorResponse
import com.nutrino.carbonfootprint.data.state.ResultState
import com.nutrino.carbonfootprint.domain.repository.FactorsRepository
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

class FactorsRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val userPrefrence: UserPrefrence
) : FactorsRepository {

    override suspend fun createFactor(factorRequest: CreateFactorRequest): Flow<ResultState<FactorResponse>> = flow {
        logRepositoryCall("FactorsRepository", "createFactor", mapOf("category" to factorRequest.category, "geography" to factorRequest.geography))
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.acessToken.first()
            val url = Constants.BASE_URL + Constants.FACTORS

            // Log request details
            val headers = mapOf("Authorization" to "Bearer $token", "Content-Type" to "application/json")
            logHttpRequest("POST", url, headers, factorRequest)

            val userId = userPrefrence.userId.first()

            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
                userId?.let { parameter("user_id", it) }
                setBody(factorRequest)
            }

            // Log response details
            val responseBody = response.bodyAsText()
            logHttpResponse(url, response.status.value, responseBody, response.headers["Content-Type"])

            val factorResponse = response.body<FactorResponse>()
            logRepositoryResult("FactorsRepository", "createFactor", true, factorResponse)
            emit(ResultState.Success(factorResponse))
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            logHttpError(Constants.BASE_URL + Constants.FACTORS, e)
            logRepositoryResult("FactorsRepository", "createFactor", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }

    override suspend fun getFactors(
        category: String?,
        geography: String?,
        validOn: String?
    ): Flow<ResultState<List<FactorResponse>>> = flow {
        logRepositoryCall("FactorsRepository", "getFactors", mapOf("category" to category, "geography" to geography, "validOn" to validOn))
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.acessToken.first()
            val url = Constants.BASE_URL + Constants.FACTORS

            // Log request details
            val headers = mapOf("Authorization" to "Bearer $token")
            val parameters = buildMap<String, Any?> {
                category?.let { put("category", it) }
                geography?.let { put("geography", it) }
                validOn?.let { put("valid_on", it) }
            }
            logHttpRequest("GET", url, headers, parameters)

            val userId = userPrefrence.userId.first()

            val response = httpClient.get(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
                userId?.let { parameter("user_id", it) }
                category?.let { parameter("category", it) }
                geography?.let { parameter("geography", it) }
                validOn?.let { parameter("valid_on", it) }
            }

            // Log response details
            val responseBody = response.bodyAsText()
            logHttpResponse(url, response.status.value, responseBody, response.headers["Content-Type"])

            val factorsResponse = response.body<List<FactorResponse>>()
            logRepositoryResult("FactorsRepository", "getFactors", true, "Retrieved ${factorsResponse.size} factors")
            emit(ResultState.Success(factorsResponse))
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            logHttpError(Constants.BASE_URL + Constants.FACTORS, e)
            logRepositoryResult("FactorsRepository", "getFactors", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }

    override suspend fun previewFactor(
        category: String,
        occurredAt: String,
        geography: String
    ): Flow<ResultState<PreviewFactorResponse>> = flow {
        logRepositoryCall("FactorsRepository", "previewFactor", mapOf("category" to category, "occurredAt" to occurredAt, "geography" to geography))
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.acessToken.first()
            val url = Constants.BASE_URL + Constants.FACTORS_PREVIEW

            // Log request details
            val headers = mapOf("Authorization" to "Bearer $token")
            val parameters = mapOf("category" to category, "occurred_at" to occurredAt, "geography" to geography)
            logHttpRequest("GET", url, headers, parameters)

            val userId = userPrefrence.userId.first()

            val response = httpClient.get(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
                userId?.let { parameter("user_id", it) }
                parameter("category", category)
                parameter("occurred_at", occurredAt)
                parameter("geography", geography)
            }

            // Log response details
            val responseBody = response.bodyAsText()
            logHttpResponse(url, response.status.value, responseBody, response.headers["Content-Type"])

            val previewResponse = response.body<PreviewFactorResponse>()
            logRepositoryResult("FactorsRepository", "previewFactor", true, previewResponse)
            emit(ResultState.Success(previewResponse))
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            logHttpError(Constants.BASE_URL + Constants.FACTORS_PREVIEW, e)
            logRepositoryResult("FactorsRepository", "previewFactor", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }
}
