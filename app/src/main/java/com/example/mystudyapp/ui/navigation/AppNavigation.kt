package com.example.mystudyapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mystudyapp.data.model.AuthState
import com.example.mystudyapp.ui.screen.auth.LoginScreen
import com.example.mystudyapp.ui.screen.auth.RegisterScreen
import com.example.mystudyapp.ui.screen.grammar.GrammarScreen
import com.example.mystudyapp.ui.screen.home.HomeScreen
import com.example.mystudyapp.ui.screen.learn.LearnScreen
import com.example.mystudyapp.ui.screen.learn.LevelUnitsScreen
import com.example.mystudyapp.ui.screen.learn.ProgramOverviewScreen
import com.example.mystudyapp.ui.screen.learn.UnitLessonsScreen
import com.example.mystudyapp.ui.screen.lesson.LessonScreen
import com.example.mystudyapp.ui.screen.listening.ListeningScreen
import com.example.mystudyapp.ui.screen.pronunciation.PronunciationScreen
import com.example.mystudyapp.ui.screen.quiz.QuizScreen
import com.example.mystudyapp.ui.screen.vocabulary.VocabularyScreen
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.viewmodel.AuthViewModel
import com.example.mystudyapp.viewmodel.AuthViewModelFactory

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory())
) {
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    val safeNavigate: (String) -> Unit = { route ->
        if (navController.currentDestination?.route != route) {
            if (route.startsWith("program/") || route.startsWith("level/") || route.startsWith("unit/") || route.startsWith("lesson/") || route.startsWith("vocabulary/") || route.startsWith("grammar/") || route.startsWith("listening/") || route.startsWith("pronunciation/") || route.startsWith("quiz/")) {
                navController.navigate(route)
            } else {
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    when (authUiState.authState) {
        is AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = LingoBlue)
            }
        }

        is AuthState.Unauthenticated -> {
            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(
                        onNavigateToRegister = {
                            navController.navigate("register")
                        },
                        viewModel = authViewModel
                    )
                }
                composable("register") {
                    RegisterScreen(
                        onNavigateToLogin = {
                            navController.navigate("login") {
                                popUpTo("register") { inclusive = true }
                            }
                        },
                        viewModel = authViewModel
                    )
                }
            }
        }

        is AuthState.Authenticated -> {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    HomeScreen(onNavigate = safeNavigate)
                }
                composable("learn") {
                    LearnScreen(onNavigate = safeNavigate)
                }
                composable(
                    route = "program/{programId}",
                    arguments = listOf(navArgument("programId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val programId = backStackEntry.arguments?.getString("programId") ?: ""
                    ProgramOverviewScreen(
                        programId = programId,
                        onBackClick = { navController.popBackStack() },
                        onSelectLevel = { levelId ->
                            navController.navigate("level/$levelId")
                        }
                    )
                }
                composable(
                    route = "level/{levelId}",
                    arguments = listOf(navArgument("levelId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val levelId = backStackEntry.arguments?.getString("levelId") ?: ""
                    LevelUnitsScreen(
                        levelId = levelId,
                        onBackClick = { navController.popBackStack() },
                        onSelectUnit = { unitId ->
                            navController.navigate("unit/$unitId")
                        }
                    )
                }
                composable(
                    route = "unit/{unitId}",
                    arguments = listOf(navArgument("unitId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val unitId = backStackEntry.arguments?.getString("unitId") ?: ""
                    UnitLessonsScreen(
                        unitId = unitId,
                        onBackClick = { navController.popBackStack() },
                        onSelectLesson = { lessonId ->
                            navController.navigate("lesson/$lessonId")
                        }
                    )
                }
                composable(
                    route = "lesson/{lessonId}",
                    arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                    LessonScreen(
                        lessonId = lessonId,
                        onBackClick = { navController.popBackStack() },
                        onNavigateToVocabulary = { navController.navigate("vocabulary/$lessonId") },
                        onNavigateToGrammar = { navController.navigate("grammar/$lessonId") },
                        onNavigateToListening = { navController.navigate("listening/$lessonId") },
                        onNavigateToPronunciation = { navController.navigate("pronunciation/$lessonId") },
                        onNavigateToQuiz = { navController.navigate("quiz/$lessonId") }
                    )
                }
                composable(
                    route = "vocabulary/{lessonId}",
                    arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                    VocabularyScreen(
                        lessonId = lessonId,
                        onBackToLesson = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(
                    route = "grammar/{lessonId}",
                    arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                    GrammarScreen(
                        lessonId = lessonId,
                        onBackToLesson = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(
                    route = "listening/{lessonId}",
                    arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                    ListeningScreen(
                        lessonId = lessonId,
                        onBackToLesson = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(
                    route = "pronunciation/{lessonId}",
                    arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                    PronunciationScreen(
                        lessonId = lessonId,
                        onBackToLesson = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(
                    route = "quiz/{lessonId}",
                    arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                    QuizScreen(
                        lessonId = lessonId,
                        onBackToLesson = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
