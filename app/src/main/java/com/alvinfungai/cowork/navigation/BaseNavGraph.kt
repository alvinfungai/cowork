package com.alvinfungai.cowork.navigation

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