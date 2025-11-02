package com.rozetka.storage.database.schedule

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Insert
    suspend fun insertLesson(lesson: LessonEntity): Long

    @Insert
    suspend fun insertAuditories(auditories: List<AuditoryEntity>)

    @Query("DELETE FROM groups WHERE groupTitle = :groupTitle")
    suspend fun clearGroup(groupTitle: String)

    @Query("DELETE FROM lessons WHERE groupTitle = :groupTitle")
    suspend fun clearLessonsForGroup(groupTitle: String)

    @Query("SELECT * FROM groups WHERE groupTitle = :groupTitle")
    suspend fun getGroup(groupTitle: String): GroupEntity?

    @Transaction
    @Query("SELECT * FROM lessons WHERE groupTitle = :groupTitle")
    suspend fun getLessonsWithAuditories(groupTitle: String): List<LessonWithAuditories>
}