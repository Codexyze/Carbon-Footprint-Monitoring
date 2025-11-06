package com.nutrino.carbonfootprint.data.repoImpl

import com.nutrino.carbonfootprint.constants.Constants
import com.nutrino.carbonfootprint.data.local.UserPrefrence
import com.nutrino.carbonfootprint.data.logs.debugLogs
import com.nutrino.carbonfootprint.data.remote.auth.GetMeResponse
import com.nutrino.carbonfootprint.data.state.ResultState
import com.nutrino.carbonfootprint.domain.repository.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
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
        emit(ResultState.Loading)
        try {
            val userId = userPrefrence.userId.first()
            if (userId == null) {
                emit(ResultState.Error("User not logged in"))
                return@flow
            }

            val httpResponse = httpClient.get(Constants.BASE_URL + Constants.GET_ME) {
                parameter("user_id", userId)
            }

            if (httpResponse.status.isSuccess()) {
                val response = httpResponse.body<GetMeResponse>()
                emit(ResultState.Success(response))
            } else {
                val errorBody = try {
                    httpResponse.body<String>()
                } catch (e: Exception) {
                    "HTTP ${httpResponse.status.value}"
                }
                debugLogs(
                    constant = Constants.BASE_URL + Constants.GET_ME,
                    e = Exception("HTTP ${httpResponse.status.value}: $errorBody")
                )
                emit(ResultState.Error("Server error: ${httpResponse.status.value} - $errorBody"))
            }

        } catch (e: Exception){
            debugLogs(
                constant = Constants.BASE_URL + Constants.GET_ME,
                e = e
            )
            emit(ResultState.Error(e.message ?: "Unknown error occurred"))
        }
    }
}
