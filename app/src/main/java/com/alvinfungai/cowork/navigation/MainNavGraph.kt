package com.alvinfungai.coworkapp.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.alvinfungai.bookings.BookingScreen
import com.alvinfungai.bookings.CreateBookingScreen
import com.alvinfungai.bookings.ProviderBookingsScreen
import com.alvinfungai.bookings.ProviderDashboardScreen
import com.alvinfungai.providers.ExploreRoute
import com.alvinfungai.providers.NotificationsScreen
import com.alvinfungai.providers.ProviderSettingsScreen
import com.alvinfungai.providers.RegisterServiceProviderScreen
import com.alvinfungai.providers.ServiceProviderDetailsScreen
import com.alvinfungai.reviews.ui.AddReviewScreen
import com.alvinfungai.reviews.ui.ReviewListScreen
import com.alvinfungai.users.EditProfileScreen
import com.alvinfungai.users.ProfileRoute
import kotlinx.serialization.Serializable

object MainNavGraph : BaseNavGraph {

    @Serializable data object NavGraph : Destination

    sealed interface Destination {
        @Serializable
        data class Root(val initialTab: String? = null, val initialCategory: String? = null) : Destination

        @Serializable
        data object Explore : Destination
        @Serializable
        data object Bookings : Destination
        @Serializable
        data object Profile : Destination
        @Serializable
        data object EditProfile : Destination

        @Serializable
        data class ProviderDetails(val providerId: String) : Destination
        
        @Serializable
        data class ReviewList(val providerId: String) : Destination

        @Serializable
        data class WriteReview(val bookingId: String, val providerId: String) : Destination

        @Serializable
        data object RegisterServiceProvider : Destination

        @Serializable data class CreateBooking(val providerId: String) : Destination

        @Serializable data object ProviderDashboard : Destination
        @Serializable data object ManageWorkRequests : Destination
        @Serializable data object Notifications : Destination
        @Serializable data object ProviderSettings : Destination
    }

    override fun build(
        modifier: Modifier,
        navController: NavController,
        navGraphBuilder: NavGraphBuilder
    ) {
        navGraphBuilder.navigation<NavGraph>(
            startDestination = Destination.Root()
        ) {
            composable<Destination.Root> { backStackEntry ->
                val args = backStackEntry.toRoute<Destination.Root>()
                MainHomeScreen(
                    parentNavController = navController,
                    initialTab = args.initialTab,
                    initialCategory = args.initialCategory
                )
            }

            composable<Destination.ProviderDetails> { backStackEntry ->
                val details = backStackEntry.toRoute<Destination.ProviderDetails>()
                ServiceProviderDetailsScreen(
                    providerId = details.providerId,
                    onBackClick = { navController.popBackStack() },
                    onBookClick = { id ->
                        navController.navigate(Destination.CreateBooking(id))
                    },
                    onViewReviewsClick = { id ->
                        navController.navigate(Destination.ReviewList(id))
                    },
                    onCategoryClick = { category ->
                        navController.navigate(Destination.Root(initialTab = "Explore", initialCategory = category)) {
                            popUpTo(NavGraph) { inclusive = false }
                        }
                    }
                )
            }

            composable<Destination.ReviewList> { backStackEntry ->
                val args = backStackEntry.toRoute<Destination.ReviewList>()
                ReviewListScreen(
                    providerId = args.providerId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Destination.WriteReview> { backStackEntry ->
                val args = backStackEntry.toRoute<Destination.WriteReview>()
                AddReviewScreen(
                    bookingId = args.bookingId,
                    providerId = args.providerId,
                    onReviewSubmitted = {
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Destination.RegisterServiceProvider> {
                RegisterServiceProviderScreen(
                    onSuccess = {
                        navController.popBackStack()
                    }
                )
            }

            composable<Destination.CreateBooking> { backStackEntry ->
                val args = backStackEntry.toRoute<Destination.CreateBooking>()
                
                CreateBookingScreen(
                    providerId = args.providerId,
                    onSuccess = {
                        navController.navigate(Destination.Root(initialTab = "Bookings")) {
                            popUpTo(NavGraph) { inclusive = false }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Destination.ProviderDashboard> {
                ProviderDashboardScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onManageRequestsClick = {
                        navController.navigate(Destination.ManageWorkRequests)
                    },
                    onNotificationsClick = {
                        navController.navigate(Destination.Notifications)
                    },
                    onSettingsClick = {
                        navController.navigate(Destination.ProviderSettings)
                    }
                )
            }

            composable<Destination.ManageWorkRequests> {
                ProviderBookingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Destination.Notifications> {
                NotificationsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Destination.ProviderSettings> {
                ProviderSettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Destination.EditProfile> {
                EditProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onProfileUpdated = { navController.popBackStack() }
                )
            }
        }
    }

    @Composable
    fun MainHomeScreen(
        parentNavController: NavController,
        initialTab: String? = null,
        initialCategory: String? = null
    ) {
        val nestedNavController = rememberNavController()

        LaunchedEffect(initialTab, initialCategory) {
            when (initialTab) {
                "Bookings" -> {
                    nestedNavController.navigate(Destination.Bookings) {
                        popUpTo(nestedNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                "Profile" -> {
                    nestedNavController.navigate(Destination.Profile) {
                        popUpTo(nestedNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                "Explore" -> {
                    nestedNavController.navigate(Destination.Explore) {
                        popUpTo(nestedNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }

        val items = listOf(
            BottomNavItem("Explore", Destination.Explore, Icons.Default.Search),
            BottomNavItem("Bookings", Destination.Bookings, Icons.Default.DateRange),
            BottomNavItem("Profile", Destination.Profile, Icons.Default.Person),
        )

        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.name) },
                            label = { Text(item.name) },
                            selected = currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true,
                            onClick = {
                                nestedNavController.navigate(item.route) {
                                    popUpTo(nestedNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = nestedNavController,
                startDestination = Destination.Explore,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable<Destination.Explore> {
                    ExploreRoute(
                        initialCategory = if (initialTab == "Explore") initialCategory else null,
                        onProviderClick = { providerId ->
                            parentNavController.navigate(Destination.ProviderDetails(providerId))
                        }
                    )
                }

                composable<Destination.Bookings> {
                    BookingScreen(
                        onWriteReview = { bookingId, providerId ->
                            parentNavController.navigate(Destination.WriteReview(bookingId, providerId))
                        }
                    )
                }

                composable<Destination.Profile> {
                    ProfileRoute(
                        onLogout = {
                            parentNavController.navigate(AuthNavGraph.Destination.Root) {
                                popUpTo(NavGraph) { inclusive = true }
                            }
                        },
                        onRegisterAsServiceProvider = {
                            parentNavController.navigate(Destination.RegisterServiceProvider)
                        },
                        onProviderDashboardClick = {
                            parentNavController.navigate(Destination.ProviderDashboard)
                        },
                        onEditProfileClick = {
                            parentNavController.navigate(Destination.EditProfile)
                        }
                    )
                }
            }
        }
    }

    data class BottomNavItem(
        val name: String,
        val route: Destination,
        val icon: ImageVector
    )
}
