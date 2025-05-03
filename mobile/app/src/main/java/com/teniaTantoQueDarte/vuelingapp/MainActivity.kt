package com.teniaTantoQueDarte.vuelingapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.teniaTantoQueDarte.vuelingapp.navigation.MainNavigationGraph
import com.teniaTantoQueDarte.vuelingapp.navigation.navigationCategories
import com.teniaTantoQueDarte.vuelingapp.ui.theme.VuelingAppTheme
import com.teniaTantoQueDarte.vuelingapp.ui.viewmodel.HomeViewmodel
import androidx.lifecycle.ViewModelProvider
import com.teniaTantoQueDarte.vuelingapp.ui.viewmodel.ProfileViewModel
import com.teniaTantoQueDarte.vuelingapp.workers.NetworkSyncWorker
import androidx.work.Configuration
import androidx.work.WorkManager


@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {


    val profileViewModel by lazy {
        ViewModelProvider(this)[ProfileViewModel::class.java]
    }
    val homeViewModel by lazy {
        ViewModelProvider(this)[HomeViewmodel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetworkSyncWorker.schedulePeriodic(applicationContext)


        setContent {
            VuelingAppTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            navigationCategories.forEach { category ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = if (isCurrentDestinationInHierarchy(
                                                    currentDestination,
                                                    category.route
                                                )
                                            ) {
                                                category.selectedIcon
                                            } else {
                                                category.icon
                                            },
                                            contentDescription = category.title
                                        )
                                    },
                                    label = {
                                        Text(category.title)
                                    },
                                    selected = isCurrentDestinationInHierarchy(
                                        currentDestination,
                                        category.route
                                    ),
                                    onClick = {
                                        navController.navigate(category.route) {
                                            // Pop up to the start destination of the graph to
                                            // avoid building up a large stack of destinations
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = false
                                                inclusive = true
                                            }
                                            // Avoid multiple copies of the same destination when
                                            // reselecting the same item
                                            launchSingleTop = true
                                            // Restore state when reselecting a previously selected item
                                            restoreState = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    MainNavigationGraph(
                        navController = navController,
                        paddingValues = innerPadding,
                        sharedProfileViewModel = profileViewModel, // Pasa el ViewModel compartido
                        sharedHomeViewModel = homeViewModel // Pasa el ViewModel compartido

                    )
                }
            }
        }

    }
}

private fun isCurrentDestinationInHierarchy(currentDestination: NavDestination?, route: Any): Boolean {
    return currentDestination?.hierarchy?.any {
        it.route?.contains(route.toString(), ignoreCase = true) == true
    } ?: false
}