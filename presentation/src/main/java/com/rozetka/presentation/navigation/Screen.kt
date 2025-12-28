package com.rozetka.presentation.navigation

import com.rozetka.presentation.R
import com.rozetka.presentation.navigation.Screen.AboutApplication.route

sealed class Screen(
    val route: String,
    val titleResId: Int,
    val icon: Int,
    val isFullScreen: Boolean = false,
    val parentRoute: String? = null
) {
    data object Login :
        Screen(
            "login",
            R.string.login_hint,
            icon = R.drawable.door_arrow_left_outline_24,
            isFullScreen = false
        )

    data object GuestGroupInput : Screen(
        route = "GuestGroupInput",
        titleResId = R.string.schedule,
        icon = R.drawable.door_arrow_left_outline_24,
        isFullScreen = false
    )


    data object Profile : Screen("profile_route", R.string.profile, 0, false)
    data object Schedule : Screen(
        route = "Schedule",
        titleResId = R.string.schedule,
        icon = R.drawable.calendar_outline,

    )
    data object SessionSchedule : Screen(
        route = "SessionSchedule/{groupName}",
        titleResId = R.string.schedule,
        icon = R.drawable.newsfeed,
        parentRoute = Schedule.route
    )

    data object ArticleScreen : Screen(
        route = "Article/{url}",
        titleResId = R.string.schedule,
        icon = R.drawable.newsfeed,
        parentRoute = Home.route
    )
    data object ScheduleLink : Screen(
        route = "ScheduleLink",
        titleResId = R.string.schedule,
        icon = R.drawable.newsfeed,
        parentRoute = Schedule.route
    )

    data object Mail : Screen(
        route = "mail", titleResId = R.string.mail, icon = R.drawable.chats_outline_28
    )

    data object DigitalService : Screen(
        route = "digitalService",
        titleResId = R.string.digital_services,
        icon = R.drawable.services_outline_24,
        isFullScreen = false,
        parentRoute = Service.route
    )

    data object Maps : Screen(
        route = "mapsScreen",
        titleResId = R.string.digital_services,
        icon = R.drawable.newsfeed,
        isFullScreen = false,
        parentRoute = Service.route
    )

    data object ProjectActivity : Screen(
        route = "projectActivity",
        titleResId = R.string.digital_services,
        icon = R.drawable.newsfeed,
        isFullScreen = false,
        parentRoute = Service.route
    )

    data object Dialo : Screen(
        route = "dialo/{userName}/{userId}",
        titleResId = R.string.back_content_description,
        icon = 0,
        isFullScreen = true,
        parentRoute = Mail.route
    )

    data object StudentCardScreen : Screen(
        route = "StudentCard",
        titleResId = R.string.session_results,
        icon = R.drawable.newsfeed,
        isFullScreen = false,
        parentRoute = Service.route
    )

    data object PhysEdJournalScreen : Screen(
        route = "PhysEdJournal",
        titleResId = R.string.physical_education,
        icon = 0,
        isFullScreen = false,
        parentRoute = Service.route
    )

    data object PhysGroupJournalScreen : Screen(
        route = "PhysGroupJournal",
        titleResId = R.string.physical_education,
        icon = R.drawable.newsfeed,
        isFullScreen = false,
        parentRoute = Service.route
    )

    data object SearchGroupScreen : Screen(
        route = "SearchGroup",
        titleResId = R.string.physical_education,
        icon = 0,
        isFullScreen = false,
        parentRoute = Schedule.route
    )

    data object SearchStudentsScreen : Screen(
        route = "SearchStudents",
        titleResId = R.string.physical_education,
        icon = R.drawable.newsfeed,
        isFullScreen = false,
        parentRoute = Service.route
    )

    data object AcademicPerScreen : Screen(
        route = "academic",
        titleResId = R.string.session_results,
        icon = R.drawable.newsfeed,
        isFullScreen = false,
        parentRoute = Service.route
    )

    data object SubmitAnApplication : Screen(
        route = "submitAnApplication",
        titleResId = R.string.session_results,
        icon = R.drawable.newsfeed,
        isFullScreen = false,
        parentRoute = Service.route
    )

    data object TeacherRating : Screen(
        route = "teacherRating/{teacherID}fio={fio}&avatar={avatar}&division={division}&email={email}&id={id}&post={post}",
        titleResId = R.string.session_results,
        icon = R.drawable.newsfeed,
        isFullScreen = false,
        parentRoute = Schedule.route
    )

    data object Employees : Screen(
        route = "employees",
        titleResId = R.string.session_results,
        icon = R.drawable.newsfeed,
        isFullScreen = false,
        parentRoute = Schedule.route
    )

    data object Home : Screen(
        route = "home", titleResId = R.string.home, icon = R.drawable.newsfeed
    )

    data object TeacherSchedule : Screen(
        route = "teacherSchedule/{fio}",
        titleResId = R.string.schedule,
        icon = R.drawable.newsfeed,
        isFullScreen = false,
        parentRoute = Schedule.route
    )

    data object TeacherReview : Screen(
        route = "teacherReview/{id}",
        titleResId = R.string.schedule,
        icon = R.drawable.newsfeed,
        isFullScreen = false,
        parentRoute = Schedule.route
    )

    data object Service : Screen(
        route = "Service",
        titleResId = R.string.services,
        isFullScreen = false,
        icon = R.drawable.services_outline_24
    )
    data object CreateApplicationScreen : Screen(
        route = "applications/{applicationId}",
        titleResId = R.string.services,
        isFullScreen = false,
        icon = R.drawable.services_outline_24,
        parentRoute = Service.route
    )

    data object Payment : Screen(
        route = "pay",
        titleResId = R.string.services,
        icon = R.drawable.newsfeed,
        isFullScreen = false,
        parentRoute = Service.route
    )

    data object Settings : Screen(
        route = "Settings",
        titleResId = R.string.settings,
        icon = R.drawable.newsfeed,
        isFullScreen = true,
        parentRoute = route
    )

    data object AboutApplication : Screen(
        route = "aboutApplication",
        titleResId = R.string.settings,
        icon = R.drawable.newsfeed,
        isFullScreen = true,

    )
}

val bottomNavItems = listOf(
    Screen.Home, Screen.Schedule, Screen.Mail, Screen.Service
)