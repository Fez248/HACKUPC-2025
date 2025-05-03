package com.teniaTantoQueDarte.vuelingapp.navigation


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.teniaTantoQueDarte.vuelingapp.ui.screen.ProfileScreen


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavigationGraph(
    navController: NavHostController = rememberNavController(),
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {


    NavHost(
        navController = navController,
        startDestination = HomeCategory,
        modifier = Modifier.padding(paddingValues)
    ) {
        HomeSection(navController)
        ProfileSection(navController)
        FavouritesSection(navController)
    }
}



// Extension functions for each navigation section
@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.HomeSection(navController: NavHostController) {
    navigation<HomeCategory>(startDestination = HomeDestination) {
        composable<HomeDestination> {

        }
    }
}


fun NavGraphBuilder.ProfileSection(navController: NavHostController) {
    navigation<ProfileCategory>(startDestination = ProfileDestination) {
        composable<ProfileDestination> {
            ProfileScreen()
        }
    }
}


fun NavGraphBuilder.FavouritesSection(navController: NavHostController) {
    navigation<FavouritesCategory>(startDestination = FavouritesDestination) {
        composable<FavouritesDestination>{

        }
    }
}