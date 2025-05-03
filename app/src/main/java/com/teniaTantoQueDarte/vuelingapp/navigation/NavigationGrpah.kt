package com.teniaTantoQueDarte.vuelingapp.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.teniaTantoQueDarte.vuelingapp.ui.screen.ProfileScreen
import com.teniaTantoQueDarte.vuelingapp.ui.viewmodel.ProfileViewModel
import androidx.activity.ComponentActivity
import com.teniaTantoQueDarte.vuelingapp.ui.screen.HomeScreen
import com.teniaTantoQueDarte.vuelingapp.ui.viewmodel.HomeViewmodel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavigationGraph(
    navController: NavHostController = rememberNavController(),
    paddingValues: PaddingValues = PaddingValues(0.dp),
    sharedProfileViewModel: ProfileViewModel? = null,
    sharedHomeViewModel: HomeViewmodel? = null

) {

    val context = LocalContext.current
    val profileViewModel = sharedProfileViewModel ?: viewModel(
        viewModelStoreOwner = context as ComponentActivity
    )

    val homeViewmodel = sharedHomeViewModel ?: viewModel(
        viewModelStoreOwner = context as ComponentActivity
    )


    // Reduce la frecuencia de recomposición al recordar las secciones
    val homeSectionBuilder = remember(navController) { { builder: NavGraphBuilder ->
        builder.HomeSection(navController,homeViewmodel)
    }}
    val profileSectionBuilder = remember(navController, profileViewModel) { { builder: NavGraphBuilder ->
        builder.ProfileSection(navController, profileViewModel)
    }}
    val favouritesSectionBuilder = remember(navController) { { builder: NavGraphBuilder ->
        builder.FavouritesSection(navController)
    }}

    NavHost(
        navController = navController,
        startDestination = HomeCategory,
        modifier = Modifier.padding(paddingValues)
    ) {
        homeSectionBuilder(this)
        profileSectionBuilder(this)
        favouritesSectionBuilder(this)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.HomeSection(navController: NavHostController, viewModel: HomeViewmodel) {

    navigation<HomeCategory>(startDestination = HomeDestination) {
        composable<HomeDestination> {
            HomeScreen(
                viewmodel = viewModel
            )

        }
    }
}

fun NavGraphBuilder.ProfileSection(
    navController: NavHostController,
    viewModel: ProfileViewModel
) {
    navigation<ProfileCategory>(startDestination = ProfileDestination) {
        composable<ProfileDestination> {
            ProfileScreen(viewModel = viewModel)
        }
    }
}

fun NavGraphBuilder.FavouritesSection(navController: NavHostController) {
    navigation<FavouritesCategory>(startDestination = FavouritesDestination) {
        composable<FavouritesDestination> {
            // Tu FavouritesScreen aquí
        }
    }
}