/*
 * Copyright 2022 Team GALAXIUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.galaxius.mnrega.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.galaxius.mnrega.core.utils.moshi
import dev.galaxius.mnrega.data.remote.Constant
import dev.galaxius.mnrega.data.remote.api.MnregaAuthService
import dev.galaxius.mnrega.data.remote.api.MnregaService
import dev.galaxius.mnrega.data.remote.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    private val baseRetrofitBuilder: Retrofit.Builder = Retrofit.Builder()
        .baseUrl(Constant.API_BASE_URL)
//        .addConverterFactory(GsonConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))

    private val okHttpClientBuilder: OkHttpClient.Builder =
        OkHttpClient.Builder()
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)

    @Provides
    fun provideMnregaService(authInterceptor: AuthInterceptor): MnregaService {
        return baseRetrofitBuilder
            .client(okHttpClientBuilder.addInterceptor(authInterceptor).build())
            .build()
            .create(MnregaService::class.java)
    }

    @Provides
    fun provideMnregaAuthService(): MnregaAuthService {
        return baseRetrofitBuilder
            .client(okHttpClientBuilder.build())
            .build()
            .create(MnregaAuthService::class.java)
    }
}
