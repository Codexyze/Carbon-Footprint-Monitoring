# 🔧 ANDROID APP API FIX GUIDE
## Critical Issues Found - All Endpoints Returning 422 Unprocessable Entity

---

## **ROOT CAUSE**
All protected endpoints require a `user_id` query parameter for authentication, but the Android app is NOT sending it.

### Backend Authentication System:
```python
def get_current_user(user_id: Annotated[int, Query(alias="user_id")], db):
    user = db.get(User, user_id)
    if not user:
        raise HTTPException(status_code=401, detail="Invalid user id")
    return user
```

**Every endpoint that uses `require_role()` dependency needs `user_id` as a query parameter.**

---

## 📋 ENDPOINTS THAT NEED FIXING

### ✅ WORKING ENDPOINTS (No Changes Needed)
1. **POST /v1/auth/signup** - ✅ Working
2. **POST /v1/auth/login** - ✅ Should work
3. **GET /v1/auth/me?user_id=1** - ✅ Working (when user_id is provided)

### ❌ BROKEN ENDPOINTS (Need user_id Parameter)

---

## 🔴 1. GET /v1/auth/me

### Current Android Code Location:
`UserRepositoryImpl.kt` (Line 32)

### ❌ Current Implementation:
```kotlin
val httpResponse = httpClient.get(Constants.BASE_URL + Constants.GET_ME) {
    headers {
        append("Authorization", "Bearer $token")
    }
    // ❌ MISSING: user_id parameter
}
```

### ✅ REQUIRED FIX:
```kotlin
val httpResponse = httpClient.get(Constants.BASE_URL + Constants.GET_ME) {
    headers {
        append("Authorization", "Bearer $token")
    }
    // ✅ ADD THIS:
    parameter("user_id", userPrefrence.userId.first())
}
```

### Backend Requirement:
- **Query Parameter**: `user_id` (integer, required)
- **Example**: `GET /v1/auth/me?user_id=17`

---

## 🔴 2. GET /v1/analytics/kpis

### Current Android Code Location:
`AnalyticsRepositoryImpl.kt` (Line 30)

### ❌ Current Implementation:
```kotlin
val response = httpClient.get(Constants.BASE_URL + Constants.ANALYTICS_KPIS) {
    headers {
        append("Authorization", "Bearer $token")
    }
    from?.let { parameter("from", it) }
    to?.let { parameter("to", it) }
    // ❌ MISSING: user_id parameter
}
```

### ✅ REQUIRED FIX:
```kotlin
val response = httpClient.get(Constants.BASE_URL + Constants.ANALYTICS_KPIS) {
    headers {
        append("Authorization", "Bearer $token")
    }
    from?.let { parameter("from", it) }
    to?.let { parameter("to", it) }
    // ✅ ADD THIS:
    parameter("user_id", userPrefrence.userId.first())
}
```

### Backend Requirement:
- **Query Parameters**:
  - `user_id` (integer, required) ← **MISSING**
  - `from` (ISO-8601 datetime string, required)
  - `to` (ISO-8601 datetime string, required)
- **Example**: `GET /v1/analytics/kpis?user_id=17&from=2025-10-07T22:17:34.092363&to=2025-11-06T22:17:34.092363`

---

## 🔴 3. GET /v1/analytics/summary

### Current Android Code Location:
`AnalyticsRepositoryImpl.kt` (Line 65)

### ❌ Current Implementation:
```kotlin
val response = httpClient.get(Constants.BASE_URL + Constants.ANALYTICS_SUMMARY) {
    // ❌ COMMENTED OUT headers!
//  headers {
//      append("Authorization", "Bearer $token")
//  }
    // ❌ MISSING: user_id parameter
    // ❌ MISSING: id parameter
}
```

### ✅ REQUIRED FIX:
```kotlin
val response = httpClient.get(Constants.BASE_URL + Constants.ANALYTICS_SUMMARY) {
    headers {
        append("Authorization", "Bearer $token")
    }
    // ✅ ADD THESE:
    parameter("user_id", userPrefrence.userId.first())
    parameter("id", userPrefrence.orgId.first()) // Organization ID
}
```

### Backend Requirement:
- **Query Parameters**:
  - `user_id` (integer, required) ← **MISSING**
  - `id` (integer, required) ← **MISSING** (Organization ID)
- **Example**: `GET /v1/analytics/summary?user_id=17&id=15`

---

## 🔴 4. GET /v1/tenants/facilities

### Current Android Code Location:
`FacilityRepositoryImpl.kt` (Line 37)

### ❌ Current Implementation:
```kotlin
val httpResponse = httpClient.get(Constants.BASE_URL + Constants.FACILITIES) {
    headers {
        append("Authorization", "Bearer $token")
    }
    // ❌ MISSING: user_id parameter
}
```

### ✅ REQUIRED FIX:
```kotlin
val httpResponse = httpClient.get(Constants.BASE_URL + Constants.FACILITIES) {
    headers {
        append("Authorization", "Bearer $token")
    }
    // ✅ ADD THIS:
    parameter("user_id", userPrefrence.userId.first())
}
```

### Backend Requirement:
- **Query Parameter**: `user_id` (integer, required)
- **Example**: `GET /v1/tenants/facilities?user_id=17`

---

## 🔴 5. GET /v1/analytics/trend

### Current Android Code Location:
`AnalyticsRepositoryImpl.kt` (Line 47)

### ❌ Current Implementation:
```kotlin
val response = httpClient.get(Constants.BASE_URL + Constants.ANALYTICS_TREND) {
    headers {
        append("Authorization", "Bearer $token")
    }
    from?.let { parameter("from", it) }
    to?.let { parameter("to", it) }
    grain?.let { parameter("grain", it) }
    // ❌ MISSING: user_id parameter
}
```

### ✅ REQUIRED FIX:
```kotlin
val response = httpClient.get(Constants.BASE_URL + Constants.ANALYTICS_TREND) {
    headers {
        append("Authorization", "Bearer $token")
    }
    from?.let { parameter("from", it) }
    to?.let { parameter("to", it) }
    grain?.let { parameter("grain", it) }
    // ✅ ADD THIS:
    parameter("user_id", userPrefrence.userId.first())
}
```

### Backend Requirement:
- **Query Parameters**:
  - `user_id` (integer, required) ← **MISSING**
  - `from` (ISO-8601 datetime, required)
  - `to` (ISO-8601 datetime, required)
  - `grain` (string, optional): "day" or "month"

---

## 🔴 6. GET /v1/analytics/suggestion

### Current Android Code Location:
`AnalyticsRepositoryImpl.kt` (Line 119)

### ✅ Current Implementation (Actually Correct!):
```kotlin
val response = httpClient.get(Constants.BASE_URL + Constants.ANALYTICS_SUGGESTION) {
    headers {
        append("Authorization", "Bearer $token")
    }
    parameter("id", id)
    parameter("user_id", userId)
    // ✅ This one is CORRECT!
}
```

**This endpoint already sends user_id correctly! No changes needed here.**

---

## 📝 STEP-BY-STEP FIX INSTRUCTIONS FOR ANDROID STUDIO

### **PREREQUISITE: Store User ID in UserPreference**

First, ensure `userId` is saved when user signs up/logs in:

#### File: `data/local/UserPreference.kt`

Add userId storage if not already present:

```kotlin
// Add this property to UserPreference class
val userId: Flow<Int?>
    get() = dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

suspend fun saveUserId(userId: Int) {
    dataStore.edit { preferences ->
        preferences[USER_ID_KEY] = userId
    }
}

companion object {
    private val USER_ID_KEY = intPreferencesKey("user_id")
    // ... other keys
}
```

#### File: `data/repoImpl/AuthRepositoryImpl.kt`

When user signs up/logs in, save the userId:

```kotlin
// In signup/login methods after getting user_id from response:
userPrefrence.saveUserId(response.user_id)
```

---

### **FIX #1: UserRepositoryImpl.kt**

```kotlin
override suspend fun getMe(): Flow<ResultState<GetMeResponse>> = flow {
    emit(ResultState.Loading)
    try {
        val token = userPrefrence.accessToken.first()
        val userId = userPrefrence.userId.first() // ✅ GET USER ID
        
        if (token.isNullOrEmpty() || userId == null) {
            emit(ResultState.Error("User not logged in"))
            return@flow
        }

        val httpResponse = httpClient.get(Constants.BASE_URL + Constants.GET_ME) {
            headers {
                append("Authorization", "Bearer $token")
            }
            parameter("user_id", userId) // ✅ ADD THIS
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
            emit(ResultState.Error("Server error: ${httpResponse.status.value} - $errorBody"))
        }
    } catch (e: Exception){
        emit(ResultState.Error(e.message ?: "Unknown error occurred"))
    }
}
```

---

### **FIX #2: AnalyticsRepositoryImpl.kt**

Update ALL analytics methods:

```kotlin
override suspend fun getKpis(from: String?, to: String?): Flow<ResultState<KpisResponse>> = flow {
    emit(ResultState.Loading)
    try {
        val token = userPrefrence.acessToken.first()
        val userId = userPrefrence.userId.first() // ✅ GET USER ID
        
        if (token.isNullOrEmpty() || userId == null) {
            emit(ResultState.Error("User not logged in"))
            return@flow
        }

        val response = httpClient.get(Constants.BASE_URL + Constants.ANALYTICS_KPIS) {
            headers {
                append("Authorization", "Bearer $token")
            }
            parameter("user_id", userId) // ✅ ADD THIS
            from?.let { parameter("from", it) }
            to?.let { parameter("to", it) }
        }
        emit(ResultState.Success(response.body<KpisResponse>()))
    } catch (e: Exception) {
        emit(ResultState.Error("Network error: ${e.message}"))
    }
}

override suspend fun getTrend(from: String?, to: String?, grain: String?): Flow<ResultState<TrendResponse>> = flow {
    emit(ResultState.Loading)
    try {
        val token = userPrefrence.acessToken.first()
        val userId = userPrefrence.userId.first() // ✅ GET USER ID
        
        if (token.isNullOrEmpty() || userId == null) {
            emit(ResultState.Error("User not logged in"))
            return@flow
        }

        val response = httpClient.get(Constants.BASE_URL + Constants.ANALYTICS_TREND) {
            headers {
                append("Authorization", "Bearer $token")
            }
            parameter("user_id", userId) // ✅ ADD THIS
            from?.let { parameter("from", it) }
            to?.let { parameter("to", it) }
            grain?.let { parameter("grain", it) }
        }
        emit(ResultState.Success(response.body<TrendResponse>()))
    } catch (e: Exception) {
        emit(ResultState.Error("Network error: ${e.message}"))
    }
}

override suspend fun getSummary(): Flow<ResultState<SummaryResponse>> = flow {
    emit(ResultState.Loading)
    try {
        val token = userPrefrence.acessToken.first()
        val userId = userPrefrence.userId.first() // ✅ GET USER ID
        val orgId = userPrefrence.orgId.first() // ✅ GET ORG ID (from /auth/me response)
        
        if (token.isNullOrEmpty() || userId == null || orgId == null) {
            emit(ResultState.Error("User not logged in"))
            return@flow
        }

        val response = httpClient.get(Constants.BASE_URL + Constants.ANALYTICS_SUMMARY) {
            headers {
                append("Authorization", "Bearer $token")
            }
            parameter("user_id", userId) // ✅ ADD THIS
            parameter("id", orgId) // ✅ ADD THIS (organization ID)
        }
        emit(ResultState.Success(response.body<SummaryResponse>()))
    } catch (e: Exception) {
        emit(ResultState.Error("Network error: ${e.message}"))
    }
}

// getSuggestion is already correct - no changes needed!
```

---

### **FIX #3: FacilityRepositoryImpl.kt**

```kotlin
override suspend fun getFacilities(): Flow<ResultState<List<FacilityResponse>>> = flow {
    emit(ResultState.Loading)
    try {
        val token = userPrefrence.accessToken.first()
        val userId = userPrefrence.userId.first() // ✅ GET USER ID
        
        if (token.isNullOrEmpty() || userId == null) {
            emit(ResultState.Error("User not logged in"))
            return@flow
        }

        val httpResponse = httpClient.get(Constants.BASE_URL + Constants.FACILITIES) {
            headers {
                append("Authorization", "Bearer $token")
            }
            parameter("user_id", userId) // ✅ ADD THIS
        }

        if (httpResponse.status.isSuccess()) {
            val response = httpResponse.body<List<FacilityResponse>>()
            emit(ResultState.Success(response))
        } else {
            val errorBody = try {
                httpResponse.body<String>()
            } catch (e: Exception) {
                "HTTP ${httpResponse.status.value}"
            }
            emit(ResultState.Error("Server error: ${httpResponse.status.value} - $errorBody"))
        }
    } catch (e: Exception){
        emit(ResultState.Error(e.message ?: "Unknown error occurred"))
    }
}
```

---

## 🎯 SUMMARY OF CHANGES

### Files to Modify:
1. ✅ `UserPreference.kt` - Add userId storage
2. ✅ `AuthRepositoryImpl.kt` - Save userId on login/signup
3. ✅ `UserRepositoryImpl.kt` - Add user_id parameter to /auth/me
4. ✅ `AnalyticsRepositoryImpl.kt` - Add user_id to all analytics endpoints + id to summary
5. ✅ `FacilityRepositoryImpl.kt` - Add user_id parameter to /facilities

### Pattern to Follow:
```kotlin
// 1. Get user ID from preferences
val userId = userPrefrence.userId.first()

// 2. Check if it exists
if (userId == null) {
    emit(ResultState.Error("User not logged in"))
    return@flow
}

// 3. Add as query parameter
parameter("user_id", userId)
```

---

## ✅ TESTING AFTER FIX

After making these changes, all endpoints should return **200 OK** instead of **422 Unprocessable Entity**.

Test each endpoint:
1. Login/Signup to get userId saved
2. Call /auth/me - should work
3. Call /analytics/kpis - should work
4. Call /analytics/summary - should work  
5. Call /tenants/facilities - should work

---

## 📞 NEED HELP?

If you encounter issues:
1. Check server logs for detailed error messages
2. Verify userId is saved correctly in UserPreference
3. Ensure userId is retrieved before making API calls
4. Check that parameter name is exactly "user_id"

**All 422 errors should be resolved after adding the user_id parameter!**
