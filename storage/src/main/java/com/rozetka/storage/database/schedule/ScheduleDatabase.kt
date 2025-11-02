package com.rozetka.storage.database.schedule

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [GroupEntity::class, LessonEntity::class, AuditoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ScheduleDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
}