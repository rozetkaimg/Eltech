package com.rozetka.presentation.di

import com.rozetka.presentation.ui.RootViewModel
import com.rozetka.presentation.ui.academicPerformance.AcademicPerformanceViewModel
import com.rozetka.presentation.ui.dialog.DialogViewModel
import com.rozetka.presentation.ui.digitalService.DigitalServiceViewModel
import com.rozetka.presentation.ui.home.HomeViewModel
import com.rozetka.presentation.ui.login.LoginViewModel
import com.rozetka.presentation.ui.message.MessagesViewModel
import com.rozetka.presentation.ui.pay.PayViewModel
import com.rozetka.presentation.ui.physEdJournal.PhysEdJournalViewModel
import com.rozetka.presentation.ui.profile.ProfileViewModel
import com.rozetka.presentation.ui.projectActivity.ProjectActivityViewModel
import com.rozetka.presentation.ui.searchGroup.SearchGroupViewModel
import com.rozetka.presentation.ui.settings.SettingsViewModel
import com.rozetka.presentation.ui.shedule.ScheduleViewModel
import com.rozetka.presentation.ui.studentCard.StudentCardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::RootViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ScheduleViewModel)
    viewModelOf(::PayViewModel)
    viewModelOf(::MessagesViewModel)
    viewModelOf(::DialogViewModel)
    viewModelOf(::AcademicPerformanceViewModel)
    viewModelOf(::StudentCardViewModel)
    viewModelOf(::DigitalServiceViewModel)
    viewModelOf(::PhysEdJournalViewModel)
    viewModelOf(::SearchGroupViewModel)
    viewModelOf(::ProjectActivityViewModel)
}