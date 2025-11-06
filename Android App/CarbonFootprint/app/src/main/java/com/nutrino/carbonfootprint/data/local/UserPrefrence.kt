package com.nutrino.carbonfootprint.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore("user_session_store")

class UserPrefrence @Inject constructor(private val context: Context) {

    private val access_token = stringPreferencesKey("access_token")
    private val user_id = intPreferencesKey("user_id")

    val accessToken: Flow<String?> = context.dataStore.data.map {
        it[access_token]
    }

    // Legacy support for existing code
    val acessToken: Flow<String?> = accessToken

    val userId: Flow<Int?> = context.dataStore.data.map {
        it[user_id]
    }

    suspend fun updateAccessToken(token: String) {
        context.dataStore.edit {
            it[access_token] = token
        }
    }

    suspend fun saveUserId(id: Int) {
        context.dataStore.edit {
            it[user_id] = id
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit {
            it.remove(access_token)
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit {
            it.remove(access_token)
            it.remove(user_id)
        }
    }

    // Check if user is logged in (token exists and not empty)
    suspend fun isLoggedIn(): Boolean {
        var hasToken = false
        context.dataStore.data.collect { preferences ->
            val token = preferences[access_token]
            hasToken = !token.isNullOrEmpty()
            return@collect
        }
        return hasToken
    }
}