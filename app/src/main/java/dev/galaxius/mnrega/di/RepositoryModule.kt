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

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.galaxius.mnrega.core.repository.MnregaNoteRepository
import dev.galaxius.mnrega.core.repository.MnregaUserRepository
import dev.galaxius.mnrega.repository.DefaultMnregaUserRepository
import dev.galaxius.mnrega.repository.MnregaRemoteNoteRepository
import dev.galaxius.mnrega.repository.local.MnregaLocalNoteRepository
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun mnregaAuthRepository(mnregaAuthRepository: DefaultMnregaUserRepository): MnregaUserRepository

    @Binds
    @LocalRepository
    fun mnregaLocalNoteRepository(localRepository: MnregaLocalNoteRepository): MnregaNoteRepository

    @Binds
    @RemoteRepository
    fun mnregaRemoteNoteRepository(remoteRepository: MnregaRemoteNoteRepository): MnregaNoteRepository
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class LocalRepository

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class RemoteRepository
