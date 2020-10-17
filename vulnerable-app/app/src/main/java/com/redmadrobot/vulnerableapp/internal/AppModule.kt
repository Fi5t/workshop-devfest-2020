package com.redmadrobot.vulnerableapp.internal

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.redmadrobot.pinkman.Pinkman
import com.redmadrobot.vulnerableapp.ui.profile.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
class AppModule {
    @Singleton
    @Provides
    fun provideSharedPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideHttpClient(preferences: SharedPreferences): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .apply {
                        preferences.getString("access_token", null)?.let { token ->
                            addHeader("Authorization", "Token $token")
                        }
                    }.build()

                chain.proceed(request)
            }.build()
    }

    @Singleton
    @Provides
    fun provideApi(httpClient: OkHttpClient): Api {
        return Retrofit.Builder()
            .baseUrl("https://vulnerable-backend.herokuapp.com/api/v1/")
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(Api::class.java)
    }

    @Singleton
    @Provides
    fun providePinkman(application: Application): Pinkman {
        return Pinkman(application.applicationContext)
    }

    @Singleton
    @Provides
    fun provideAesEncryption(): AesEncryption {
        return AesEncryption()
    }
}
