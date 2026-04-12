package com.rozetka.data.di

import com.rozetka.data.SecureStorage
import com.rozetka.data.repository.CalendarRepositoryImpl
import com.rozetka.data.repository.MoodleRepositoryImpl
import com.rozetka.data.repository.UserRepositoryImpl
import com.rozetka.domain.repository.CalendarRepository
import com.rozetka.domain.repository.MoodleRepository
import com.rozetka.domain.repository.UserRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single { SecureStorage(androidContext()) }
    single<CalendarRepository> { CalendarRepositoryImpl(androidContext()) }
    single<MoodleRepository> { MoodleRepositoryImpl() }
}