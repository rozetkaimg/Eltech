package com.rozetka.domain.repository

import com.rozetka.model.PDModel
import com.rozetka.model.ProjectSheetItem
import com.rozetka.model.ScheduleModel

interface ProjectActivityRepository {
    suspend fun getProjectActivity(token: String): PDModel
    suspend fun getProjectSchedule(url: String = DEFAULT_SCHEDULE_URL): List<ProjectSheetItem>
    suspend fun replacePDDiscipline(
        schedule: ScheduleModel,
        token: String,
        url: String = DEFAULT_SCHEDULE_URL
    ): ScheduleModel

    companion object {
        const val DEFAULT_SCHEDULE_URL =
            "https://docs.google.com/spreadsheets/d/e/2PACX-1vRHgwt89VM13uuUy8dDvpjlb-BDM4ovsIKhoLDGIaI58NJtAawxABVW7sdVRDDH5UGaM2bsryni6Im7/pubhtml?gid=643150359&single=true"
    }
}
