package com.rozetka.storage.di

import androidx.room.Room
import com.rozetka.storage.UserStorageImpl
import com.rozetka.storage.database.schedule.ScheduleDatabase
import com.rozetka.storage.database.schedule.ScheduleStorage
import com.rozetka.storage.database.userCard.UserDatabase
import com.rozetka.storage.database.userCard.UserStudentCardStorage
import com.rozetka.storage.database.userProfile.UserProfileDatabase
import com.rozetka.storage.database.userProfile.UserProfileStorage
import com.rozetka.storage.repository.SessionManager
import com.rozetka.storage.repository.UserStorage
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val storageModule = module {

    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    single { SessionManager(androidContext()) }
    single<UserStorage> { UserStorageImpl(androidContext()) }



    single {
        Room.databaseBuilder(
            androidContext(),
            ScheduleDatabase::class.java,
            "schedule_database"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    single {
        get<ScheduleDatabase>().scheduleDao()
    }

    single {
        ScheduleStorage(db = get(), dao = get())
    }


    single {
        Room.databaseBuilder(
            androidContext(),
            UserDatabase::class.java,
            "user_database"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    single {
        get<UserDatabase>().userStudentCardDao()
    }

    single {
        UserStudentCardStorage(db = get(), dao = get())
    }
    single {
        Room.databaseBuilder(
            androidContext(),
            UserProfileDatabase::class.java,
            "user_profile_database"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    single {
        get<UserProfileDatabase>().userDao()
    }

    single {
        UserProfileStorage(db = get(), dao = get())
    }
}
