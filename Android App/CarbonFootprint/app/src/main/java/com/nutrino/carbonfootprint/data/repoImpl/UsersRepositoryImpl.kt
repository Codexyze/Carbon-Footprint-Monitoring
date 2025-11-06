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
import com.nutrino.carbonfootprint.data.remote.users.CreateUserRequest
import com.nutrino.carbonfootprint.data.remote.users.UserResponse
import com.nutrino.carbonfootprint.data.state.ResultState
import com.nutrino.carbonfootprint.domain.repository.UsersRepository
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

class UsersRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val userPrefrence: UserPrefrence
) : UsersRepository {

    override suspend fun createUser(userRequest: CreateUserRequest): Flow<ResultState<UserResponse>> = flow {
        logRepositoryCall("UsersRepository", "createUser", mapOf("request" to userRequest))
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.acessToken.first()
            val userId = userPrefrence.userId.first()
            val url = Constants.BASE_URL + Constants.USERS

            // Log request details
            val headers = mapOf("Authorization" to "Bearer $token", "Content-Type" to "application/json")
            logHttpRequest("POST", url, headers, userRequest)

            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
                userId?.let { parameter("user_id", it) }
                setBody(userRequest)
            }

            // Log response details
            val responseBody = response.bodyAsText()
            logHttpResponse(url, response.status.value, responseBody, response.headers["Content-Type"])

            val userResponse = response.body<UserResponse>()
            logRepositoryResult("UsersRepository", "createUser", true, userResponse)
            emit(ResultState.Success(userResponse))
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            logHttpError(Constants.BASE_URL + Constants.USERS, e)
            logRepositoryResult("UsersRepository", "createUser", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }

    override suspend fun getUsers(): Flow<ResultState<List<UserResponse>>> = flow {
        logRepositoryCall("UsersRepository", "getUsers")
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.acessToken.first()
            val userId = userPrefrence.userId.first()
            val url = Constants.BASE_URL + Constants.USERS

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

            val usersResponse = response.body<List<UserResponse>>()
            logRepositoryResult("UsersRepository", "getUsers", true, "Retrieved ${usersResponse.size} users")
            emit(ResultState.Success(usersResponse))
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            logHttpError(Constants.BASE_URL + Constants.USERS, e)
            logRepositoryResult("UsersRepository", "getUsers", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }
}
