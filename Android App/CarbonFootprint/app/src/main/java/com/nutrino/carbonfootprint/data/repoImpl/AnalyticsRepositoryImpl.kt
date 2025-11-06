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
import com.nutrino.carbonfootprint.data.remote.analytics.KpisResponse
import com.nutrino.carbonfootprint.data.remote.analytics.SuggestionResponse
import com.nutrino.carbonfootprint.data.remote.analytics.SummaryResponse
import com.nutrino.carbonfootprint.data.remote.analytics.TrendResponse
import com.nutrino.carbonfootprint.data.state.ResultState
import com.nutrino.carbonfootprint.domain.repository.AnalyticsRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val userPrefrence: UserPrefrence
) : AnalyticsRepository {

    override suspend fun getKpis(from: String?, to: String?): Flow<ResultState<KpisResponse>> = flow {
        logRepositoryCall("AnalyticsRepository", "getKpis", mapOf("from" to from, "to" to to))
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.acessToken.first()
            val userId = userPrefrence.userId.first()
            val url = Constants.BASE_URL + Constants.ANALYTICS_KPIS

            // Log request details
            val headers = mapOf("Authorization" to "Bearer $token")
            val parameters = buildMap<String, Any?> {
                userId?.let { put("user_id", it) }
                from?.let { put("from", it) }
                to?.let { put("to", it) }
            }
            logHttpRequest("GET", url, headers, parameters)

            val response = httpClient.get(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
                userId?.let { parameter("user_id", it) }
                from?.let { parameter("from", it) }
                to?.let { parameter("to", it) }
            }

            // Log response details
            val responseBody = response.bodyAsText()
            logHttpResponse(url, response.status.value, responseBody, response.headers["Content-Type"])

            val kpisResponse = response.body<KpisResponse>()
            logRepositoryResult("AnalyticsRepository", "getKpis", true, kpisResponse)
            emit(ResultState.Success(kpisResponse))
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            logHttpError(Constants.BASE_URL + Constants.ANALYTICS_KPIS, e)
            logRepositoryResult("AnalyticsRepository", "getKpis", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }

    override suspend fun getTrend(from: String?, to: String?, grain: String?): Flow<ResultState<TrendResponse>> = flow {
        logRepositoryCall("AnalyticsRepository", "getTrend", mapOf("from" to from, "to" to to, "grain" to grain))
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.acessToken.first()
            val userId = userPrefrence.userId.first()
            val url = Constants.BASE_URL + Constants.ANALYTICS_TREND

            // Log request details
            val headers = mapOf("Authorization" to "Bearer $token")
            val parameters = buildMap<String, Any?> {
                userId?.let { put("user_id", it) }
                from?.let { put("from", it) }
                to?.let { put("to", it) }
                grain?.let { put("grain", it) }
            }
            logHttpRequest("GET", url, headers, parameters)

            val response = httpClient.get(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
                userId?.let { parameter("user_id", it) }
                from?.let { parameter("from", it) }
                to?.let { parameter("to", it) }
                grain?.let { parameter("grain", it) }
            }

            // Log response details
            val responseBody = response.bodyAsText()
            logHttpResponse(url, response.status.value, responseBody, response.headers["Content-Type"])

            val trendResponse = response.body<TrendResponse>()
            logRepositoryResult("AnalyticsRepository", "getTrend", true, trendResponse)
            emit(ResultState.Success(trendResponse))
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            logHttpError(Constants.BASE_URL + Constants.ANALYTICS_TREND, e)
            logRepositoryResult("AnalyticsRepository", "getTrend", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }

    override suspend fun getSummary(): Flow<ResultState<SummaryResponse>> = flow {
        logRepositoryCall("AnalyticsRepository", "getSummary")
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.acessToken.first()
            val userId = userPrefrence.userId.first()
            val url = Constants.BASE_URL + Constants.ANALYTICS_SUMMARY

            // Log request details
            val headers = mapOf("Authorization" to "Bearer $token")
            val parameters = userId?.let { mapOf("user_id" to it, "id" to it) } ?: emptyMap()
            logHttpRequest("GET", url, headers, parameters)

            val response = httpClient.get(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
                userId?.let {
                    parameter("user_id", it)
                    parameter("id", it) // Server requires both user_id and id parameters
                }
            }

            // Log response details
            val responseBody = response.bodyAsText()
            logHttpResponse(url, response.status.value, responseBody, response.headers["Content-Type"])

            val summaryResponse = response.body<SummaryResponse>()
            logRepositoryResult("AnalyticsRepository", "getSummary", true, summaryResponse)
            emit(ResultState.Success(summaryResponse))
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            logHttpError(Constants.BASE_URL + Constants.ANALYTICS_SUMMARY, e)
            logRepositoryResult("AnalyticsRepository", "getSummary", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }

    override suspend fun getSuggestion(id: Int, userId: Int): Flow<ResultState<SuggestionResponse>> = flow {
        logRepositoryCall("AnalyticsRepository", "getSuggestion", mapOf("id" to id, "userId" to userId))
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.acessToken.first()
            val url = Constants.BASE_URL + Constants.ANALYTICS_SUGGESTION

            // Log request details
            val headers = mapOf("Authorization" to "Bearer $token")
            val parameters = mapOf("id" to id, "user_id" to userId)
            logHttpRequest("GET", url, headers, parameters)

            val response = httpClient.get(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
                parameter("id", id)
                parameter("user_id", userId)
            }

            // Log response details
            val responseBody = response.bodyAsText()
            logHttpResponse(url, response.status.value, responseBody, response.headers["Content-Type"])

            val suggestionResponse = response.body<SuggestionResponse>()
            logRepositoryResult("AnalyticsRepository", "getSuggestion", true, suggestionResponse)
            emit(ResultState.Success(suggestionResponse))
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            logHttpError(Constants.BASE_URL + Constants.ANALYTICS_SUGGESTION, e)
            logRepositoryResult("AnalyticsRepository", "getSuggestion", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }
}
