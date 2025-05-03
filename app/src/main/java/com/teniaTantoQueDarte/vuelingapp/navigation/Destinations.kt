package com.teniaTantoQueDarte.vuelingapp.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder



@Serializable
object HomeDestination

@Serializable
object PostDestination


@Serializable
object ProfileDestination

@Serializable
object FavouritesDestination


data class NavigationCategory<T:Any>(val route:T, val icon:ImageVector, val selectedIcon:ImageVector, val title:String)
val navigationCategories = listOf(
    NavigationCategory(route = HomeCategory, icon = Icons.Outlined.Home, selectedIcon = Icons.Filled.Home, title = "Home"),
    NavigationCategory(route = FavouritesCategory, icon = Icons.Outlined.FavoriteBorder, selectedIcon = Icons.Filled.Favorite, title = "Favourites"),
    NavigationCategory(route = ProfileCategory, icon = Icons.Outlined.Person, selectedIcon = Icons.Filled.Person, title = "Profile"),
)

@Serializable
object HomeCategory



@Serializable
object ProfileCategory


@Serializable
object FavouritesCategory