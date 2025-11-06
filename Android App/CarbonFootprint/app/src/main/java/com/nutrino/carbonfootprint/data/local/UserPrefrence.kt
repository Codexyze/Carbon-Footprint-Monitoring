package com.nutrino.carbonfootprint.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore("user_session_store")

class UserPrefrence @Inject constructor(private val context: Context) {

    private val user_id_key = intPreferencesKey("user_id")

    val userId: Flow<Int?> = context.dataStore.data.map {
        it[user_id_key]
    }

    suspend fun updateUserId(userId: Int) {
        context.dataStore.edit {
            it[user_id_key] = userId
        }
    }

    suspend fun clearUserId() {
        context.dataStore.edit {
            it.remove(user_id_key)
        }
    }

    suspend fun clearSession() {
        clearUserId()
    }

    // Check if user is logged in (userId exists)
    suspend fun isLoggedIn(): Boolean {
        var hasUserId = false
        context.dataStore.data.collect { preferences ->
            val userId = preferences[user_id_key]
            hasUserId = userId != null
            return@collect
        }
        return hasUserId
    }
}