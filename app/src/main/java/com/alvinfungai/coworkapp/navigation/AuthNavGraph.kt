package com.alvinfungai.coworkapp.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.alvinfungai.users.AuthScreen
import kotlinx.serialization.Serializable

object AuthNavGraph: BaseNavGraph {

    sealed interface Destination {

        @Serializable
        data object Root : Destination

        @Serializable
        data object Auth : Destination
    }
    override fun build(
        modifier: Modifier,
        navController: NavController,
        navGraphBuilder: NavGraphBuilder
    ) {
        navGraphBuilder.navigation<Destination.Root>(startDestination = Destination.Auth) {
            composable<Destination.Auth> {
                AuthScreen(
                    modifier = modifier,
                    onAuthSuccess = {
                        // 1. Navigate to the Main Graph using an instance of Root()
                        navController.navigate(MainNavGraph.Destination.Root()) {
                            // 2. Pop the entire Auth Graph so user can't go back
                            popUpTo(Destination.Root) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}
