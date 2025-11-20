package com.rozetka.presentation.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.rozetka.presentation.ui.submitanApplication.SubmitAnApplicationScreen
import com.rozetka.presentation.ui.aboutApplication.AboutScreen
import com.rozetka.presentation.ui.academicPerformance.AcademicPerformanceScreen
import com.rozetka.presentation.ui.digitalService.DigitalServiceScreen
import com.rozetka.presentation.ui.home.HomeScreen
import com.rozetka.presentation.ui.message.MessagesScreen
import com.rozetka.presentation.ui.dialog.DialogScreen
import com.rozetka.presentation.ui.maps.MapsScreen
import com.rozetka.presentation.ui.pay.PayScreen
import com.rozetka.presentation.ui.physEdJournal.PhysEdJournalScreen
import com.rozetka.presentation.ui.profile.ProfileScreen
import com.rozetka.presentation.ui.projectActivity.ProjectActivityScreen
import com.rozetka.presentation.ui.searchGroup.SearchGroupScreen
import com.rozetka.presentation.ui.settings.SettingsScreen
import com.rozetka.presentation.ui.shedule.ScheduleScreen
import com.rozetka.presentation.ui.studentCard.StudentCardScreen
import com.rozetka.domain.util.StringObject
import com.rozetka.presentation.ui.employees.EmployeesScreen
import com.rozetka.presentation.ui.teacherSchedule.TeacherScheduleScreen
import com.rozetka.presentation.ui.teachersRaiting.TeacherRatingScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass
) {
    NavHost(
        navController = navController,
        startDestination = if (StringObject.groupName == "") Screen.Home.route else Screen.Schedule.route + "/${StringObject.groupName}",
        modifier = modifier
    ) {


        composable(
            Screen.Dialo.route,
            arguments = listOf(
                navArgument("userName") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType }

            )
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            DialogScreen(
                navController = navController,
                userId = userId,
                userName = userName
            )
        }
        composable(Screen.AcademicPerScreen.route) {
            AcademicPerformanceScreen(navController)

        }
        composable(Screen.AboutApplication.route) {
            AboutScreen(navController)

        }
        composable(Screen.PhysEdJournalScreen.route) {
            PhysEdJournalScreen(navController)

        }
        composable(
            route = Screen.SearchGroupScreen.route,
            deepLinks = listOf(navDeepLink {
                uriPattern = "app://com.rozetka.epotitech/search_group"
            })
        ) {
            SearchGroupScreen(navController)
        }
        composable(Screen.StudentCardScreen.route) {
            StudentCardScreen(navController)

        }
        composable(Screen.ProjectActivity.route) {
            ProjectActivityScreen(navController)

        }
        composable(Screen.Employees.route) {
            EmployeesScreen(navController)
        }

        composable(Screen.Maps.route) {
            MapsScreen(navController)

        }

        composable(Screen.SubmitAnApplication.route) {
            SubmitAnApplicationScreen(navController)
        }
        composable(Screen.DigitalService.route) {
            DigitalServiceScreen(navController)
        }


        composable(
            route = Screen.TeacherRating.route + "/{teacherID}?fio={fio}&avatar={avatar}&division={division}&email={email}&id={id}&post={post}",
            arguments = listOf(
                navArgument("teacherID") { type = NavType.StringType },
                navArgument("fio") { type = NavType.StringType; defaultValue = "" },
                navArgument("avatar") { type = NavType.StringType; defaultValue = "" },
                navArgument("division") { type = NavType.StringType; defaultValue = "" },
                navArgument("email") { type = NavType.StringType; defaultValue = "" },
                navArgument("id") { type = NavType.StringType; defaultValue = "" },
                navArgument("post") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherID") ?: ""
            val fio = backStackEntry.arguments?.getString("fio") ?: ""
            val avatar = backStackEntry.arguments?.getString("avatar") ?: ""
            val division = backStackEntry.arguments?.getString("division") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val post = backStackEntry.arguments?.getString("post") ?: ""

            TeacherRatingScreen(
                navController = navController,
                teacherId = teacherId,
                avatar = avatar,
                division = division,
                email = email,
                fio = fio,
                id = id,
                post = post
            )
        }

        composable(
            route = Screen.Schedule.route + "/{groupName}",
            arguments = listOf(
                navArgument("groupName") { type = NavType.StringType },
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "app://com.rozetka.epotitech/schedule/{groupName}"
            })
        ) { backStackEntry ->
            val groupName =
                backStackEntry.arguments?.getString("groupName") ?: StringObject.groupName
            ScheduleScreen(navController, groupName)
        }
        composable(
            route = Screen.TeacherSchedule.route,
            arguments = listOf(
                navArgument("fio") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val fio = backStackEntry.arguments?.getString("fio") ?: ""
            TeacherScheduleScreen(navController = navController, fio = fio)
        }
        composable(Screen.Mail.route) {
            MessagesScreen(navController = navController)
        }

        composable(
            route = Screen.Home.route,
            deepLinks = listOf(navDeepLink {
                uriPattern = "app://com.rozetka.epotitech"
            })
        ) {
            HomeScreen(navController)
        }

        composable(Screen.Payment.route) {
            PayScreen(navController)
        }
        composable(Screen.Settings.route, deepLinks = listOf(navDeepLink {
            uriPattern = "app://com.rozetka.epotitech/settings"
        })) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.Profile.route) {

        }
        composable(Screen.Service.route) {
            ProfileScreen(navController = navController)

        }

    }
}