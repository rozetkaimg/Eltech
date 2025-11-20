package com.rozetka.presentation.di

import com.rozetka.presentation.ui.RootViewModel
import com.rozetka.presentation.ui.academicPerformance.AcademicPerformanceViewModel
import com.rozetka.presentation.ui.dialog.DialogViewModel
import com.rozetka.presentation.ui.digitalService.DigitalServiceViewModel
import com.rozetka.presentation.ui.employees.EmployeesViewModel
import com.rozetka.presentation.ui.home.HomeViewModel
import com.rozetka.presentation.ui.login.LoginViewModel
import com.rozetka.presentation.ui.message.MessagesViewModel
import com.rozetka.presentation.ui.teacherSchedule.TeacherScheduleViewModel
import com.rozetka.presentation.ui.pay.PayViewModel
import com.rozetka.presentation.ui.physEdJournal.PhysEdJournalViewModel
import com.rozetka.presentation.ui.profile.ProfileViewModel
import com.rozetka.presentation.ui.projectActivity.ProjectActivityViewModel
import com.rozetka.presentation.ui.searchGroup.SearchGroupViewModel
import com.rozetka.presentation.ui.settings.SettingsViewModel
import com.rozetka.presentation.ui.shedule.ScheduleViewModel
import com.rozetka.presentation.ui.studentCard.StudentCardViewModel
import com.rozetka.presentation.ui.teachersRaiting.TeacherRatingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::RootViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::EmployeesViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ScheduleViewModel)
    viewModelOf(::TeacherScheduleViewModel)
    viewModelOf(::PayViewModel)
    viewModelOf(::MessagesViewModel)
    viewModelOf(::DialogViewModel)
    viewModelOf(::AcademicPerformanceViewModel)
    viewModelOf(::StudentCardViewModel)
    viewModelOf(::DigitalServiceViewModel)
    viewModelOf(::PhysEdJournalViewModel)
    viewModelOf(::SearchGroupViewModel)
    viewModelOf(::ProjectActivityViewModel)
    viewModelOf(::TeacherRatingViewModel)


}