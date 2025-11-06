package com.nutrino.carbonfootprint.di

import android.content.Context
import com.nutrino.carbonfootprint.data.local.UserPrefrence
import com.nutrino.carbonfootprint.data.repoImpl.AnalyticsRepositoryImpl
import com.nutrino.carbonfootprint.data.repoImpl.AuthRepositoryImpl
import com.nutrino.carbonfootprint.data.repoImpl.EmissionsRepositoryImpl
import com.nutrino.carbonfootprint.data.repoImpl.FacilityRepositoryImpl
import com.nutrino.carbonfootprint.data.repoImpl.FactorsRepositoryImpl
import com.nutrino.carbonfootprint.data.repoImpl.IngestionRepositoryImpl
import com.nutrino.carbonfootprint.data.repoImpl.ReportsRepositoryImpl
import com.nutrino.carbonfootprint.data.repoImpl.UserRepositoryImpl
import com.nutrino.carbonfootprint.domain.repository.AnalyticsRepository
import com.nutrino.carbonfootprint.domain.repository.AuthRepository
import com.nutrino.carbonfootprint.domain.repository.EmissionsRepository
import com.nutrino.carbonfootprint.domain.repository.FacilityRepository
import com.nutrino.carbonfootprint.domain.repository.FactorsRepository
import com.nutrino.carbonfootprint.domain.repository.IngestionRepository
import com.nutrino.carbonfootprint.domain.repository.ReportsRepository
import com.nutrino.carbonfootprint.domain.repository.UserRepository
import com.nutrino.carbonfootprint.domain.usecase.CreateFacilityUseCase
import com.nutrino.carbonfootprint.domain.usecase.GetFacilitiesUseCase
import com.nutrino.carbonfootprint.domain.usecase.GetKpisUseCase
import com.nutrino.carbonfootprint.domain.usecase.GetMeUseCase
import com.nutrino.carbonfootprint.domain.usecase.GetSummaryUseCase
import com.nutrino.carbonfootprint.domain.usecase.IngestEventsUseCase
import com.nutrino.carbonfootprint.domain.usecase.SignInUseCase
import com.nutrino.carbonfootprint.domain.usecase.SignUpUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton
import com.nutrino.carbonfootprint.data.logs.debugLogs

@Module
@InstallIn(SingletonComponent::class)
object DiModule {

    @Provides
    @Singleton
    fun provideKtorClient(): HttpClient{
        val client = HttpClient(CIO){
            install(ContentNegotiation){
                json(Json{
                    ignoreUnknownKeys=true
                    isLenient = true
                    prettyPrint=true
                })
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        debugLogs(message, "Ktor")
                    }
                }
                level = LogLevel.ALL
            }
        }
        debugLogs("HttpClient initialized with logging configuration", "DiModule")
        return client
    }

    @Provides
    fun contextObj(@ApplicationContext context: Context): Context{
        return context

    }

    @Provides
    fun userPrefrenceObj(@ApplicationContext context: Context): UserPrefrence {
        return UserPrefrence(context)
    }

    @Provides
    fun provideAutjRepo(userPrefrence: UserPrefrence, httpClient: HttpClient, userRepository: UserRepository): AuthRepository{
        return AuthRepositoryImpl(
            userPrefrence = userPrefrence,
            httpClient = httpClient,
            userRepository = userRepository
        )
    }

    //USECASE
    @Provides
    fun provideSignUpUseCase(authRepository: AuthRepository): SignUpUseCase{
        return SignUpUseCase(authRepository = authRepository)
    }

    @Provides
    fun provideSignInUseCase(authRepository: AuthRepository): SignInUseCase{
        return SignInUseCase(authRepository = authRepository)
    }

    @Provides
    fun provideGetMeUseCase(userRepository: UserRepository): GetMeUseCase{
        return GetMeUseCase(userRepository = userRepository)
    }

    @Provides
    fun provideGetFacilitiesUseCase(facilityRepository: FacilityRepository): GetFacilitiesUseCase{
        return GetFacilitiesUseCase(facilityRepository = facilityRepository)
    }

    @Provides
    fun provideCreateFacilityUseCase(facilityRepository: FacilityRepository): CreateFacilityUseCase{
        return CreateFacilityUseCase(facilityRepository = facilityRepository)
    }

    @Provides
    fun provideGetKpisUseCase(analyticsRepository: AnalyticsRepository): GetKpisUseCase{
        return GetKpisUseCase(analyticsRepository = analyticsRepository)
    }

    @Provides
    fun provideGetSummaryUseCase(analyticsRepository: AnalyticsRepository): GetSummaryUseCase{
        return GetSummaryUseCase(analyticsRepository = analyticsRepository)
    }

    @Provides
    fun provideIngestEventsUseCase(ingestionRepository: IngestionRepository): IngestEventsUseCase{
        return IngestEventsUseCase(ingestionRepository = ingestionRepository)
    }

    // REPOSITORIES
    @Provides
    fun provideUserRepository(httpClient: HttpClient, userPrefrence: UserPrefrence): UserRepository{
        return UserRepositoryImpl(httpClient = httpClient, userPrefrence = userPrefrence)
    }

    @Provides
    fun provideFacilityRepository(httpClient: HttpClient, userPrefrence: UserPrefrence): FacilityRepository{
        return FacilityRepositoryImpl(httpClient = httpClient, userPrefrence = userPrefrence)
    }

    @Provides
    fun provideAnalyticsRepository(httpClient: HttpClient, userPrefrence: UserPrefrence): AnalyticsRepository{
        return AnalyticsRepositoryImpl(httpClient = httpClient, userPrefrence = userPrefrence)
    }

    @Provides
    fun provideIngestionRepository(httpClient: HttpClient, userPrefrence: UserPrefrence): IngestionRepository{
        return IngestionRepositoryImpl(httpClient = httpClient, userPrefrence = userPrefrence)
    }


    @Provides
    fun provideFactorsRepository(httpClient: HttpClient, userPrefrence: UserPrefrence): FactorsRepository{
        return FactorsRepositoryImpl(httpClient = httpClient, userPrefrence = userPrefrence)
    }

    @Provides
    fun provideEmissionsRepository(httpClient: HttpClient, userPrefrence: UserPrefrence): EmissionsRepository{
        return EmissionsRepositoryImpl(httpClient = httpClient, userPrefrence = userPrefrence)
    }

    @Provides
    fun provideReportsRepository(httpClient: HttpClient, userPrefrence: UserPrefrence): ReportsRepository{
        return ReportsRepositoryImpl(httpClient = httpClient, userPrefrence = userPrefrence)
    }

}