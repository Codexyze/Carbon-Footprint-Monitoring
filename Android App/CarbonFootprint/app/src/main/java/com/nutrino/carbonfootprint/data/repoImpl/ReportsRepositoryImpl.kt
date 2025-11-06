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
import com.nutrino.carbonfootprint.data.state.ResultState
import com.nutrino.carbonfootprint.domain.repository.ReportsRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ReportsRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val userPrefrence: UserPrefrence
) : ReportsRepository {

    override suspend fun exportReport(from: String, to: String): Flow<ResultState<HttpResponse>> = flow {
        logRepositoryCall("ReportsRepository", "exportReport", mapOf("from" to from, "to" to to))
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.acessToken.first()
            val url = Constants.BASE_URL + Constants.REPORTS_PERIOD

            // Log request details
            val headers = mapOf("Authorization" to "Bearer $token")
            val parameters = mapOf("from" to from, "to" to to)
            logHttpRequest("GET", url, headers, parameters)

            val userId = userPrefrence.userId.first()

            val response = httpClient.get(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
                userId?.let { parameter("user_id", it) }
                parameter("from", from)
                parameter("to", to)
            }

            // Log response details
            val responseBody = response.bodyAsText()
            logHttpResponse(url, response.status.value, responseBody, response.headers["Content-Type"])

            logRepositoryResult("ReportsRepository", "exportReport", true, "Report exported successfully")
            emit(ResultState.Success(response))
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            logHttpError(Constants.BASE_URL + Constants.REPORTS_PERIOD, e)
            logRepositoryResult("ReportsRepository", "exportReport", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }
}
