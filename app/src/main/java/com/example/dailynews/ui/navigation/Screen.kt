package com.oxclub.dailynews.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Categories : Screen("categories")
    data object Search : Screen("search")
    data object Bookmarks : Screen("bookmarks")
    data object Article : Screen("article/{url}") {
        fun createRoute(url: String) = "article/${android.net.Uri.encode(url)}"
    }
    data object About : Screen("about")
    data object Privacy : Screen("privacy")
}
