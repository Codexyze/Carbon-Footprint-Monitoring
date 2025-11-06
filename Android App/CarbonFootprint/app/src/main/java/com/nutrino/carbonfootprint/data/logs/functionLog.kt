package com.nutrino.carbonfootprint.data.logs

import android.util.Log
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentType

const val TAG = "CarbonFootprint"

fun debugLogs(message: String, tag: String = TAG) {
    Log.d(tag, message)
}

fun errorLogs(message: String, throwable: Throwable? = null, tag: String = TAG) {
    Log.e(tag, message, throwable)
}

fun infoLogs(message: String, tag: String = TAG) {
    Log.i(tag, message)
}

fun warnLogs(message: String, tag: String = TAG) {
    Log.w(tag, message)
}

// Network logging utilities
fun logHttpRequest(method: String, url: String, headers: Map<String, String>? = null, body: Any? = null, tag: String = "Network") {
    val logMessage = buildString {
        appendLine("🌐 HTTP REQUEST:")
        appendLine("Method: $method")
        appendLine("URL: $url")
        headers?.let {
            appendLine("Headers:")
            it.forEach { (key, value) ->
                // Mask sensitive headers
                val maskedValue = if (key.lowercase().contains("authorization") || key.lowercase().contains("token")) {
                    value.take(10) + "***"
                } else value
                appendLine("  $key: $maskedValue")
            }
        }
        body?.let {
            appendLine("Body: $it")
        }
    }
    debugLogs(logMessage, tag)
}

fun logHttpResponse(url: String, statusCode: Int, responseBody: String? = null, contentType: String? = null, tag: String = "Network") {
    val logMessage = buildString {
        appendLine("📡 HTTP RESPONSE:")
        appendLine("URL: $url")
        appendLine("Status Code: $statusCode")
        contentType?.let {
            appendLine("Content-Type: $it")
        }
        responseBody?.let {
            appendLine("Response Body: $it")
        }
    }
    debugLogs(logMessage, tag)
}

fun logHttpError(url: String, error: Throwable, tag: String = "Network") {
    val logMessage = buildString {
        appendLine("❌ HTTP ERROR:")
        appendLine("URL: $url")
        appendLine("Error: ${error.message}")
        appendLine("Exception: ${error.javaClass.simpleName}")
    }
    errorLogs(logMessage, error, tag)
}

suspend fun HttpResponse.logResponse(url: String) {
    try {
        val responseBody = this.bodyAsText()
        logHttpResponse(
            url = url,
            statusCode = this.status.value,
            responseBody = responseBody,
            contentType = this.contentType().toString()
        )
    } catch (e: Exception) {
        logHttpError(url, e)
    }
}

// Repository logging utilities
fun logRepositoryCall(repositoryName: String, methodName: String, parameters: Map<String, Any?> = emptyMap()) {
    val logMessage = buildString {
        appendLine("📋 REPOSITORY CALL:")
        appendLine("Repository: $repositoryName")
        appendLine("Method: $methodName")
        if (parameters.isNotEmpty()) {
            appendLine("Parameters:")
            parameters.forEach { (key, value) ->
                appendLine("  $key: $value")
            }
        }
    }
    debugLogs(logMessage, "Repository")
}

fun logRepositoryResult(repositoryName: String, methodName: String, success: Boolean, result: Any? = null, error: String? = null) {
    val logMessage = buildString {
        appendLine("📊 REPOSITORY RESULT:")
        appendLine("Repository: $repositoryName")
        appendLine("Method: $methodName")
        appendLine("Success: $success")
        if (success) {
            result?.let {
                appendLine("Result: $it")
            }
        } else {
            error?.let {
                appendLine("Error: $it")
            }
        }
    }
    debugLogs(logMessage, "Repository")
}
