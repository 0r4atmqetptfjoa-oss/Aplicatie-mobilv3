package com.example.educationalapp.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Always return to [GamesMenuRoute] when leaving a mini-game.
 *
 * - If GamesMenu is already on the back stack -> we pop back to it.
 * - Otherwise -> we navigate to it and clear to the graph start destination.
 */
fun NavController.backToGamesMenu() {
    val popped = runCatching { popBackStack<GamesMenuRoute>(inclusive = false) }.getOrDefault(false)
    if (!popped) {
        navigate(GamesMenuRoute) {
            popUpTo(graph.findStartDestination().id) { inclusive = false }
            launchSingleTop = true
        }
    }
}
