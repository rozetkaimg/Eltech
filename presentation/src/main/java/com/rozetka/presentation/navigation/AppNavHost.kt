package com.rozetka.presentation.navigation

import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.rozetka.domain.util.StringObject
import com.rozetka.model.CourseModule
import com.rozetka.model.ModuleType
import com.rozetka.presentation.ui.aboutApplication.AboutScreen
import com.rozetka.presentation.ui.academicPerformance.AcademicPerformanceScreen
import com.rozetka.presentation.ui.article.ArticleDetailScreen
import com.rozetka.presentation.ui.createApplication.CreateApplicationScreen
import com.rozetka.presentation.ui.dialog.DialogScreen
import com.rozetka.presentation.ui.digitalService.DigitalServiceScreen
import com.rozetka.presentation.ui.employees.EmployeesScreen
import com.rozetka.presentation.ui.groupJournal.GroupJournalScreen
import com.rozetka.presentation.ui.guestSearchGroup.GuestSearchGroupScreen
import com.rozetka.presentation.ui.home.HomeScreen
import com.rozetka.presentation.ui.login.LoginScreen
import com.rozetka.presentation.ui.maps.MapsScreen
import com.rozetka.presentation.ui.message.MessagesScreen
import com.rozetka.presentation.ui.moodle.DeadlinesScreen
import com.rozetka.presentation.ui.moodle.MoodleDetailScreen
import com.rozetka.presentation.ui.moodle.MoodleScreen
import com.rozetka.presentation.ui.moodle.quiz.ActiveQuizScreen
import com.rozetka.presentation.ui.moodle.quiz.QuizScreen
import com.rozetka.presentation.ui.pay.PayScreen
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.compose.composable
import com.rozetka.presentation.ui.physEdJournal.PhysEdJournalScreen
import com.rozetka.presentation.ui.profile.ProfileScreen
import com.rozetka.presentation.ui.projectActivity.ProjectActivityScreen
import com.rozetka.presentation.ui.scheduleLink.ScheduleLinkScreen
import com.rozetka.presentation.ui.searchGroup.SearchGroupScreen
import com.rozetka.presentation.ui.sessionSchedule.SessionScheduleScreen
import com.rozetka.presentation.ui.settings.SettingsScreen
import com.rozetka.presentation.ui.shedule.ScheduleScreen
import com.rozetka.presentation.ui.studentCard.StudentCardScreen
import com.rozetka.presentation.ui.searchPeople.UnifiedSearchScreen
import com.rozetka.presentation.ui.students.StudentsScreen
import com.rozetka.presentation.ui.submitanApplication.SubmitAnApplicationScreen
import com.rozetka.presentation.ui.teacherReview.TeacherReviewScreen
import com.rozetka.presentation.ui.teacherSchedule.TeacherScheduleScreen
import com.rozetka.presentation.ui.teachersRaiting.TeacherRatingScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass
) {
    val startDestination = if (StringObject.isGuest) {
        Screen.GuestGroupInput.route
    } else {
        if (StringObject.groupName == "") Screen.Home.route else Screen.Schedule.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        popExitTransition = {
            // Уходящий экран: уменьшается и растворяется
            scaleOut(
                targetScale = 0.9f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0.5f)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 300)
            )
        },
        popEnterTransition = {

            scaleIn(
                initialScale = 0.95f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0.5f)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 300)
            )
        }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    StringObject.isGuest = false

                    val targetRoute = if (StringObject.groupName.isNotEmpty()) {
                        Screen.Schedule.route
                    } else {
                        Screen.Home.route
                    }

                    navController.navigate(targetRoute) {
                        popUpTo(Screen.GuestGroupInput.route) { inclusive = true }
                        popUpTo(Screen.Login.route) { inclusive = true }
                        popUpTo(Screen.AddAccount.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AddAccount.route) {
            LoginScreen(
                onLoginSuccess = {
                    StringObject.isGuest = false
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.GuestGroupInput.route) {
            GuestSearchGroupScreen(navController = navController)
        }

        composable(
            Screen.Dialo.route,
            arguments = listOf(
                navArgument("userName") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType },
                navArgument("avatarUrl") { type = NavType.StringType; defaultValue = "" },
                navArgument("isSubject") { type = NavType.BoolType; defaultValue = false },
                navArgument("opponentData") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val avatarUrl = backStackEntry.arguments?.getString("avatarUrl") ?: ""
            val isSubject = backStackEntry.arguments?.getBoolean("isSubject") ?: false
            val opponentData = backStackEntry.arguments?.getString("opponentData") ?: ""

            DialogScreen(
                navController = navController,
                userId = userId,
                userName = userName,
                avatarURL = avatarUrl,
                isSubject = isSubject,
                opponentData = Uri.decode(opponentData)
            )
        }
        composable(
            route = Screen.ArticleScreen.route,
            arguments = listOf(
                navArgument("url") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
            val articleUrl = Uri.decode(encodedUrl)

            ArticleDetailScreen(
                articleUrl = articleUrl,
                onBackClick = { navController.popBackStack() }
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

        composable(Screen.SearchStudentsScreen.route) {

            StudentsScreen(navController)
        }
        composable(Screen.SearchPeople.route) {
            UnifiedSearchScreen(navController = navController)
        }
        composable(
            route = Screen.ScheduleLink.route + "/{groupName}",
            arguments = listOf(
                navArgument("groupName") { type = NavType.StringType },
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "app://com.rozetka.epotitech/schedulelink/{groupName}"
            })
        ) { backStackEntry ->
            val groupName =
                backStackEntry.arguments?.getString("groupName") ?: StringObject.groupName
            ScheduleLinkScreen(navController, groupName)
        }
        composable(
            route = Screen.Schedule.route,
            deepLinks = listOf(navDeepLink {
                uriPattern = "app://com.rozetka.epotitech/schedule"
            })
        ) {
            ScheduleScreen(navController, )
        }
        composable(
            route = Screen.TeacherReview.route,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val id =
                backStackEntry.arguments?.getString("id") ?: ""
            TeacherReviewScreen(navController, id)
        }
        composable(
            route = Screen.SessionSchedule.route,
            arguments = listOf(
                navArgument("groupName") { type = NavType.StringType },
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "app://com.rozetka.epotitech/SessionSchedule/{groupName}"
            })
        ) { backStackEntry ->
            val groupName =
                backStackEntry.arguments?.getString("groupName") ?: StringObject.groupName
            SessionScheduleScreen(navController, groupName)
        }
        composable(
            route = Screen.PhysGroupJournalScreen.route

        ) {
            GroupJournalScreen(navController)
        }

        composable(
            route = Screen.CreateApplicationScreen.route,
            arguments = listOf(
                navArgument("applicationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getString("applicationId") ?: ""
            CreateApplicationScreen(
                applicationId = applicationId,
                navController = navController
            )
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
            MessagesScreen(navController = navController, windowSizeClass = windowSizeClass.widthSizeClass)
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
        composable(Screen.Moodle.route) {
            MoodleScreen(
                onBackClick = { navController.popBackStack() },
                onCourseClick = { course ->
                    navController.navigate("moodle_detail/${course.id}/${Uri.encode(course.title)}")
                },
                onDeadlinesClick = {
                    navController.navigate(Screen.MoodleDeadlines.route)
                }
            )
        }
        composable(Screen.MoodleDeadlines.route) {
            DeadlinesScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = koinViewModel()
            )
        }
        composable(
            route = Screen.MoodleDetail.route,
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("courseTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            val courseTitle = Uri.decode(backStackEntry.arguments?.getString("courseTitle") ?: "")
            MoodleDetailScreen(
                courseId = courseId,
                courseTitle = courseTitle,
                onBackClick = { navController.popBackStack() },
                onModuleClick = { module ->
                    if (module.type == ModuleType.QUIZ) {
                        navController.navigate("quiz/${module.id}/${Uri.encode(module.name)}")
                    }
                }
            )
        }
        composable(
            route = Screen.Quiz.route,
            arguments = listOf(
                navArgument("quizId") { type = NavType.StringType },
                navArgument("quizTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId") ?: ""
            val quizTitle = Uri.decode(backStackEntry.arguments?.getString("quizTitle") ?: "")
            QuizScreen(
                quizId = quizId,
                quizTitle = quizTitle,
                onBackClick = { navController.popBackStack() },
                onStartAttempt = { attemptUrl ->
                    navController.navigate("active_quiz/${Uri.encode(attemptUrl)}/${Uri.encode(quizTitle)}")
                }
            )
        }
        composable(
            route = Screen.ActiveQuiz.route,
            arguments = listOf(
                navArgument("quizUrl") { type = NavType.StringType },
                navArgument("quizTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val quizUrl = Uri.decode(backStackEntry.arguments?.getString("quizUrl") ?: "")
            val quizTitle = Uri.decode(backStackEntry.arguments?.getString("quizTitle") ?: "")
            
            ActiveQuizScreen(
                attemptUrl = quizUrl,
                quizTitle = quizTitle,
                onBackClick = { navController.popBackStack() }
            )
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