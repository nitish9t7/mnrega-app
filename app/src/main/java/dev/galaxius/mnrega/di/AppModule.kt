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

import android.app.Application
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.galaxius.mnrega.core.connectivity.ConnectivityObserver
import dev.galaxius.mnrega.core.preference.PreferenceManager
import dev.galaxius.mnrega.core.session.SessionManager
import dev.galaxius.mnrega.preference.PreferenceManagerImpl
import dev.galaxius.mnrega.preference.uiModePrefDataStore
import dev.galaxius.mnrega.session.MnregaSharedPreferencesFactory
import dev.galaxius.mnrega.session.SessionManagerImpl
import dev.galaxius.mnrega.utils.connectivityManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun providePreferenceManager(application: Application): PreferenceManager {
        return PreferenceManagerImpl(application.uiModePrefDataStore)
    }

    @Singleton
    @Provides
    fun provideSessionManager(application: Application): SessionManager {
        return SessionManagerImpl(MnregaSharedPreferencesFactory.sessionPreferences(application))
    }

    @Singleton
    @Provides
    fun provideConnectivityObserver(application: Application): ConnectivityObserver {
        return dev.galaxius.mnrega.connectivity.ConnectivityObserverImpl(application.connectivityManager)
    }

    @Singleton
    @Provides
    fun provideWorkManager(application: Application): WorkManager {
        return WorkManager.getInstance(application)
    }
}
