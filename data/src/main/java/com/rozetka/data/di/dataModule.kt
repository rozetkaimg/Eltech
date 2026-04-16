package com.rozetka.data.di

import com.rozetka.data.SecureStorage
import com.rozetka.data.repository.CalendarRepositoryImpl
import com.rozetka.data.repository.DigitalServiceRepositoryImpl
import com.rozetka.data.repository.GroupJournalRepositoryImpl
import com.rozetka.data.repository.MoodleRepositoryImpl
import com.rozetka.data.repository.PayRepositoryImpl
import com.rozetka.data.repository.PhysEdJournalRepositoryImpl
import com.rozetka.data.repository.ProjectActivityRepositoryImpl
import com.rozetka.data.repository.UserRepositoryImpl
import com.rozetka.domain.repository.CalendarRepository
import com.rozetka.domain.repository.DigitalServiceRepository
import com.rozetka.domain.repository.GroupJournalRepository
import com.rozetka.domain.repository.MoodleRepository
import com.rozetka.domain.repository.PayRepository
import com.rozetka.domain.repository.PhysEdJournalRepository
import com.rozetka.domain.repository.ProjectActivityRepository
import com.rozetka.domain.repository.UserRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single { SecureStorage(androidContext()) }
    single<CalendarRepository> { CalendarRepositoryImpl(androidContext()) }
    single<MoodleRepository> { MoodleRepositoryImpl() }
    single<GroupJournalRepository> { GroupJournalRepositoryImpl(get()) }
    single<PhysEdJournalRepository> { PhysEdJournalRepositoryImpl(get()) }
    single<PayRepository> { PayRepositoryImpl(get()) }
    single<DigitalServiceRepository> { DigitalServiceRepositoryImpl(get()) }
    single<ProjectActivityRepository> { ProjectActivityRepositoryImpl(get()) }
}