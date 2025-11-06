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
import com.nutrino.carbonfootprint.data.remote.auth.GetMeResponse
import com.nutrino.carbonfootprint.data.state.ResultState
import com.nutrino.carbonfootprint.domain.repository.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val userPrefrence: UserPrefrence
) : UserRepository {
    override suspend fun getMe(): Flow<ResultState<GetMeResponse>> = flow {
        logRepositoryCall("UserRepository", "getMe")
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.accessToken.first()
            if (token.isNullOrEmpty()) {
                val errorMessage = "User not logged in"
                errorLogs(errorMessage, null, "UserRepository")
                logRepositoryResult("UserRepository", "getMe", false, error = errorMessage)
                emit(ResultState.Error(errorMessage))
                return@flow
            }

            val userId = userPrefrence.userId.first()
            val url = Constants.BASE_URL + Constants.GET_ME

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
                val response = httpResponse.body<GetMeResponse>()
                logRepositoryResult("UserRepository", "getMe", true, response)
                emit(ResultState.Success(response))
            } else {
                val errorBody = try {
                    httpResponse.body<String>()
                } catch (e: Exception) {
                    "HTTP ${httpResponse.status.value}"
                }
                val errorMessage = "Server error: ${httpResponse.status.value} - $errorBody"
                logRepositoryResult("UserRepository", "getMe", false, error = errorMessage)
                emit(ResultState.Error(errorMessage))
            }

        } catch (e: Exception){
            val errorMessage = e.message ?: "Unknown error occurred"
            logHttpError(Constants.BASE_URL + Constants.GET_ME, e)
            logRepositoryResult("UserRepository", "getMe", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }
}
