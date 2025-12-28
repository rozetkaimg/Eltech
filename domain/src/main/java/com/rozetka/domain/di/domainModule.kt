package com.rozetka.domain.di


import com.rozetka.domain.repository.AppRepository
import com.rozetka.domain.repository.LoginRepository
import com.rozetka.domain.repository.ScheduleRepository
import com.rozetka.domain.repository.UsersRepository
import com.rozetka.domain.usecase.CalendarUseCase
import com.rozetka.domain.usecase.GetAuthTokenUseCase
import com.rozetka.domain.usecase.GetUserDataUseCase
import com.rozetka.domain.usecase.SignInUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val domainModule = module {
    factory { SignInUseCase(get()) }
    factory { CalendarUseCase(get()) }
    factory { GetUserDataUseCase(get()) }
    single { LoginRepository(get(), get()) }
    single { UsersRepository(get(), get(), get(), get()) }
    single { AppRepository(get(), get(), get(), get()) }
    factory { GetAuthTokenUseCase(get()) }
    single {
        ScheduleRepository(
            networkApi = get(),
            storage = get(),
            context = androidContext()
        )
    }
}