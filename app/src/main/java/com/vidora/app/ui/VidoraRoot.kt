package com.vidora.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vidora.app.ui.icons.IconHome
import com.vidora.app.ui.icons.IconLibrary
import com.vidora.app.ui.icons.IconSearch
import com.vidora.app.ui.icons.IconSubscriptions
import com.vidora.app.ui.screens.ChannelScreen
import com.vidora.app.ui.screens.HistoryScreen
import com.vidora.app.ui.screens.HomeScreen
import com.vidora.app.ui.screens.LibraryScreen
import com.vidora.app.ui.screens.MyPlaylistScreen
import com.vidora.app.ui.screens.PlaylistScreen
import com.vidora.app.ui.screens.PlayerScreen
import com.vidora.app.ui.screens.SearchScreen
import com.vidora.app.ui.screens.SettingsScreen
import com.vidora.app.ui.screens.SubscriptionsScreen
import com.vidora.app.ui.screens.WatchLaterScreen

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val SUBS = "subs"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val HISTORY = "history"
    const val WATCH_LATER = "watchlater"
    const val VIDEO = "video/{videoId}"
    const val CHANNEL = "channel/{channelId}"
    const val PLAYLIST = "playlist/{playlistId}"
    const val MY_PLAYLIST = "myplaylist/{playlistId}"

    fun video(id: String) = "video/$id"
    fun channel(id: String) = "channel/$id"
    fun playlist(id: String) = "playlist/$id"
    fun myPlaylist(id: String) = "myplaylist/$id"
}

private data class Tab(
    val route: String,
    val label: String,
    val icon: @Composable (Color, Boolean) -> Unit
)

private val tabs = listOf(
    Tab(Routes.HOME, "Home") { tint, selected -> IconHome(tint, selected) },
    Tab(Routes.SEARCH, "Search") { tint, _ -> IconSearch(tint) },
    Tab(Routes.SUBS, "Subscriptions") { tint, selected -> IconSubscriptions(tint, selected) },
    Tab(Routes.LIBRARY, "Library") { tint, selected -> IconLibrary(tint, selected) }
)

@Composable
fun VidoraRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBar = tabs.any { it.route == currentRoute }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = showBar,
                enter = fadeIn(tween(160)) + slideInVertically(tween(180)) { it / 3 },
                exit = fadeOut(tween(120)) + slideOutVertically(tween(150)) { it / 3 }
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    tabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                tab.icon(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    selected
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            // Only pad for the bar when it is actually shown; full-screen pages
            // (player, settings, channel) get the whole window.
            modifier = if (showBar) Modifier.padding(padding) else Modifier
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onVideoClick = { navController.navigate(Routes.video(it.id)) },
                    onChannelClick = { navController.navigate(Routes.channel(it)) },
                    onSearchClick = { navController.navigate(Routes.SEARCH) },
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) }
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    onVideoClick = { navController.navigate(Routes.video(it.id)) },
                    onChannelClick = { navController.navigate(Routes.channel(it)) }
                )
            }
            composable(Routes.SUBS) {
                SubscriptionsScreen(
                    onVideoClick = { navController.navigate(Routes.video(it.id)) },
                    onChannelClick = { navController.navigate(Routes.channel(it)) }
                )
            }
            composable(Routes.LIBRARY) {
                LibraryScreen(
                    onVideoClick = { navController.navigate(Routes.video(it.id)) },
                    onHistoryClick = { navController.navigate(Routes.HISTORY) },
                    onWatchLaterClick = { navController.navigate(Routes.WATCH_LATER) },
                    onMyPlaylistClick = { navController.navigate(Routes.myPlaylist(it)) },
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.HISTORY) {
                HistoryScreen(
                    onBack = { navController.popBackStack() },
                    onVideoClick = { navController.navigate(Routes.video(it.id)) },
                    onChannelClick = { navController.navigate(Routes.channel(it)) }
                )
            }
            composable(Routes.WATCH_LATER) {
                WatchLaterScreen(
                    onBack = { navController.popBackStack() },
                    onVideoClick = { navController.navigate(Routes.video(it.id)) },
                    onChannelClick = { navController.navigate(Routes.channel(it)) }
                )
            }
            composable(
                route = Routes.VIDEO,
                arguments = listOf(navArgument("videoId") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("videoId").orEmpty()
                PlayerScreen(
                    videoId = id,
                    onBack = { navController.popBackStack() },
                    onVideoClick = { navController.navigate(Routes.video(it.id)) },
                    onChannelClick = { navController.navigate(Routes.channel(it)) }
                )
            }
            composable(
                route = Routes.CHANNEL,
                arguments = listOf(navArgument("channelId") { type = NavType.StringType })
            ) { entry ->
                ChannelScreen(
                    channelId = entry.arguments?.getString("channelId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onVideoClick = { navController.navigate(Routes.video(it.id)) }
                )
            }
            composable(
                route = Routes.PLAYLIST,
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
            ) { entry ->
                PlaylistScreen(
                    playlistId = entry.arguments?.getString("playlistId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onVideoClick = { navController.navigate(Routes.video(it.id)) },
                    onChannelClick = { navController.navigate(Routes.channel(it)) }
                )
            }
            composable(
                route = Routes.MY_PLAYLIST,
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
            ) { entry ->
                MyPlaylistScreen(
                    playlistId = entry.arguments?.getString("playlistId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onVideoClick = { navController.navigate(Routes.video(it.id)) }
                )
            }
        }
    }
}
