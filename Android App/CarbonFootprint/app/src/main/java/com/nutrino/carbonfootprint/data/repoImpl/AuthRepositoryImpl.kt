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
import com.nutrino.carbonfootprint.data.remote.auth.SignInRequest
import com.nutrino.carbonfootprint.data.remote.auth.SignInResponse
import com.nutrino.carbonfootprint.data.remote.auth.SignUpRequest
import com.nutrino.carbonfootprint.data.remote.auth.SignUpResponse
import com.nutrino.carbonfootprint.data.state.ResultState
import com.nutrino.carbonfootprint.domain.repository.AuthRepository
import com.nutrino.carbonfootprint.domain.repository.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val userPrefrence: UserPrefrence,
    private val httpClient: HttpClient,
    private val userRepository: UserRepository
) : AuthRepository {
    override suspend fun signUp(signUpRequest: SignUpRequest): Flow<ResultState<SignUpResponse>> = flow {
        logRepositoryCall("AuthRepository", "signUp", mapOf("email" to signUpRequest.email, "orgId" to signUpRequest.org_name))
        emit(ResultState.Loading)
        try {
            val url = Constants.BASE_URL + Constants.SIGN_UP

            // Log request details
            logHttpRequest("POST", url, mapOf("Content-Type" to "application/json"), signUpRequest)

            val httpResponse = httpClient.post(url) {
                this.setBody(signUpRequest)
                this.contentType(ContentType.Application.Json)
            }

            // Log response details
            val responseBody = httpResponse.bodyAsText()
            logHttpResponse(url, httpResponse.status.value, responseBody, httpResponse.headers["Content-Type"])

            if (httpResponse.status.isSuccess()) {
                val response = httpResponse.body<SignUpResponse>()

                // Save user_id directly from signup response
                userPrefrence.saveUserId(response.userId)
                debugLogs("User ID saved from signup: ${response.userId}", "AuthRepository")

                // Store a dummy token since server doesn't provide access_token but app expects it
                userPrefrence.updateAccessToken("user_authenticated_${response.userId}")
                debugLogs("Authentication token stored successfully", "AuthRepository")

                logRepositoryResult("AuthRepository", "signUp", true, response)
                emit(ResultState.Success(response))
            } else {
                val errorBody = try {
                    httpResponse.body<String>()
                } catch (e: Exception) {
                    "HTTP ${httpResponse.status.value}"
                }
                val errorMessage = "Server error: ${httpResponse.status.value} - $errorBody"
                logRepositoryResult("AuthRepository", "signUp", false, error = errorMessage)
                emit(ResultState.Error(errorMessage))
            }

        } catch (e: Exception){
            val errorMessage = e.message ?: "Unknown error occurred"
            logHttpError(Constants.BASE_URL + Constants.SIGN_UP, e)
            logRepositoryResult("AuthRepository", "signUp", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }

    override suspend fun signIn(signInRequest: SignInRequest): Flow<ResultState<SignInResponse>> = flow {
        logRepositoryCall("AuthRepository", "signIn", mapOf("email" to signInRequest.email))
        emit(ResultState.Loading)
        try {
            val url = Constants.BASE_URL + Constants.SIGN_IN

            // Log request details (mask password)
            val maskedRequest = signInRequest.copy(password = "****")
            logHttpRequest("POST", url, mapOf("Content-Type" to "application/json"), maskedRequest)

            val httpResponse = httpClient.post(url) {
                this.setBody(signInRequest)
                this.contentType(ContentType.Application.Json)
            }

            // Log response details
            val responseBody = httpResponse.bodyAsText()
            logHttpResponse(url, httpResponse.status.value, responseBody, httpResponse.headers["Content-Type"])

            if (httpResponse.status.isSuccess()) {
                val response = httpResponse.body<SignInResponse>()

                // Save user_id directly from signin response
                response.userId?.let { userId ->
                    userPrefrence.saveUserId(userId)
                    debugLogs("User ID saved from signin: $userId", "AuthRepository")

                    // Store a dummy token since server doesn't provide access_token but app expects it
                    userPrefrence.updateAccessToken("user_authenticated_$userId")
                    debugLogs("Authentication token stored successfully", "AuthRepository")
                }

                logRepositoryResult("AuthRepository", "signIn", true, response)
                emit(ResultState.Success(response))
            } else {
                val errorBody = try {
                    httpResponse.body<String>()
                } catch (e: Exception) {
                    "HTTP ${httpResponse.status.value}"
                }
                val errorMessage = "Server error: ${httpResponse.status.value} - $errorBody"
                logRepositoryResult("AuthRepository", "signIn", false, error = errorMessage)
                emit(ResultState.Error(errorMessage))
            }

        } catch (e: Exception){
            val errorMessage = e.message ?: "Unknown error occurred"
            logHttpError(Constants.BASE_URL + Constants.SIGN_IN, e)
            logRepositoryResult("AuthRepository", "signIn", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }
}