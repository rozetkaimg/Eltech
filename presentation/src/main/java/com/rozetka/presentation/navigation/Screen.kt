package com.rozetka.presentation.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataArray
import com.rozetka.presentation.new.HomeOutline28
import com.rozetka.presentation.R
import com.rozetka.presentation.new.MailOutline28
import com.rozetka.presentation.new.Profile28
import com.rozetka.presentation.new.ServicesOutline28
import com.rozetka.presentation.new.CalendarOutline28
import com.rozetka.presentation.navigation.Screen.Login.route

sealed class Screen(
    val route: String,
    val titleResId: Int,
    val icon: ImageVector,
    val isFullScreen: Boolean = false,
    val parentRoute: String? = null
) {
    data object Login :
        Screen("login", R.string.login_hint, icon = Icons.Default.DataArray, isFullScreen = false)

    data object Profile : Screen("profile_route", R.string.profile, Profile28, false)
    data object Schedule : Screen(
        route = "Schedule",
        titleResId = R.string.schedule,
        icon = CalendarOutline28
    )

    data object Mail : Screen(
        route = "mail",
        titleResId = R.string.mail,
        icon = MailOutline28
    )

    data object DigitalService : Screen(
        route = "digitalService",
        titleResId = R.string.digital_services,
        icon = MailOutline28,
        isFullScreen = false,
        parentRoute = Service.route
    )
    data object Maps : Screen(
        route = "mapsScreen",
        titleResId = R.string.digital_services,
        icon = MailOutline28,
        isFullScreen = false,
        parentRoute = Service.route
    )
    data object ProjectActivity : Screen(
        route = "projectActivity",
        titleResId = R.string.digital_services,
        icon = MailOutline28,
        isFullScreen = false,
        parentRoute = Service.route
    )

    data object Dialo : Screen(
        route = "dialo/{userName}/{userId}",
        titleResId = R.string.back_content_description,
        icon = MailOutline28,
        isFullScreen = true,
        parentRoute = Mail.route
    )

    data object StudentCardScreen : Screen(
        route = "StudentCard",
        titleResId = R.string.session_results,
        icon = MailOutline28,
        isFullScreen = false,
        parentRoute = Service.route

    )
    data object PhysEdJournalScreen : Screen(
        route = "PhysEdJournal",
        titleResId = R.string.physical_education,
        icon = MailOutline28,
        isFullScreen = false,
        parentRoute = Service.route

    )
    data object SearchGroupScreen : Screen(
        route = "SearchGroup",
        titleResId = R.string.physical_education,
        icon = MailOutline28,
        isFullScreen = false,
        parentRoute = Schedule.route

    )


    data object AcademicPerScreen : Screen(
        route = "academic",
        titleResId = R.string.session_results,
        icon = MailOutline28,
        isFullScreen = false,
        parentRoute = Service.route

    )
    data object SubmitAnApplication: Screen(
        route = "submitAnApplication",
        titleResId = R.string.session_results,
        icon = MailOutline28,
        isFullScreen = false,
        parentRoute = Service.route
    )

    data object TeacherRating: Screen(
        route = "teacherRating",
        titleResId = R.string.session_results,
        icon = MailOutline28,
        isFullScreen = false,
        parentRoute = Schedule.route
    )
    data object Employees: Screen(
        route = "employees",
        titleResId = R.string.session_results,
        icon = MailOutline28,
        isFullScreen = false,
        parentRoute = Schedule.route
    )
    data object Home : Screen(
        route = "home",
        titleResId = R.string.home,
        icon = HomeOutline28
    )

    data object TeacherSchedule : Screen(
        route = "teacherSchedule/{fio}",
        titleResId = R.string.schedule,
        icon = CalendarOutline28,
        isFullScreen = false,
        parentRoute = Schedule.route
    )
    data object Service : Screen(
        route = "Service",
        titleResId = R.string.services,
        isFullScreen = false,
        icon = ServicesOutline28
    )

    data object Payment : Screen(
        route = "pay",
        titleResId = R.string.services,
        icon = ServicesOutline28,
        isFullScreen = false,
        parentRoute = Service.route
    )

    data object Settings : Screen(
        route = "Settings",
        titleResId = R.string.settings,
        icon = ServicesOutline28,
        isFullScreen = true,
        parentRoute = route
    )
    data object AboutApplication : Screen(
        route = "aboutApplication",
        titleResId = R.string.settings,
        icon = ServicesOutline28,
        isFullScreen = true,
        parentRoute = route
    )

}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Schedule,
    Screen.Mail,
    Screen.Service
)