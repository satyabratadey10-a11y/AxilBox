package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.AddInstanceScreen
import com.example.ui.screens.BootScreen
import com.example.ui.screens.DeveloperOptionsScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.PureBlack
import com.example.ui.viewmodel.VMViewModel

object Destinations {
    const val MAIN_MENU = "main_menu"
    const val ADD_INSTANCE = "add_instance"
    const val BOOT_INSTANCE = "boot_instance/{instanceId}"
    const val DEVELOPER_OPTIONS = "developer_options"
    const val SETTINGS = "settings"

    fun bootInstance(id: Int) = "boot_instance/$id"
}

@Composable
fun VMNavigation(
    viewModel: VMViewModel,
    navController: NavHostController = rememberNavController()
) {
    val instances by viewModel.instances.collectAsState()
    val transitionDuration = 250

    NavHost(
        navController = navController,
        startDestination = Destinations.MAIN_MENU,
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack),
        enterTransition = {
            fadeIn(animationSpec = tween(transitionDuration, easing = EaseInOut)) +
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(transitionDuration, easing = EaseInOut)
                )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(transitionDuration, easing = EaseInOut)) +
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(transitionDuration, easing = EaseInOut)
                )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(transitionDuration, easing = EaseInOut)) +
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(transitionDuration, easing = EaseInOut)
                )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(transitionDuration, easing = EaseInOut)) +
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(transitionDuration, easing = EaseInOut)
                )
        }
    ) {
        composable(Destinations.MAIN_MENU) {
            MainMenuScreen(
                instances = instances,
                onAddInstanceClick = {
                    navController.navigate(Destinations.ADD_INSTANCE)
                },
                onInstanceClick = { instance ->
                    navController.navigate(Destinations.bootInstance(instance.id))
                },
                onDeleteInstance = { instance ->
                    viewModel.deleteInstance(instance)
                },
                onNavigateToDeveloperOptions = {
                    navController.navigate(Destinations.DEVELOPER_OPTIONS)
                },
                onNavigateToSettings = {
                    navController.navigate(Destinations.SETTINGS)
                }
            )
        }

        composable(Destinations.ADD_INSTANCE) {
            AddInstanceScreen(
                onSaveInstance = { name, iconUri, osImageUri, ramMb, storageGb ->
                    viewModel.addInstance(
                        name = name,
                        iconUri = iconUri,
                        osImageUri = osImageUri,
                        ramMb = ramMb,
                        storageGb = storageGb,
                        onSuccess = {
                            navController.popBackStack()
                        }
                    )
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Destinations.BOOT_INSTANCE,
            arguments = listOf(
                navArgument("instanceId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val instanceId = backStackEntry.arguments?.getInt("instanceId") ?: -1
            val instance = instances.find { it.id == instanceId }

            BootScreen(
                instance = instance,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Destinations.DEVELOPER_OPTIONS) {
            DeveloperOptionsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Destinations.SETTINGS) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
