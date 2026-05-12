package com.alvinfungai.coworkapp.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder


interface BaseNavGraph {

    fun build(
        modifier: Modifier,
        navController: NavController,
        navGraphBuilder: NavGraphBuilder
    )
}
