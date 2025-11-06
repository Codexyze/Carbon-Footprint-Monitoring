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
import com.nutrino.carbonfootprint.data.remote.ingestion.IngestEventsRequest
import com.nutrino.carbonfootprint.data.remote.ingestion.IngestEventsResponse
import com.nutrino.carbonfootprint.data.state.ResultState
import com.nutrino.carbonfootprint.domain.repository.IngestionRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
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
import java.io.File
import javax.inject.Inject

class IngestionRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val userPrefrence: UserPrefrence
) : IngestionRepository {

    override suspend fun ingestEvents(eventsRequest: IngestEventsRequest): Flow<ResultState<IngestEventsResponse>> = flow {
        logRepositoryCall("IngestionRepository", "ingestEvents", mapOf("eventsCount" to eventsRequest.events.size))
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.acessToken.first()
            val userId = userPrefrence.userId.first()
            val url = Constants.BASE_URL + Constants.INGEST_EVENTS

            // Log request details
            val headers = mapOf("Authorization" to "Bearer $token", "Content-Type" to "application/json")
            val parameters = userId?.let { mapOf("user_id" to it) } ?: emptyMap()
            logHttpRequest("POST", url, headers, eventsRequest)

            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
                userId?.let { parameter("user_id", it) }
                setBody(eventsRequest)
            }

            // Log response details
            val responseBody = response.bodyAsText()
            logHttpResponse(url, response.status.value, responseBody, response.headers["Content-Type"])

            val ingestResponse = response.body<IngestEventsResponse>()
            logRepositoryResult("IngestionRepository", "ingestEvents", true, ingestResponse)
            emit(ResultState.Success(ingestResponse))
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            logHttpError(Constants.BASE_URL + Constants.INGEST_EVENTS, e)
            logRepositoryResult("IngestionRepository", "ingestEvents", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }

    override suspend fun uploadCsv(csvFile: File): Flow<ResultState<IngestEventsResponse>> = flow {
        logRepositoryCall("IngestionRepository", "uploadCsv", mapOf("fileName" to csvFile.name, "fileSize" to csvFile.length()))
        emit(ResultState.Loading)
        try {
            val token = userPrefrence.acessToken.first()
            val userId = userPrefrence.userId.first()
            val url = Constants.BASE_URL + Constants.UPLOAD_CSV

            // Log request details
            val headers = mapOf("Authorization" to "Bearer $token")
            val parameters = userId?.let { mapOf("user_id" to it) } ?: emptyMap()
            logHttpRequest("POST", url, headers, mapOf("file" to csvFile.name, "fileSize" to "${csvFile.length()} bytes"))

            val response = httpClient.post(url) {
                headers {
                    append("Authorization", "Bearer $token")
                }
                userId?.let { parameter("user_id", it) }
                setBody(MultiPartFormDataContent(
                    formData {
                        append("file", csvFile.readBytes(), io.ktor.http.Headers.build {
                            append(io.ktor.http.HttpHeaders.ContentType, "text/csv")
                            append(io.ktor.http.HttpHeaders.ContentDisposition, "filename=\"${csvFile.name}\"")
                        })
                    }
                ))
            }

            // Log response details
            val responseBody = response.bodyAsText()
            logHttpResponse(url, response.status.value, responseBody, response.headers["Content-Type"])

            val uploadResponse = response.body<IngestEventsResponse>()
            logRepositoryResult("IngestionRepository", "uploadCsv", true, uploadResponse)
            emit(ResultState.Success(uploadResponse))
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            logHttpError(Constants.BASE_URL + Constants.UPLOAD_CSV, e)
            logRepositoryResult("IngestionRepository", "uploadCsv", false, error = errorMessage)
            emit(ResultState.Error(errorMessage))
        }
    }
}
