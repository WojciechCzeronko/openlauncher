package com.openlauncher.app.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
import com.here.sdk.routing.Route

@Stable
class Aw11HereMapState {

    var isSearchOpen by mutableStateOf(false)

    var searchQuery by mutableStateOf("")

    var searchResults by mutableStateOf<List<HereSearchResult>>(
        emptyList()
    )
    var isSearching by mutableStateOf(false)

    var searchError by mutableStateOf<String?>(null)

    var activeRoute by mutableStateOf<Route?>(null)

    var isFollowing by mutableStateOf(true)

    var isRecentering by mutableStateOf(false)

    var mapSize by mutableStateOf(IntSize.Zero)

    fun openSearch() {
        isSearchOpen = true
    }

    fun clearSearch() {
        searchQuery = ""
        searchResults = emptyList()
        searchError = null
    }

    fun closeSearch() {
        isSearchOpen = false
        clearSearch()
    }

    fun startSearch() {
        isSearching = true
        searchError = null
        searchResults = emptyList()
    }

    fun completeSearch(
        results: List<HereSearchResult>
    ) {
        searchResults = results
        isSearching = false
    }

    fun failSearch(
        error: String
    ) {
        searchError = error
        isSearching = false
    }
}

@Composable
fun rememberAw11HereMapState(): Aw11HereMapState {
    return remember {
        Aw11HereMapState()
    }
}